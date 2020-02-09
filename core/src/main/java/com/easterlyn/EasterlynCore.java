package com.easterlyn;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreCompletions;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.listener.UniqueListener;
import com.easterlyn.user.UserManager;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.BlockUpdateManager;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.server.PluginDisableEvent;
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

	/*
	 * TODO
	 *  - System for rich(er) messages
	 *  - Extract command feedback out of commands to lang files
	 *  - Generic useful command conditions
	 */
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
			CoreContexts.register(this);
			CoreCompletions.register(this);
			// TODO system for Group resolvers
		}

		Colors.load(this);

		registerCommands(this, getClassLoader(), "com.easterlyn.command");

		// Listener for preventing ruining unique items
		getServer().getPluginManager().registerEvents(new UniqueListener(), this);

		PluginDisableEvent.getHandlerList().register(new SimpleListener<>(PluginDisableEvent.class, event -> {
			if (pluginCommands.containsKey(event.getPlugin().getClass())) {
				pluginCommands.get(event.getPlugin().getClass()).forEach(commandManager::unregisterCommand);
			}
		}, this));

	}

	public void registerCommands(Plugin plugin, ClassLoader loader, String packageName) {
		if (!plugin.equals(this)) {
			commandManager.registerDependency(plugin.getClass(), plugin);
		}
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
					commandManager.registerCommand(command, true);
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
