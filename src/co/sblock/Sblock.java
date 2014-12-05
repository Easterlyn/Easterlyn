package co.sblock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.MaterialData;
import org.bukkit.plugin.java.JavaPlugin;
import org.reflections.Reflections;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Chat;
import co.sblock.commands.SblockCommand;
import co.sblock.events.Events;
import co.sblock.machines.Machines;
import co.sblock.module.Module;
import co.sblock.users.Users;
import co.sblock.fx.FXManager;
import co.sblock.utilities.Log;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.meteors.Meteors;
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
		if (Bukkit.getServer() instanceof org.bukkit.craftbukkit.v1_8_R1.CraftServer) {
			try {
				Field f = org.bukkit.craftbukkit.v1_8_R1.CraftServer.class.getDeclaredField("commandMap");
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
		modules.add(new Chat().enable());
		modules.add(new Users().enable());
		modules.add(new Events().enable());
		modules.add(new FXManager().enable());
		modules.add(new Machines().enable());
		modules.add(new Captcha().enable());
		modules.add(new Meteors().enable());
		modules.add(new RawAnnouncer().enable());
		modules.add(new Spectators().enable());

		createRecipes();
		registerAllCommands();
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		unregisterAllCommands();
		HandlerList.unregisterAll(this);
		// Disable in reverse order - should better respect modules that require others to function
		Collections.reverse(modules);
		for (Module module : this.modules) {
			module.disable();
		}
		instance = null;
	}

	public void registerAllCommands() {
		try {
			// TODO wrap /version and override tab completion
			Field field = cmdMap.getClass().getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> cmdMapKnownCommands = (HashMap<String, Command>) field.get(cmdMap);
			Reflections reflections = new Reflections("co.sblock.commands");
			Set<Class<? extends SblockCommand>> commands = reflections.getSubTypesOf(SblockCommand.class);
			for (Class<? extends SblockCommand> command : commands) {
				try {
					SblockCommand cmd = command.newInstance();
					if (cmdMapKnownCommands.containsKey(cmd.getName())) {
						Command overwritten = cmdMapKnownCommands.remove(cmd.getName());
						getLog().info("Overriding " + cmd.getName() + " by "
						+ (overwritten instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand) overwritten).getPlugin().getName() : "Vanilla/Spigot")
						+ ". Available aliases: " + overwritten.getAliases().toString());
					}
					cmdMap.register(this.getDescription().getName(), cmd);
				} catch (InstantiationException | IllegalAccessException e) {
					getLog().severe("Unable to register command " + command.getName());
					e.printStackTrace();
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			getLog().severe("Unable to modify SimpleCommandMap.knownCommands! No commands will be registered!");
			e.printStackTrace();
		}
	}

	private void unregisterAllCommands() {
		try {
			Field field = cmdMap.getClass().getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> cmdMapKnownCommands = (HashMap<String, Command>) field.get(cmdMap);
			for (Iterator<Map.Entry<String, Command>> iterator = cmdMapKnownCommands.entrySet().iterator(); iterator.hasNext();) {
				Map.Entry<String, Command> entry = iterator.next();
				if (entry.getValue() instanceof SblockCommand) {
					iterator.remove();
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			getLog().severe("Unable to modify SimpleCommandMap.knownCommands! Commands cannot be unregistered!");
			e.printStackTrace();
		}
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

		for (DyeColor dye : DyeColor.values()) {
			// Dye 8 wool at a time just like stained glass, allows for re-dyeing
			shaped = new ShapedRecipe(new ItemStack(Material.WOOL, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.WOOL, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);

			// Re-dye stained clay
			shaped = new ShapedRecipe(new ItemStack(Material.STAINED_CLAY, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.STAINED_CLAY, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);

			// Re-dye stained glass
			shaped = new ShapedRecipe(new ItemStack(Material.STAINED_GLASS, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.STAINED_GLASS, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);

			// Allow dyeing of panes and re-dyeing of stained panes
			shaped = new ShapedRecipe(new ItemStack(Material.STAINED_GLASS_PANE, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.THIN_GLASS).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);

			shaped = new ShapedRecipe(new ItemStack(Material.STAINED_GLASS_PANE, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.STAINED_GLASS_PANE, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);
		}

		// General: Packed ice = 2 snow 2 ice
		shaped = new ShapedRecipe(new ItemStack(Material.PACKED_ICE));
		shaped.shape("XY", "YX");
		shaped.setIngredient('X', Material.SNOW_BLOCK);
		shaped.setIngredient('Y', Material.ICE);
		getServer().addRecipe(shaped);
		shaped.shape("YX", "XY");
		getServer().addRecipe(shaped);

		// General: 8 gravel, 1 bucket water -> 4 clay
		shaped = new ShapedRecipe(new ItemStack(Material.CLAY, 4));
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
	 * Gets the CommandMap containing all registered commands.
	 * 
	 * @return
	 */
	public SimpleCommandMap getCommandMap() {
		return cmdMap;
	}

	/**
	 * Gets a Set of the names of all registered commands.
	 */
	public List<String> getAllCommandAliases() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		Field field = cmdMap.getClass().getDeclaredField("knownCommands");
		field.setAccessible(true);
		@SuppressWarnings("unchecked")
		HashMap<String, Command> cmdMapKnownCommands = (HashMap<String, Command>) field.get(cmdMap);
		return ImmutableList.copyOf(cmdMapKnownCommands.keySet());
	}

	/**
	 * Executes the given command.
	 * 
	 * @see org.bukkit.command.CommandExecutor#onCommand(CommandSender, Command, String, String[])
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return command.execute(sender, label, args);
	}

	public File getUserDataFolder() throws IOException {
		File file = new File(getDataFolder(), "UserData");
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static final Log getLog() {
		return logger;
	}
}
