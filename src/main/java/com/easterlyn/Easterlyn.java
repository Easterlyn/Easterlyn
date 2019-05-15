package com.easterlyn;

import com.easterlyn.captcha.Captcha;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.commands.EasterlynCommandAlias;
import com.easterlyn.discord.Discord;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.Events;
import com.easterlyn.machines.Machines;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.micromodules.FreeCart;
import com.easterlyn.micromodules.Holograms;
import com.easterlyn.micromodules.Meteors;
import com.easterlyn.micromodules.ParticleUtils;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.RawAnnouncer;
import com.easterlyn.micromodules.SleepVote;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.micromodules.VillagerAdjustment;
import com.easterlyn.module.Dependencies;
import com.easterlyn.module.Dependency;
import com.easterlyn.module.Module;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.player.PermissionUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Easterlyn is the base of easterlyn.com's custom plugin. All features are handled by
 * smaller modules.
 *
 * @author Jikoo, FireNG, Dublek
 */
public class Easterlyn extends JavaPlugin {

	/* The Modules enabled. */
	private Map<Class<?>, Module> modules;

	/* A reference to Bukkit's internal CommandMap. */
	private SimpleCommandMap cmdMap;

	@Override
	public void onEnable() {
		try {
			Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
			cmdMap = (SimpleCommandMap) getCommandMap.invoke(getServer());
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException
				| NoSuchMethodException | InvocationTargetException e) {
			getLogger().severe("Could not fetch SimpleCommandMap from CraftServer, Easterlyn commands will fail to register.");
			getLogger().severe(TextUtils.getTrace(e));
		}

		createBasePermissions();

		modules = new LinkedHashMap<>();

		// Critical base modules
		addModule(new Language(this));
		addModule(new Cooldowns(this));
		addModule(new Chat(this));
		addModule(new Users(this));

		// Non-critical chat-based modules
		addModule(new Discord(this));
		addModule(new RawAnnouncer(this));
		addModule(new AwayFromKeyboard(this));

		addModule(new Effects(this));
		addModule(new Captcha(this));
		addModule(new Holograms(this));
		addModule(new ParticleUtils(this));
		addModule(new Protections(this));

		// Machines depends on Captcha, Effects, Holograms, ParticleUtils, Protections, and Users to construct.
		addModule(new Machines(this));

		// Misc. event-driven modules
		addModule(new FreeCart(this));
		addModule(new Meteors(this));
		addModule(new SleepVote(this));
		addModule(new Spectators(this));
		addModule(new VillagerAdjustment(this));

		addModule(new Events(this));

		List<String> disabledModules = getConfig().getStringList("disabled-modules");
		for (Module module : modules.values()) {
			if (module.isRequired() || !disabledModules.contains(module.getName())) {
				module.enable();
			}
		}

		final HashMap<String, Command> cmdMapKnownCommands;
		try {
			Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> map = (HashMap<String, Command>) field.get(cmdMap);
			// For some reason, the compiler just hates directly doing this.
			cmdMapKnownCommands = map;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			getLogger().severe("Unable to modify SimpleCommandMap.knownCommands! No commands will be registered!");
			getLogger().severe(TextUtils.getTrace(e));
			return;
		}

		Reflections reflections = new Reflections("com.easterlyn.commands");
		Map<Object, List<Class<? extends EasterlynCommand>>> commands = reflections.getSubTypesOf(EasterlynCommand.class).stream().collect(Collectors.groupingBy(clazz -> {
			if (Modifier.isAbstract(clazz.getModifiers())) {
				return "abstract";
			}
			if (EasterlynCommandAlias.class.isAssignableFrom(clazz)) {
				return "alias";
			}
			return "command";
		}));

		// Set up basic commands. These only depend on our own internals.
		registerAllCommands(cmdMapKnownCommands, commands.get("command"));

		/*
		 * Set up aliases with custom behavior after all other plugins. This is done to guarantee
		 * that all commands and their aliases are completely registered.
		 */
		new BukkitRunnable() {
			@Override
			public void run() {
				registerAllCommands(cmdMapKnownCommands, commands.get("alias"));
			}
		}.runTaskLater(this, 1L);
	}

	private void addModule(Module module) {
		modules.put(module.getClass(), module);
	}

	private void createBasePermissions() {
		UserRank previousRank = null;
		for (UserRank rank : UserRank.values()) {
			Permission permission = PermissionUtils.getOrCreate(rank.getPermission(), rank.getPermissionDefault());
			PermissionUtils.getOrCreate("easterlyn.chat.color." + rank.getLowercaseName(), rank.getPermissionDefault()).addParent(permission, true);
			if (previousRank != null) {
				Permission child = PermissionUtils.getOrCreate(previousRank.getPermission(), previousRank.getPermissionDefault());
				child.addParent(permission, true);
			} else {
				// Permission#addParent calls recalculatePermissibles
				permission.recalculatePermissibles();
			}
			previousRank = rank;
		}
	}

