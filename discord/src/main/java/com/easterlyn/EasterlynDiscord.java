package com.easterlyn;

import com.easterlyn.discord.ChannelType;
import com.easterlyn.discord.DiscordUser;
import com.easterlyn.discord.MinecraftBridge;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.tuple.Pair;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.object.entity.GuildMessageChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import java.io.File;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Phaser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import reactor.core.publisher.Mono;

public class EasterlynDiscord extends JavaPlugin {

	private final Map<ChannelType, Pair<StringBuffer, Long>> messageQueue = new ConcurrentHashMap<>();
	private DiscordClient client;
	private ConcurrentConfiguration datastore;

	@Override
	public void onEnable() {
		saveDefaultConfig();
		datastore = ConcurrentConfiguration.load(new File(getDataFolder(), "datastore.yml"));

		String token = getConfig().getString("token");
		if (token == null || token.isEmpty()) {
			getLogger().warning("No token provided! Nothing to do.");
			return;
		}

		getServer().getScheduler().runTaskAsynchronously(this, () -> {
			client = DiscordClientBuilder.create(token).setInitialPresence(Presence.online(Activity.playing("play.easterlyn.com"))).build();
			new MinecraftBridge(this, client).setup();
			client.login().subscribe();
		});

		ReportableEvent.getHandlerList().register(new SimpleListener<>(ReportableEvent.class, event ->
				postMessage(ChannelType.REPORT, event.getMessage() + (event.hasTrace() ? event.getTrace() : "")), this));

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class, event -> {
			if (event.getPlugin() instanceof EasterlynCore) {
				register((EasterlynCore) event.getPlugin());
			}
		}, this));
	}

	private void register(EasterlynCore plugin) {
		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.discord.command");
		plugin.getLocaleManager().addLocaleSupplier(this);
	}

	public boolean isChannelType(Snowflake channelID, ChannelType type) {
		ConfigurationSection guildSection = this.getConfig().getConfigurationSection("guilds");
		if (guildSection == null) {
			return false;
		}

		String channelIdString = channelID.asString();
		for (String guildIDString : guildSection.getKeys(false)) {
			if (channelIdString.equals(guildSection.getString(guildIDString + ".channels." + type.getPath()))) {
				return true;
			}
		}

		return false;
	}

	public Collection<GuildMessageChannel> getChannelIDs(ChannelType type) {
		if (Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("Don't demand information from Discord on the main thread.");
		}
		Collection<GuildMessageChannel> collection = Collections.newSetFromMap(new ConcurrentHashMap<>());
		if (!this.isEnabled() || client == null) {
			return collection;
		}
		ConfigurationSection guildSection = this.getConfig().getConfigurationSection("guilds");
		if (guildSection == null) {
			return collection;
		}

		Phaser phaser = new Phaser(1);
		for (String guildIDString : guildSection.getKeys(false)) {
			// Parse guild ID
			Snowflake guildID;
			try {
				guildID = Snowflake.of(guildIDString);
			} catch (NumberFormatException e) {
				continue;
			}

			String channelIdString = guildSection.getString(guildIDString + ".channels." + type.getPath());
			if (channelIdString == null) {
				continue;
			}
			Snowflake channelID;
			try {
				channelID = Snowflake.of(channelIdString);
			} catch (NumberFormatException e) {
				continue;
			}

			phaser.register();
			client.getGuildById(guildID)
					.flatMap(guild -> guild.getChannelById(channelID).cast(GuildMessageChannel.class).doOnSuccess(collection::add))
					.doOnSuccessOrError((obj, thrown) -> {
						if (thrown != null) {
							thrown.printStackTrace();
						}
						phaser.arriveAndDeregister();
					}).subscribe();
		}

		phaser.arriveAndAwaitAdvance();
		return collection;
	}

	@Nullable
	public DiscordUser getUser(@NotNull Snowflake id) throws IllegalStateException {
		String uuidString = datastore.getString("link." + id.asString());
		if (uuidString == null) {
			return null;
		}
		return getUser(UUID.fromString(uuidString));
	}

	@NotNull
	public DiscordUser getUser(@NotNull UUID uuid) throws IllegalStateException {
		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration == null) {
			throw new IllegalStateException("EasterlynCore not enabled!");
		}
		return new DiscordUser(registration.getProvider().getUserManager().getUser(uuid));
	}

	public void postMessage(ChannelType channelType, String message) {
		if (!client.isConnected()) {
			// TODO handle client not connected
			return;
		}

		if (channelType == ChannelType.MAIN) {
			postMessage(ChannelType.LOG, message);
		}

		if (channelType.getAggregateTime() > 0) {
			Pair<StringBuffer, Long> aggregateData = messageQueue.get(channelType);
			if (aggregateData == null) {
				aggregateData = new Pair<>(new StringBuffer(), 0L);
				messageQueue.put(channelType, aggregateData);
			}

			// Max message length is 2000. Cap aggregation to 1900 to be safe.
			if (aggregateData.getLeft().length() + message.length() + 1 > 1900) {
				directPostMessage(channelType, aggregateData.getLeft().toString());
				aggregateData.getLeft().delete(0, aggregateData.getLeft().length());
			}

			if (aggregateData.getLeft().length() > 0) {
				aggregateData.getLeft().append('\n');
			}
			aggregateData.getLeft().append(message);

			if (aggregateData.getRight() <= System.currentTimeMillis()) {
				// Typing status while aggregating
				getChannelIDs(channelType).forEach(channel ->
						channel.typeUntil(Mono.delay(Duration.ofMillis(channelType.getAggregateTime()))).subscribe());

				aggregateData.setRight(System.currentTimeMillis() + channelType.getAggregateTime());
				Pair<StringBuffer, Long> data = aggregateData;
				getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
					directPostMessage(channelType, data.getLeft().toString());
					data.getLeft().delete(0, data.getLeft().length());
				}, channelType.getAggregateTime() / 20);
			}

			return;
		}

		directPostMessage(channelType, message);
	}

	private void directPostMessage(ChannelType channelType, String message) {
		if (!client.isConnected()) {
			// TODO handle client not connected
			return;
		}

		while (message.length() > 1900) {
			String search = message.substring(0, 1900);
			int index = search.lastIndexOf('\n');
			if (index > -1) {
				directPostMessage(channelType, message.substring(0, index));
				// Ignore newline.
				message = message.substring(index + 1);
				continue;
			}
			directPostMessage(channelType, message.substring(0, 1900));
			message = message.substring(1900);
		}

		String finalMessage = message.trim();
		if (finalMessage.isEmpty()) {
			return;
		}


		getChannelIDs(channelType).forEach(channel -> channel.createMessage(finalMessage).subscribe());

	}


}
