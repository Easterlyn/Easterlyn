package com.easterlyn;

import co.aikar.commands.BukkitCommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.contexts.ContextResolver;
import com.easterlyn.kitchensink.listener.ColorSignText;
import com.easterlyn.kitchensink.listener.FortuneShears;
import com.easterlyn.kitchensink.listener.NoCommandPrefix;
import com.easterlyn.kitchensink.listener.NoIllegalName;
import com.easterlyn.kitchensink.listener.RestrictCreativeItems;
import com.easterlyn.kitchensink.listener.DeathCoordinates;
import com.easterlyn.kitchensink.listener.KillerRabbit;
import com.easterlyn.kitchensink.listener.NoCreativeCrammingDrops;
import com.easterlyn.kitchensink.listener.PVPKeepInventory;
import com.easterlyn.kitchensink.listener.WitherFacts;
import com.easterlyn.util.event.SimpleListener;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class EasterlynKitchenSink extends JavaPlugin {

	@Override
	public void onEnable() {

		/* TODO
		 *  - DeathDropProtection
		 *    - Tag death drops to prevent lava burn
		 *    - EntityDamageEvent +> EntityDamageByEntityEvent
		 *  - ItemProtection
		 *    - Only allow wither/skulls to destroy items
		 *  - BottleExperience
		 *    - ExpBottleEvent -> 10 exp flat, no effect shown
		 *    - PlayerItemConsumeEvent -> update fill cooldown
		 *    - PlayerInteractEvent -> fill if not on create cooldown, update throw cooldown
		 *    - ProjectileLaunchEvent -> throw if not on cooldown
		 *  - CommandRedirect? probably unnecessary, replacing essentials
		 *    - PlayerCommandPreprocessEvent
		 *  - Portals?
		 *  - RebalancedScrap? mcMMO repair kinda replaces smelting down gear
		 *  - FreeCart
		 *    - PlayerDeathEvent -> remove cart
		 *    - PlayerQuitEvent -> remove cart
		 *    - Vehicle events, oh boy
		 *  - CartContainerCrasher
		 *    - VehicleBlockCollisionEvent -> into block inventory
		 *  - DeathPointCommand
		 *    - PlayerDeathEvent -> set
		 *  - Meteors
		 *  - PlayerListHeaderFooterWelcome
		 *  - IPCache
		 *    - Used for seen
		 *    - ServerListPingEvent -> replace "Player"
		 */

		// Feature: Allow color codes on signs via &
		getServer().getPluginManager().registerEvents(new ColorSignText(), this);

		// Feature: Send player their coordinates when they die.
		getServer().getPluginManager().registerEvents(new DeathCoordinates(), this);

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

	private void register(@NotNull EasterlynCore plugin) {
		plugin.getCommandManager().registerDependency(this.getClass(), this);

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

		plugin.registerCommands(getClassLoader(), "com.easterlyn.kitchensink.command");

		// Feature: Keep inventory when dying in PVP, drop a max of 30 exp.
		getServer().getPluginManager().registerEvents(new PVPKeepInventory(plugin), plugin);
	}

}
