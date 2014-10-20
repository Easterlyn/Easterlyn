package co.sblock;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.chat.SblockChat;
import co.sblock.data.SblockData;
import co.sblock.events.SblockEvents;
import co.sblock.machines.SblockMachines;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.CustomCommand;
import co.sblock.module.Module;
import co.sblock.module.SblockCommand;
import co.sblock.effects.SblockEffects;
import co.sblock.users.SblockUsers;
import co.sblock.utilities.Log;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.meteors.MeteorMod;
import co.sblock.utilities.rawmessages.RawAnnouncer;
import co.sblock.utilities.spectator.Spectators;

/**
 * Sblock is the base of Sblock.co's custom plugin. All features are handled by
 * smaller modules.
 * 
 * @author Jikoo, FireNG, Dublek
 */
public class Sblock extends JavaPlugin {

	/* Sblock's Log */
	private static final Log logger = Log.getLog("Sblock");

	/* The Sblock instance. */
	private static Sblock instance;

	/* The Set of Modules enabled. */
	private List<Module> modules;

	/* The Map of commands currently being managed and their respective Method. */
	private Map<String, Method> commandHandlers;

	/* The Map of registered CommandListeners. */
	private Map<Class<? extends CommandListener>, CommandListener> listenerInstances;

	/* A List of overridden commands. Allows their aliases to function. */
	private Map<PluginCommand, CommandExecutor> overriddenCommands;

	/* The CommandMap used to register commands for Modules. */
	private SimpleCommandMap cmdMap;

