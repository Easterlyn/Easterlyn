package com.easterlyn;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.PaperCommandManager;
import co.aikar.commands.RegisteredCommand;
import com.easterlyn.command.CommandRank;
import com.easterlyn.users.UserManager;
import com.easterlyn.users.UserRank;
import com.easterlyn.util.Colors;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.bukkit.command.SimpleCommandMap;
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
public class Easterlyn extends JavaPlugin {

	private UserManager userManager = new UserManager(this);
	private PaperCommandManager commandManager;
	private SimpleCommandMap simpleCommandMap;

	@Override
	public void onEnable() {
		saveDefaultConfig();

		getServer().getServicesManager().register(Easterlyn.class, this, this, ServicePriority.Normal);

		if (commandManager == null) {
			commandManager = new PaperCommandManager(this);
			//noinspection deprecation
			commandManager.enableUnstableAPI("help");
		}

		userManager.registerCommandContext(this);
		Colors.load(this);

		registerCommands("com.easterlyn.command");
	}

	public void registerCommands(String packageName) {
		new Reflections(packageName).getSubTypesOf(BaseCommand.class).stream()
				.filter(clazz -> !Modifier.isAbstract(clazz.getModifiers()))
				.forEach(clazz -> {
					Constructor<? extends BaseCommand> constructor;
					BaseCommand command;
					try {
						constructor = clazz.getConstructor(this.getClass());
						command = constructor.newInstance(this);
					} catch (ReflectiveOperationException e) {
						getLogger().severe("Unable to register command " + clazz.getName());
						e.printStackTrace();
						return;
					}
					commandManager.registerCommand(command);
					addPermissions(command.getDefaultRegisteredCommand());
					command.getRegisteredCommands().forEach(this::addPermissions);
				});
	}

	private void addPermissions(RegisteredCommand<?> command) {
		CommandRank commandRank = command.getAnnotation(CommandRank.class);
		UserRank rank = commandRank == null ? UserRank.MEMBER : commandRank.value();
		command.getRequiredPermissions().forEach(permission -> {
			PermissionUtil.addParent(permission, rank.getPermission());
			if (rank != UserRank.DANGER_DANGER_HIGH_VOLTAGE) {
				PermissionUtil.addParent(permission, "easterlyn.command.*");
			}
		});
	}

	@Override
	public void onDisable() {
		// TODO purge and save users
		commandManager.unregisterCommands();
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
			getLogger().severe("Could not fetch SimpleCommandMap from CraftServer, Easterlyn commands will fail to register.");
			getLogger().severe(StringUtil.getTrace(e));
		}
		return simpleCommandMap;
	}

}
