package com.easterlyn;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.ContextResolver;
import com.easterlyn.kitchensink.combo.BackCommand;
import com.easterlyn.kitchensink.combo.DeathPointCommand;
import com.easterlyn.kitchensink.combo.FreeCarts;
import com.easterlyn.kitchensink.combo.LoginCommands;
import com.easterlyn.kitchensink.combo.Meteors;
import com.easterlyn.kitchensink.listener.BottleExperience;
import com.easterlyn.kitchensink.listener.CartContainerCrasher;
import com.easterlyn.kitchensink.listener.ColorSignText;
import com.easterlyn.kitchensink.listener.DeathCoordinates;
import com.easterlyn.kitchensink.listener.DeathDropProtection;
import com.easterlyn.kitchensink.listener.FortuneShears;
import com.easterlyn.kitchensink.listener.KillerRabbit;
import com.easterlyn.kitchensink.listener.NoCommandPrefix;
import com.easterlyn.kitchensink.listener.NoCreativeCrammingDrops;
import com.easterlyn.kitchensink.listener.NoIllegalName;
import com.easterlyn.kitchensink.listener.OnlyWitherKillsItems;
import com.easterlyn.kitchensink.listener.PVPKeepInventory;
import com.easterlyn.kitchensink.listener.RestrictCreativeItems;
import com.easterlyn.kitchensink.listener.WitherFacts;
import com.easterlyn.util.event.SimpleListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EasterlynKitchenSink extends JavaPlugin {

	private List<BaseCommand> extraCommands = new ArrayList<>();

	@Override
	public void onEnable() {

		saveDefaultConfig();

		/* TODO
		 *  - CommandRedirect? probably unnecessary, replacing essentials
		 *    - PlayerCommandPreprocessEvent
		 *  - Portals?
		 *  - PlayerListHeaderFooterWelcome
		 *  - IPCache
		 *    - Used for seen
		 *    - ServerListPingEvent -> replace "Player"
		 */

		// Feature: Command for returning to last location teleported (unnaturally) from.
		BackCommand backCommand = new BackCommand();
		getServer().getPluginManager().registerEvents(backCommand, this);
		extraCommands.add(backCommand);

		// Feature: Command for teleporting to last death location.
		DeathPointCommand deathCommand = new DeathPointCommand();
		getServer().getPluginManager().registerEvents(deathCommand, this);
		extraCommands.add(deathCommand);

		// Feature: Temporary carts for official server rails. No clogging, no running low.
		FreeCarts freeCarts = new FreeCarts(this);
		getServer().getPluginManager().registerEvents(freeCarts, this);
		extraCommands.add(freeCarts);

		// Feature: Configurable commands run on login.
		LoginCommands loginCommands = new LoginCommands();
		getServer().getPluginManager().registerEvents(loginCommands, this);
		extraCommands.add(loginCommands);

		// Feature: Meteorites. Who doesn't love 'em? Most people, that's who.
		Meteors meteors = new Meteors(this);
		getServer().getPluginManager().registerEvents(meteors, this);
		extraCommands.add(meteors);

		// Feature: Bottle experience by right clicking with an empty bottle.
		getServer().getPluginManager().registerEvents(new BottleExperience(), this);

		// Feature: Allow color codes on signs via &
		getServer().getPluginManager().registerEvents(new ColorSignText(), this);

		// Feature: Insert carts into dispensers/droppers when crashed into.
		getServer().getPluginManager().registerEvents(new CartContainerCrasher(), this);

		// Feature: Send player their coordinates when they die.
		getServer().getPluginManager().registerEvents(new DeathCoordinates(), this);

		// Feature: Items dropped on death cannot be damaged.
		getServer().getPluginManager().registerEvents(new DeathDropProtection(this), this);

		// Feature: Fortune works on shears
		getServer().getPluginManager().registerEvents(new FortuneShears(), this);

		// Feature: Killer rabbit has a 1/1000 chance to spawn
		getServer().getPluginManager().registerEvents(new KillerRabbit(), this);

		// Feature: Permission is required to use prefixes in commands.
		getServer().getPluginManager().registerEvents(new NoCommandPrefix(), this);

		// Feature: Entities killed by creative players or cramming do not drop loot or exp.
		getServer().getPluginManager().registerEvents(new NoCreativeCrammingDrops(), this);

		// Feature: Prevent players joining with illegal names
		getServer().getPluginManager().registerEvents(new NoIllegalName(), this);

		// Feature: Dropped items cannot be harmed by any entities other than the Wither.
		getServer().getPluginManager().registerEvents(new OnlyWitherKillsItems(), this);

		// Feature: Keep inventory when dying in PVP, drop a max of 30 exp.
		getServer().getPluginManager().registerEvents(new PVPKeepInventory(), this);

		// Feature: Restrict and clean NBT on items spawned and used in creative.
		getServer().getPluginManager().registerEvents(new RestrictCreativeItems(), this);

		// Fact: Withers are awesome.
		getServer().getPluginManager().registerEvents(new WitherFacts(), this);

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class,
				pluginEnableEvent -> {
					if (pluginEnableEvent.getPlugin() instanceof EasterlynCore) {
						register((EasterlynCore) pluginEnableEvent.getPlugin());
					}
				}, this));
	}

	@Override
	public void onDisable() {
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		EasterlynCore plugin = easterlynProvider.getProvider();

		extraCommands.forEach(command -> plugin.getCommandManager().unregisterCommand(command));
	}

	private void register(@NotNull EasterlynCore plugin) {
		ContextResolver<ChatColor, BukkitCommandExecutionContext> colourResolver = supplier -> {
			String firstArg = supplier.popFirstArg();
			ChatColor matchedColor = null;
			if (firstArg.length() == 1 || firstArg.length() == 2 && firstArg.charAt(0) == '&') {
				matchedColor = ChatColor.getByChar(Character.toLowerCase(firstArg.charAt(firstArg.length() - 1)));
			} else {
				try {
					matchedColor = ChatColor.valueOf(firstArg.toUpperCase());
				} catch (IllegalArgumentException ignored) {}
			}
			if (matchedColor == null || supplier.hasFlag("colour") && !matchedColor.isColor()
					|| supplier.hasFlag("format") && !matchedColor.isFormat()) {
				throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}",
						Arrays.stream(ChatColor.values()).filter(chatColor -> supplier.hasFlag("format")
								? chatColor.isFormat() : !supplier.hasFlag("colour") || chatColor.isColor())
								.map(ChatColor::name).collect(Collectors.joining(", ", "[", "]")));
			}

			return matchedColor;
		};

		plugin.getCommandManager().getCommandContexts().registerContext(ChatColor.class, colourResolver);

		plugin.getCommandManager().getCommandContexts().registerContext(net.md_5.bungee.api.ChatColor.class, context -> colourResolver.getContext(context).asBungee());

		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.kitchensink.command");

		extraCommands.forEach(command -> plugin.getCommandManager().registerCommand(command));

	}

}
