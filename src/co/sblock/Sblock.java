package co.sblock;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
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

import co.sblock.chat.SblockChat;
import co.sblock.data.SblockData;
import co.sblock.events.SblockEvents;
import co.sblock.machines.SblockMachines;
import co.sblock.module.Module;
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

		registerAllCommands();
	}

	/**
	 * @see org.bukkit.plugin.Plugin#onDisable()
	 */
	@Override
	public void onDisable() {
		HandlerList.unregisterAll(this);
		// Disable in reverse order - should better respect modules that require others to function
		Collections.reverse(modules);
		for (Module module : this.modules) {
			module.disable();
		}
		SblockData.getDB().disable();
		instance = null;
	}

	public void registerAllCommands() {
		try {
			Field field = cmdMap.getClass().getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> cmdMapMap = (HashMap<String, Command>) field.get(cmdMap);
			Reflections reflections = new Reflections("co.sblock.commands");
			Set<Class<? extends co.sblock.commands.SblockCommand>> commands = reflections.getSubTypesOf(co.sblock.commands.SblockCommand.class);
			for (Class<? extends co.sblock.commands.SblockCommand> command : commands) {
				try {
					co.sblock.commands.SblockCommand cmd = command.newInstance();
					if (cmdMapMap.containsKey(cmd.getName())) {
						Command overwritten = cmdMapMap.remove(cmd.getName());
						getLog().info("Overriding " + cmd.getName() + " by "
						+ (overwritten instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand) overwritten).getPlugin().getName() : "Vanilla/Spigot")
						+ ". Available aliases: " + overwritten.getAliases().toString());
						// TODO perhaps re-alias just in case
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

		// Dye 8 wool at a time just like stained glass, allows for re-dying
		for (DyeColor dye : DyeColor.values()) {
			shaped = new ShapedRecipe(new ItemStack(Material.WOOL, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.WOOL, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);
		}

		// Re-dye stained clay
		for (DyeColor dye : DyeColor.values()) {
			shaped = new ShapedRecipe(new ItemStack(Material.STAINED_CLAY, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.STAINED_CLAY, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
			getServer().addRecipe(shaped);
		}

		// Re-dye stained glass
		for (DyeColor dye : DyeColor.values()) {
			shaped = new ShapedRecipe(new ItemStack(Material.STAINED_GLASS, 8, dye.getWoolData()));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.STAINED_GLASS, Short.MAX_VALUE).setIngredient('Y', new MaterialData(Material.INK_SACK, dye.getDyeData()));
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
			file.createNewFile();
		}
		return file;
	}

	public static final Log getLog() {
		return logger;
	}
}
