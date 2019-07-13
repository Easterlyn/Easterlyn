package com.easterlyn;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import com.easterlyn.command.CommandExecutionContexts;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserManager;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.BlockUpdateManager;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.inventory.ItemUtil;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.GameMode;
import org.bukkit.Keyed;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

/**
 * The core plugin for <a href=easterlyn.com>Easterlyn</a>'s Minecraft server.
 *
 * @author Jikoo
 */
public class EasterlynCore extends JavaPlugin {

	private UserManager userManager = new UserManager(this);
	private PaperCommandManager commandManager;
	private SimpleCommandMap simpleCommandMap;
	private BlockUpdateManager blockUpdateManager = new BlockUpdateManager(this);
	private Multimap<Class<? extends Plugin>, BaseCommand> pluginCommands = HashMultimap.create();

	@Override
	public void onEnable() {
		saveDefaultConfig();

		getServer().getServicesManager().register(EasterlynCore.class, this, this, ServicePriority.Normal);

		if (commandManager == null) {
			commandManager = new PaperCommandManager(this);
			//noinspection deprecation
			commandManager.enableUnstableAPI("help");
			CommandExecutionContexts.register(this);
			// TODO system for Group resolvers
		}

		Colors.load(this);

		registerCommands(this, getClassLoader(), "com.easterlyn.command");

		PrepareItemCraftEvent.getHandlerList().register(new SimpleListener<>(PrepareItemCraftEvent.class, event -> {
			if (event.getRecipe() instanceof Keyed
					&& ((Keyed) event.getRecipe()).getKey().getKey().startsWith(ItemUtil.UNIQUE_KEYED_PREFIX)) {
				// Allow custom recipes using unique items
				return;
			}

			for (ItemStack itemStack : event.getInventory().getMatrix()) {
				if (ItemUtil.isUniqueItem(itemStack)) {
					event.getInventory().setResult(ItemUtil.AIR);
					return;
				}
			}
		}, this));

		CraftItemEvent.getHandlerList().register(new SimpleListener<>(CraftItemEvent.class, event -> {
			if (event.getWhoClicked().getGameMode() == GameMode.CREATIVE
					&& !event.getWhoClicked().hasPermission("easterlyn.events.creative.unfiltered")) {
				event.setCurrentItem(ItemUtil.cleanNBT(event.getCurrentItem()));
			}

			if (event.getRecipe() instanceof Keyed
					&& ((Keyed) event.getRecipe()).getKey().getKey().startsWith(ItemUtil.UNIQUE_KEYED_PREFIX)) {
				// Allow custom recipes using unique items
				return;
			}

			for (ItemStack itemStack : event.getInventory().getMatrix()) {
				if (ItemUtil.isUniqueItem(itemStack)) {
					event.getWhoClicked().sendMessage("events.craft.unique".replace("{ITEM}", ItemUtil.getItemName(itemStack)));
					event.setCancelled(true);
					return;
				}
			}
		}, this));

		// TODO uniques in anvils

		PluginDisableEvent.getHandlerList().register(new SimpleListener<>(PluginDisableEvent.class, event -> {
			if (pluginCommands.containsKey(event.getPlugin().getClass())) {
				pluginCommands.get(event.getPlugin().getClass()).forEach(commandManager::unregisterCommand);
			}
		}, this));

	}

	public void registerCommands(Plugin plugin, ClassLoader loader, String packageName) {
		commandManager.registerDependency(plugin.getClass(), plugin);
		new Reflections(packageName, loader).getSubTypesOf(BaseCommand.class).stream()
				.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
				.forEach(clazz -> {
					Constructor<? extends BaseCommand> constructor;
					BaseCommand command;
					try {
						constructor = clazz.getConstructor();
						command = constructor.newInstance();
					} catch (ReflectiveOperationException e) {
						getLogger().severe("Unable to register command " + clazz.getName());
						e.printStackTrace();
						return;
					}
					commandManager.registerCommand(command);
					CommandRank commandRank = clazz.getAnnotation(CommandRank.class);
					UserRank defaultRank = commandRank != null ? commandRank.value() : UserRank.MEMBER;
					command.getRegisteredCommands().forEach(registeredCommand -> addPermissions(defaultRank, registeredCommand));
					if (!this.equals(plugin)) {
						pluginCommands.put(plugin.getClass(), command);
					}
				});
	}

	private void addPermissions(UserRank defaultRank, RegisteredCommand<?> registeredCommand) {
		CommandRank commandRank = registeredCommand.getAnnotation(CommandRank.class);
		UserRank rank = commandRank != null ? commandRank.value() : defaultRank;
		registeredCommand.getRequiredPermissions().forEach(permission -> {
			if (permission == null || permission.isEmpty()) {
				return;
			}
			PermissionUtil.addParent(permission, rank.getPermission());
			if (rank != UserRank.DANGER_DANGER_HIGH_VOLTAGE) {
				PermissionUtil.addParent(permission, "easterlyn.command.*");
			}
		});
	}

	@Override
	public void onDisable() {
		userManager.clearCache();
		commandManager.unregisterCommands();
		blockUpdateManager.forceAllUpdates();
		simpleCommandMap = null;
	}

	@NotNull
	public PaperCommandManager getCommandManager() throws IllegalStateException {
		if (commandManager == null || !this.isEnabled()) {
			throw new IllegalStateException("Plugin not ready!");
		}

		return commandManager;
	}

	@NotNull
	public UserManager getUserManager() throws IllegalStateException {
		return userManager;
	}

	@NotNull
	public BlockUpdateManager getBlockUpdateManager() {
		return blockUpdateManager;
	}

	@Nullable
	public SimpleCommandMap getSimpleCommandMap() {
		if (simpleCommandMap != null) {
			return simpleCommandMap;
		}

		try {
			Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
			simpleCommandMap = (SimpleCommandMap) getCommandMap.invoke(getServer());
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException
				| NoSuchMethodException | InvocationTargetException e) {
			getLogger().severe("Could not fetch SimpleCommandMap from CraftServer, Easterlyn command will fail to register.");
			getLogger().severe(StringUtil.getTrace(e));
		}
		return simpleCommandMap;
	}

}
