package co.sblock.Sblock;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.Chat.SblockChat;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.SblockEffects.SblockEffects;
import co.sblock.Sblock.UserData.SblockUsers;
import co.sblock.Sblock.Utilities.Log;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Counter.CounterModule;
import co.sblock.Sblock.Utilities.MeteorMod.MeteorMod;
import co.sblock.Sblock.Utilities.RawMessages.RawAnnouncer;
import co.sblock.Sblock.Utilities.Spectator.Spectators;

/**
 * Sblock is the base of Sblock.co's custom plugin. All features are handled by
 * smaller modules.
 * 
 * @author Jikoo, FireNG, Dublek
 */
public class Sblock extends JavaPlugin implements CommandListener {

	/** Sblock's Log */
	private static final Log logger = Log.getLog("Sblock");

	/** The Sblock instance. */
	private static Sblock instance;

	/** The Set of Modules enabled. */
	private Set<Module> modules;

	/** The Map of commands currently being managed and their respective Method. */
	private Map<String, Method> commandHandlers;

	/** The Map of registered CommandListeners. */
	private Map<Class<? extends CommandListener>, CommandListener> listenerInstances;

	/** The CommandMap used to register commands for Modules. */
	private CommandMap cmdMap;

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
		if (Bukkit.getServer() instanceof org.bukkit.craftbukkit.v1_7_R2.CraftServer) {
			try {
				Field f = org.bukkit.craftbukkit.v1_7_R2.CraftServer.class.getDeclaredField("commandMap");
				f.setAccessible(true);
				cmdMap = (CommandMap) f.get(Bukkit.getServer());
			} catch (IllegalArgumentException | IllegalAccessException
					| NoSuchFieldException | SecurityException e) {
				logger.criticalErr(e);
			}
		} else {
			getLog().severe("Invalid server version, Sblock commands will fail to register.");
		}
		instance = this;
		this.modules = new HashSet<Module>();
		this.commandHandlers = new HashMap<String, Method>();
		this.listenerInstances = new HashMap<Class<? extends CommandListener>, CommandListener>();
		saveDefaultConfig();
		createRecipes();

		SblockData.getDB().enable();
		
		modules.add(new SblockChat().enable());
		modules.add(new SblockUsers().enable());
		modules.add(new SblockEvents().enable());
		modules.add(new SblockEffects().enable());
		modules.add(new SblockMachines().enable());
		modules.add(new Captcha().enable());
		modules.add(new CounterModule().enable());
		modules.add(new MeteorMod().enable());
		modules.add(new RawAnnouncer().enable());
		modules.add(new Spectators().enable());
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		this.unregisterAllCommands();
		HandlerList.unregisterAll(this);
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

			logger.severe("Malformed SblockCommand: " + method.getName() + ". Expected public boolean "
				 + method.getDeclaringClass().getName() + "." + method.getName()
				+ "(org.bukkit.command.CommandSender,java.lang.String[]) and recieved "
				+ method.toString());
			return false;
		}

		// TODO check if name must be lower case despite most recent onCommand changes
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
			getLog().info("Overriding control of /" + m.getName() + " by "
					+ ((PluginCommand) cmd).getExecutor().getClass().getName());
			((PluginCommand) cmd).setExecutor(this);
		} else {
			cmd = new CustomCommand(m.getName());
		}
		cmd.setDescription(m.getAnnotation(SblockCommand.class).description());
		cmd.setUsage(m.getAnnotation(SblockCommand.class).usage());
		cmd.setPermission(m.getAnnotation(SblockCommand.class).permission());
		cmd.setPermissionMessage(ChatMsgs.permissionDenied());
		return cmd;
	}

	/**
	 * Creates generic crafting recipies allowed by Sblock.
	 * <p>
	 * Module-dependent recipes such as CaptchaCards should be registered in
	 * {@link Module#onEnable()}.
	 */
	public void createRecipes() {
		// BoonConomy: 1 emerald -> 9 lapis block
		ShapelessRecipe toLapis = new ShapelessRecipe(new ItemStack(Material.LAPIS_BLOCK, 9));
		toLapis.addIngredient(Material.EMERALD);
		//getServer().addRecipe(toLapis);

		// BoonConomy: 9 lapis block -> 1 emerald
		ShapedRecipe shaped = new ShapedRecipe(new ItemStack(Material.EMERALD));
		shaped.shape("XXX", "XXX", "XXX");
		shaped.setIngredient('X', Material.LAPIS_BLOCK);
		//getServer().addRecipe(shaped);

		// General: Packed ice = 2 snow 2 ice
		shaped = new ShapedRecipe(new ItemStack(Material.PACKED_ICE));
		shaped.shape("XY", "YX");
		shaped.setIngredient('X', Material.SNOW_BLOCK);
		shaped.setIngredient('Y', Material.ICE);
		getServer().addRecipe(shaped);
		shaped.shape("YX", "XY");
		getServer().addRecipe(shaped);
	}

	/**
	 * Unregister all registered commands.
	 */
	public void unregisterAllCommands() {
		for (Method m : this.commandHandlers.values()) {
			try {
				Bukkit.getPluginCommand(m.getName()).setExecutor(null);
			} catch (NullPointerException e) {
				getLog().fine("Command " + m.getName() + " was registered interally.");
			}
		}
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
			this.getLogger().warning( "Command /" + label + " has no associated handler.");
			sender.sendMessage(ChatColor.RED
					+ "An internal error has occurred. Please notify a member of staff of this issue as soon as possible.");
			return true;
		} else {
			Method handlerMethod = this.commandHandlers.get(command.getName());
			if (sender instanceof ConsoleCommandSender
					&& !handlerMethod.getAnnotation(SblockCommand.class).consoleFriendly()) {
				sender.sendMessage("This command cannot be issued from the console.");
				return true;
			}
			if (!sender.hasPermission(command.getPermission())) {
				sender.sendMessage(command.getPermissionMessage());
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
		}
		return false;
	}

	public static final Log getLog() {
		return logger;
	}
}