	private void registerAllCommands(HashMap<String, Command> knownCommands, List<Class<? extends EasterlynCommand>> commands) {
		if (commands == null) {
			return;
		}
		for (Class<? extends EasterlynCommand> command : commands) {
			if (areDependenciesMissing(command)) {
				getLogger().warning(command.getSimpleName() + " is missing dependencies, skipping.");
				continue;
			}
			try {
				Constructor<? extends EasterlynCommand> constructor = command.getConstructor(this.getClass());
				EasterlynCommand cmd = constructor.newInstance(this);
				if (knownCommands.containsKey(cmd.getName())) {
					Command overwritten = knownCommands.remove(cmd.getName());
					getLogger().info("Overriding " + cmd.getName() + " by "
					+ (overwritten instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand) overwritten).getPlugin().getName() : "Vanilla/Spigot")
					+ ". Aliases: " + overwritten.getAliases().toString());
				}
				for (String alias : cmd.getAliases()) {
					if (knownCommands.containsKey(alias)) {
						Command overwritten = knownCommands.remove(alias);
						getLogger().info("Overriding " + alias + " by "
						+ (overwritten instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand) overwritten).getPlugin().getName() : "Vanilla/Spigot")
						+ ". Aliases: " + overwritten.getAliases().toString());
					}
				}
				cmdMap.register(this.getDescription().getName(), cmd);
				String permissionString = cmd.getPermission();
				if (permissionString == null) {
					continue;
				}
				Permission permission = new Permission(permissionString);
				if (cmd.getPermissionLevel() != UserRank.DANGER_DANGER_HIGH_VOLTAGE) {
					permission.addParent("easterlyn.command.*", true).recalculatePermissibles();
				}
				permission.addParent(cmd.getPermissionLevel().getPermission(), true).recalculatePermissibles();
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
					| SecurityException | IllegalArgumentException | InvocationTargetException e) {
				getLogger().severe("Unable to register command " + command.getName());
				getLogger().severe(TextUtils.getTrace(e));
			}
		}
	}

	@Override
	public void onDisable() {
		unregisterAllCommands();
		HandlerList.unregisterAll(this);
		// Disable in reverse order - should better respect modules that require others to function
		ArrayList<Module> moduleList = new ArrayList<>(modules.values());
		ListIterator<Module> iterator = moduleList.listIterator(moduleList.size());
		while (iterator.hasPrevious()) {
			iterator.previous().disable();
		}
	}

	private void unregisterAllCommands() {
		try {
			Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> cmdMapKnownCommands = (HashMap<String, Command>) field.get(cmdMap);
			cmdMapKnownCommands.entrySet().removeIf(entry -> entry.getValue() instanceof EasterlynCommand);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			getLogger().severe("Unable to modify SimpleCommandMap.knownCommands! Commands cannot be unregistered!");
			getLogger().severe(TextUtils.getTrace(e));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getModule(Class<T> clazz) {
		Preconditions.checkArgument(Module.class.isAssignableFrom(clazz), "That's not a Module. Are you even trying?");
		Preconditions.checkArgument(modules.containsKey(clazz), "Module not enabled!");
		Object object = modules.get(clazz);
		Preconditions.checkArgument(clazz.isAssignableFrom(object.getClass()));
		return (T) object;
	}

	/**
	 * Gets the CommandMap containing all registered commands.
	 */
	public SimpleCommandMap getCommandMap() {
		return cmdMap;
	}

	/**
	 * Gets a Set of the names of all registered commands.
	 */
	public List<String> getAllCommandAliases() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = SimpleCommandMap.class.getDeclaredField("knownCommands");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		HashMap<String, Command> cmdMapKnownCommands = (HashMap<String, Command>) field.get(cmdMap);
		return ImmutableList.copyOf(cmdMapKnownCommands.keySet());
	}

	public GameProfile getFakeGameProfile(String name) {
		String uuidString = getConfig().getString("uuid." + name);
		UUID uuid;
		if (uuidString != null) {
			uuid = UUID.fromString(uuidString);
		} else {
			uuid = UUID.randomUUID();
			getConfig().set("uuid." + name, uuid.toString());
			saveConfig();
		}
		return new GameProfile(uuid, name);
	}

	public static <T> boolean areDependenciesMissing(Class<T> clazz) {
		if (clazz.isAnnotationPresent(Dependencies.class)) {
			for (Dependency dependency : clazz.getAnnotation(Dependencies.class).value()) {
				String pluginName = dependency.value();
				if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
					Logger.getLogger("Easterlyn").severe("Dependency " + pluginName + " is not enabled!");
					return true;
				}
			}
		} else if (clazz.isAnnotationPresent(Dependency.class)) {
			String pluginName = clazz.getAnnotation(Dependency.class).value();
			if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
				Logger.getLogger("Easterlyn").severe("Dependency " + pluginName + " is not enabled!");
				return true;
			}
		}
		return false;
	}

}
