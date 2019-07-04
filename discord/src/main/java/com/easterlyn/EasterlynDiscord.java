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
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.TextChannel;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.object.util.Snowflake;
import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
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
			client = new DiscordClientBuilder(token).setInitialPresence(Presence.online(Activity.playing("play.easterlyn.com"))).build();
			client.login().subscribe(aVoid -> new MinecraftBridge(this, client).setup());
		});

		ReportableEvent.getHandlerList().register(new SimpleListener<>(ReportableEvent.class, event ->
				postMessage(ChannelType.REPORT, event.getMessage() + (event.hasTrace() ? event.getTrace() : "")), this));

		RegisteredServiceProvider<Easterlyn> registration = getServer().getServicesManager().getRegistration(Easterlyn.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class, event -> {
			if (event.getPlugin() instanceof Easterlyn) {
				register((Easterlyn) event.getPlugin());
			}
		}, this));
	}

	private void register(Easterlyn plugin) {
		plugin.registerCommands("com.easterlyn.discord.command");
		plugin.getCommandManager().registerDependency(this.getClass(), this);
	}

	public Collection<TextChannel> getChannelIDs(ChannelType type) {
		if (Bukkit.isPrimaryThread()) {
			throw new IllegalStateException("Don't demand information from Discord on the main thread.");
		}
		List<TextChannel> list = new ArrayList<>();
		if (!this.isEnabled()) {
			return list;
		}
		ConfigurationSection guildSection = this.getConfig().getConfigurationSection("guilds");
		if (guildSection == null) {
			return list;
		}

		for (String guildIDString : guildSection.getKeys(false)) {
			// Parse guild ID
			Snowflake guildID;
			try {
				guildID = Snowflake.of(guildIDString);
			} catch (NumberFormatException e) {
				continue;
			}

			Guild guild = client.getGuildById(guildID).block();
			// Ensure valid guild
			if (guild == null) {
				continue;
			}

			String channelIdString = guildSection.getString(guildIDString + '.' + type);
			if (channelIdString == null) {
				continue;
			}

			Snowflake snowflake;
			try {
				snowflake = Snowflake.of(channelIdString);
			} catch (NumberFormatException e) {
				continue;
			}
			TextChannel channel = guild.getChannelById(snowflake).cast(TextChannel.class).block();

			if (channel != null) {
				list.add(channel);
			}
		}
		return list;
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
		RegisteredServiceProvider<Easterlyn> registration = getServer().getServicesManager().getRegistration(Easterlyn.class);
		if (registration == null) {
			throw new IllegalStateException("Easterlyn-Core not enabled!");
		}
		return new DiscordUser(registration.getProvider().getUserManager().getUser(uuid));
	}

	public void postMessage(ChannelType channelType, String message) {
		if (channelType.getAggregateTime() > 0) {
			Pair<StringBuffer, Long> aggregateData = messageQueue.get(channelType);
			if (aggregateData == null) {
				aggregateData = new Pair<>(new StringBuffer(), 0L);
				messageQueue.put(channelType, aggregateData);
			}

			// Max message length is 2000. Cap aggregation to 1900 to be safe.
			if (aggregateData.getLeft().length() + message.length() + 1 > 1900) {
				postAggregatedMessage(channelType, aggregateData.getLeft().toString());
				aggregateData.getLeft().delete(0, aggregateData.getLeft().length());
			}

			if (aggregateData.getLeft().length() > 0) {
				aggregateData.getLeft().append('\n');
			}
			aggregateData.getLeft().append(message);

			if (aggregateData.getRight() <= System.currentTimeMillis()) {
				aggregateData.setRight(System.currentTimeMillis() + channelType.getAggregateTime());
				Pair<StringBuffer, Long> data = aggregateData;
				getServer().getScheduler().runTaskLaterAsynchronously(this, () -> {
					postAggregatedMessage(channelType, data.getLeft().toString());
					data.getLeft().delete(0, data.getLeft().length());

					// Typing status while aggregating
					getChannelIDs(channelType).forEach(channel ->
							channel.typeUntil(Mono.delay(Duration.ofMillis(channelType.getAggregateTime()))));
				}, channelType.getAggregateTime() / 20);
			}


			return;
		}
		postAggregatedMessage(channelType, message);
	}

	private void postAggregatedMessage(ChannelType channelType, String message) {
		if (!client.isConnected()) {
			// TODO handle client not connected
			return;
		}

		while (message.length() > 1900) {
			String search = message.substring(0, 1900);
			int index = search.lastIndexOf('\n');
			if (index > -1) {
				postAggregatedMessage(channelType, message.substring(0, index));
				// Ignore newline.
				message = message.substring(index + 1);
				continue;
			}
			postAggregatedMessage(channelType, message.substring(0, 1900));
			message = message.substring(1900);
		}

		String finalMessage = message.trim();
		if (finalMessage.isEmpty()) {
			return;
		}


		getChannelIDs(channelType).forEach(channel -> channel.createMessage(finalMessage));

	}


}