	/**
	 * Get the current instance of the Sblock plugin.
	 * 
	 * @return the Sblock instance
	 */
	public static Sblock getInstance() {
		return instance;
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onEnable()
	 */
	@Override
	public void onEnable() {
		if (Bukkit.getServer() instanceof org.bukkit.craftbukkit.v1_7_R4.CraftServer) {
			try {
				Field f = org.bukkit.craftbukkit.v1_7_R4.CraftServer.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				cmdMap = (SimpleCommandMap) f.get(Bukkit.getServer());
			} catch (IllegalArgumentException | IllegalAccessException
					| NoSuchFieldException | SecurityException e) {
				logger.criticalErr(e);
			}
		} else {
			getLog().severe("Invalid server version, Sblock commands will fail to register.");
		}
		instance = this;
		this.modules = new ArrayList<>();
		this.commandHandlers = new HashMap<>();
		this.listenerInstances = new HashMap<>();
		this.overriddenCommands = new HashMap<>();
		createRecipes();

		SblockData.getDB().enable();

		modules.add(new SblockChat().enable());
		modules.add(new SblockUsers().enable());
		modules.add(new SblockEvents().enable());
		modules.add(new SblockEffects().enable());
		modules.add(new SblockMachines().enable());
		modules.add(new Captcha().enable());
		modules.add(new MeteorMod().enable());
		modules.add(new RawAnnouncer().enable());
		modules.add(new Spectators().enable());
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		SblockData.getDB().enterFinalizeMode();
		this.unregisterAllCommands();
		HandlerList.unregisterAll(this);
		// Disable in reverse order - should better respect modules that require others to function
		Collections.reverse(modules);
		for (Module module : this.modules) {
			module.disable();
		}
		SblockData.getDB().disable();
		instance = null;
	}

	/**
	 * Register all commands for a CommandListener.
	 * 
	 * @param listener the CommandListener to register commands for
	 */
	public void registerCommands(CommandListener listener) {
		for (Method method : listener.getClass().getMethods()) {
			if (this.commandHandlers.containsKey(method)) {
				getLog().severe("Duplicate handlers for command " + method.getName() + " found in "
						+ this.commandHandlers.get(method.getName()).getDeclaringClass().getName()
						+ " and " + listener.getClass().getName());
			} else if (isValidCommand(method)) {
				this.commandHandlers.put(method.getName(), method);
				Command cmd = createCommand(method);
				if (cmd instanceof CustomCommand) {
					cmdMap.register(this.getDescription().getName(), createCommand(method));
				}
			}
		}
		this.listenerInstances.put(listener.getClass(), listener);
	}

	/**
	 * Verifies that a SblockCommand is properly formed.
	 * <p>
	 * A properly formed SblockCommand accepts a CommandSender and String[] as
	 * arguments and returns a boolean.
	 * 
	 * @param method the potential SblockCommand
	 * 
	 * @return true if the method is a SblockCommand
	 */
	private boolean isValidCommand(Method method) {
		if (method.getAnnotation(SblockCommand.class) == null) {
			// Method is not a SblockCommand, fail silently.
			return false;
		}

		if (method.getParameterTypes().length < 2
				|| !CommandSender.class.isAssignableFrom(method.getParameterTypes()[0])
				|| !String[].class.isAssignableFrom(method.getParameterTypes()[1])
				|| boolean.class != method.getGenericReturnType()) {

			logger.severe("Malformed SblockCommand: " + method.getName() + "\nExpected public boolean "
				 + method.getDeclaringClass().getName() + "." + method.getName()
				+ "(org.bukkit.command.CommandSender,java.lang.String[]) and recieved "
				+ method.toString());
			return false;
		}

		if (!method.getName().toLowerCase().equals(method.getName())) {
			logger.severe("Malformed SblockCommand: " + method.getDeclaringClass().getName() + "."
					+ method.getName() + "\nMethod name must be entirely lower case.");
		}
		return true;
	}

	/**
	 * Creates a CustomCommand that can be added to a CommandMap.
	 * 
	 * @param m the Method which will be used by the command
	 * 
	 * @return the CustomCommand created
	 */
	private Command createCommand(Method m) {
		Command cmd = getServer().getPluginCommand(m.getName());
		if (cmd != null && cmd.getName().equals(m.getName())) {
			// Command has been registered by another plugin.
			getLog().info("Overriding /" + m.getName() + " by "
					+ ((PluginCommand) cmd).getExecutor().getClass().getName()
					+ ". The original is available through " + cmd.getAliases().toString());
			this.overriddenCommands.put((PluginCommand) cmd, ((PluginCommand) cmd).getExecutor());
			((PluginCommand) cmd).setExecutor(this);
		} else {
			cmd = new CustomCommand(m.getName());
		}
		String s;
		if (m.getAnnotation(CommandDescription.class) != null) {
			s = ChatColor.YELLOW + ChatColor.translateAlternateColorCodes('&', m.getAnnotation(CommandDescription.class).value());
		} else {
			s = ChatColor.YELLOW + "A Sblock command.";
		}
		cmd.setDescription(s);
		if (m.getAnnotation(CommandUsage.class) != null) {
			s = ChatColor.RED + ChatColor.translateAlternateColorCodes('&', m.getAnnotation(CommandUsage.class).value());
		} else {
			s = ChatColor.RED + "/<command>";
		}
		cmd.setUsage(s);
		if (m.getAnnotation(CommandPermission.class) != null) {
			s = m.getAnnotation(CommandPermission.class).value();
		} else {
			s = null;
		}
		cmd.setPermission(s);
		if (m.getAnnotation(CommandDenial.class) != null) {
			s = ChatColor.translateAlternateColorCodes('&', m.getAnnotation(CommandDenial.class).value());
		} else {
			s = ChatColor.RED + "By the order of the Jarl, stop right there!";
		}
		cmd.setPermissionMessage(s);
		return cmd;
	}

	/**
	 * Creates generic crafting recipies allowed by Sblock.
	 * <p>
	 * Module-dependent recipes such as CaptchaCards should be registered in
	 * {@link Module#onEnable()}.
	 */
	@SuppressWarnings("deprecation")
	public void createRecipes() {
		// BoonConomy: 1 emerald -> 9 lapis block
		ShapelessRecipe toLapis = new ShapelessRecipe(new ItemStack(Material.LAPIS_BLOCK, 9));
		toLapis.addIngredient(Material.EMERALD);
		getServer().addRecipe(toLapis);

		// BoonConomy: 9 lapis block -> 1 emerald
		ShapedRecipe shaped = new ShapedRecipe(new ItemStack(Material.EMERALD));
		shaped.shape("XXX", "XXX", "XXX");
		shaped.setIngredient('X', Material.LAPIS_BLOCK);
		getServer().addRecipe(shaped);

		// General: Packed ice = 2 snow 2 ice
		shaped = new ShapedRecipe(new ItemStack(Material.PACKED_ICE));
		shaped.shape("XY", "YX");
		shaped.setIngredient('X', Material.SNOW_BLOCK);
		shaped.setIngredient('Y', Material.ICE);
		getServer().addRecipe(shaped);
		shaped.shape("YX", "XY");
		getServer().addRecipe(shaped);

		// General: 8 gravel, 1 bucket water -> 4 clay
		shaped.shape("XXX", "XYX", "XXX").setIngredient('X', Material.GRAVEL).setIngredient('Y', Material.WATER_BUCKET);
		getServer().addRecipe(shaped);

		// Smelting: Revert armor to crafting material, 1 coal if durability% too low
		// Deprecated constructor required to ignore item durability
		FurnaceRecipe furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_BOOTS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_CHESTPLATE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_HELMET, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_HOE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_LEGGINGS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_PICKAXE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_SPADE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.DIAMOND_SWORD, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_AXE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_BOOTS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_CHESTPLATE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_HELMET, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_HOE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_LEGGINGS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_PICKAXE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_SPADE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.GOLD_SWORD, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_AXE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_BOOTS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_CHESTPLATE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_HELMET, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_HOE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_LEGGINGS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_PICKAXE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_SPADE, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.IRON_SWORD, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
		furnace.setInput(Material.SHEARS, Short.MAX_VALUE);
		getServer().addRecipe(furnace);
	}

	/**
	 * Unregister all registered commands.
	 */
	public void unregisterAllCommands() {
		for (Method m : this.commandHandlers.values()) {
			Command cmd = cmdMap.getCommand(m.getName());
			if (cmd == null) {
				continue;
			}
			if (!overriddenCommands.containsKey(cmd)) {
				cmd.unregister(cmdMap);
			} else {
				((PluginCommand) cmd).setExecutor(overriddenCommands.remove(cmd));
			}
		}
	}

	/**
	 * Gets the CommandMap containing all registered commands.
	 * 
	 * @return
	 */
	public SimpleCommandMap getCommandMap() {
		return cmdMap;
	}

	/**
	 * Passes all registered commands to the CommandListener that registered
	 * them.
	 * 
	 * @see org.bukkit.command.CommandExecutor#onCommand(CommandSender, Command,
	 *      String, String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!this.commandHandlers.containsKey(label)) {
			if (this.overriddenCommands.containsKey(command)) {
				if (this.overriddenCommands.get(command).onCommand(sender, command, label, args)) {
					return true;
				}
				// Fall through to Sblock - most plugins return true with custom permission denial messages, etc.
			}
		}
		if (!this.commandHandlers.containsKey(command.getName())) {
			this.getLogger().warning( "Command /" + command.getName() + " has no associated handler.");
			sender.sendMessage(ChatColor.RED
					+ "An internal error has occurred. Please notify a member of staff of this issue as soon as possible.");
			return true;
		}
		Method handlerMethod = this.commandHandlers.get(command.getName());
		if (sender instanceof ConsoleCommandSender
				&& !handlerMethod.getAnnotation(SblockCommand.class).consoleFriendly()) {
			sender.sendMessage("This command cannot be issued from the console.");
			return true;
		}
		if (!command.testPermission(sender)) {
			return true;
		}
		try {
			if (!(Boolean) handlerMethod.invoke(this.listenerInstances
					.get(handlerMethod.getDeclaringClass()), sender, args)) {
				sender.sendMessage(command.getUsage());
			}
			return true;
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			logger.err(e);
		}
		return false;
	}

	public static final Log getLog() {
		return logger;
	}
}
