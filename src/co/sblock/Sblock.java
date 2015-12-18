package co.sblock;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.apache.commons.lang3.Validate;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.material.Dye;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.java.JavaPlugin;

import org.reflections.Reflections;

import com.google.common.collect.ImmutableList;

import com.mojang.authlib.GameProfile;

import co.sblock.captcha.Captcha;
import co.sblock.chat.Chat;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;
import co.sblock.effects.Effects;
import co.sblock.events.Events;
import co.sblock.machines.Machines;
import co.sblock.micromodules.AwayFromKeyboard;
import co.sblock.micromodules.Cooldowns;
import co.sblock.micromodules.FreeCart;
import co.sblock.micromodules.Godule;
import co.sblock.micromodules.Holograms;
import co.sblock.micromodules.Meteors;
import co.sblock.micromodules.ParticleUtils;
import co.sblock.micromodules.Protections;
import co.sblock.micromodules.RawAnnouncer;
import co.sblock.micromodules.SleepVote;
import co.sblock.micromodules.Spectators;
import co.sblock.module.Dependencies;
import co.sblock.module.Dependency;
import co.sblock.module.Module;
import co.sblock.users.Users;
import co.sblock.utilities.RegexUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Sblock is the base of Sblock.co's custom plugin. All features are handled by
 * smaller modules.
 * 
 * @author Jikoo, FireNG, Dublek
 */
public class Sblock extends JavaPlugin {

	/* The Modules enabled. */
	private Map<Class<?>, Module> modules;

	/* A reference to Bukkit's internal CommandMap. */
	private SimpleCommandMap cmdMap;

	/* A Random for use in all modules. */
	private final Random random = new Random();

	@Override
	public void onEnable() {
		try {
			Method getCommandMap = getServer().getClass().getMethod("getCommandMap");
			cmdMap = (SimpleCommandMap) getCommandMap.invoke(getServer());
		} catch (IllegalArgumentException | IllegalAccessException | SecurityException
				| NoSuchMethodException | InvocationTargetException e) {
			getLogger().severe("Could not fetch SimpleCommandMap from CraftServer, Sblock commands will fail to register.");
			getLogger().severe(RegexUtils.getTrace(e));
		}

		createBasePermissions();

		modules = new LinkedHashMap<>();

		addModule(new Cooldowns(this));
		addModule(new Discord(this));
		addModule(new Chat(this));
		addModule(new RawAnnouncer(this));
		addModule(new AwayFromKeyboard(this));

		addModule(new Users(this));

		addModule(new ParticleUtils(this));
		addModule(new FreeCart(this));

		addModule(new Effects(this));
		addModule(new Captcha(this));
		addModule(new Holograms(this));
		addModule(new Protections(this));
		// Machines depends on Captcha, Effects, Holograms, Protections, and Users to construct
		addModule(new Machines(this));


		addModule(new Meteors(this));
		addModule(new SleepVote(this));
		addModule(new Spectators(this));
		addModule(new Godule(this));

		addModule(new Events(this));

		for (Module module : modules.values()) {
			module.enable();
		}

		createRecipes();
		registerAllCommands();
	}

	private void addModule(Module module) {
		modules.put(module.getClass(), module);
	}

	private void createBasePermissions() {
		Permission permission;
		try {
			permission = new Permission("sblock.default", PermissionDefault.TRUE);
			getServer().getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			getServer().getPluginManager().getPermission("sblock.default").setDefault(PermissionDefault.TRUE);
		}
		for (String perm : new String[] { "hero", "godtier", "donator", "helper", "felt", "denizen", "horrorterror" }) {
			try {
				permission = new Permission("sblock." + perm, PermissionDefault.OP);
				getServer().getPluginManager().addPermission(permission);
			} catch (IllegalArgumentException e) {
				getServer().getPluginManager().getPermission("sblock." + perm).setDefault(PermissionDefault.OP);
			}
		}
		for (ChatColor color : ChatColor.values()) {
			try {
				permission = new Permission("sblockchat." + color.name().toLowerCase(), PermissionDefault.FALSE);
				getServer().getPluginManager().addPermission(permission);
			} catch (IllegalArgumentException e) {
				getServer().getPluginManager().getPermission("sblockchat." + color.name().toLowerCase())
						.setDefault(PermissionDefault.FALSE);
			}
		}
	}

	private void registerAllCommands() {
		HashMap<String, Command> cmdMapKnownCommands;
		try {
			Field field = cmdMap.getClass().getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> map = (HashMap<String, Command>) field.get(cmdMap);
			// For some reason, the compiler just hates directly doing this.
			cmdMapKnownCommands = map;
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			getLogger().severe("Unable to modify SimpleCommandMap.knownCommands! No commands will be registered!");
			e.printStackTrace();
			return;
		}
		Reflections reflections = new Reflections("co.sblock.commands");
		Set<Class<? extends SblockCommand>> commands = reflections.getSubTypesOf(SblockCommand.class);
		for (Class<? extends SblockCommand> command : commands) {
			if (Modifier.isAbstract(command.getModifiers())) {
				continue;
			}
			if (!areDependenciesPresent(command)) {
				getLogger().warning(command.getSimpleName() + " is missing dependencies, skipping.");
				continue;
			}
			try {
				Constructor<? extends SblockCommand> constructor = command.getConstructor(this.getClass());
				SblockCommand cmd = constructor.newInstance(this);
				if (cmdMapKnownCommands.containsKey(cmd.getName())) {
					Command overwritten = cmdMapKnownCommands.remove(cmd.getName());
					getLogger().info("Overriding " + cmd.getName() + " by "
					+ (overwritten instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand) overwritten).getPlugin().getName() : "Vanilla/Spigot")
					+ ". Aliases: " + overwritten.getAliases().toString());
				}
				for (String alias : cmd.getAliases()) {
					if (cmdMapKnownCommands.containsKey(alias)) {
						Command overwritten = cmdMapKnownCommands.remove(alias);
						getLogger().info("Overriding " + alias + " by "
						+ (overwritten instanceof PluginIdentifiableCommand ? ((PluginIdentifiableCommand) overwritten).getPlugin().getName() : "Vanilla/Spigot")
						+ ". Aliases: " + overwritten.getAliases().toString());
					}
				}
				cmdMap.register(this.getDescription().getName(), cmd);
				Permission permission = new Permission(cmd.getPermission());
				permission.addParent(cmd.getPermissionLevel(), true).recalculatePermissibles();
				permission.addParent("sblock.command.*", true).recalculatePermissibles();
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
					| SecurityException | IllegalArgumentException | InvocationTargetException e) {
				getLogger().severe("Unable to register command " + command.getName());
				getLogger().severe(RegexUtils.getTrace(e));
			}
		}
	}

	/**
	 * Creates generic crafting recipies allowed by Sblock.
	 * <p>
	 * Module-dependent recipes such as CaptchaCards should be registered in
	 * {@link Module#onEnable()}.
	 */
	@SuppressWarnings("deprecation")
	private void createRecipes() {
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
			Dye dyeMaterial = new Dye(Material.INK_SACK);
			dyeMaterial.setColor(dye);

			for (Material material : new Material[] { Material.CARPET, Material.STAINED_CLAY,
					Material.STAINED_GLASS, Material.STAINED_GLASS_PANE, Material.WOOL }) {
				// Dye 8 of an item at a time just like stained glass, allows for re-dyeing
				shaped = new ShapedRecipe(new ItemStack(material, 8, dye.getWoolData()));
				shaped.shape("XXX", "XYX", "XXX");
				shaped.setIngredient('X', material, Short.MAX_VALUE).setIngredient('Y', dyeMaterial);
				getServer().addRecipe(shaped);
			}

			// Allow dyeing of panes
			Dye resultDye = new Dye(Material.STAINED_GLASS_PANE);
			resultDye.setColor(dye);
			shaped = new ShapedRecipe(resultDye.toItemStack(8));
			shaped.shape("XXX", "XYX", "XXX");
			shaped.setIngredient('X', Material.THIN_GLASS).setIngredient('Y', dyeMaterial);
			getServer().addRecipe(shaped);
		}

		// General: Packed ice = 2 snow 2 ice
		shaped = new ShapedRecipe(new ItemStack(Material.PACKED_ICE));
		shaped.shape("XY", "YX");
		shaped.setIngredient('X', Material.SNOW_BLOCK).setIngredient('Y', Material.ICE);
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
			Field field = cmdMap.getClass().getDeclaredField("knownCommands");
			field.setAccessible(true);
			@SuppressWarnings("unchecked")
			HashMap<String, Command> cmdMapKnownCommands = (HashMap<String, Command>) field.get(cmdMap);
			for (Iterator<Map.Entry<String, Command>> iterator = cmdMapKnownCommands.entrySet().iterator(); iterator.hasNext();) {
				if (iterator.next().getValue() instanceof SblockCommand) {
					iterator.remove();
				}
			}
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			getLogger().severe("Unable to modify SimpleCommandMap.knownCommands! Commands cannot be unregistered!");
			getLogger().severe(RegexUtils.getTrace(e));
		}
	}

	@SuppressWarnings("unchecked")
	public <T> T getModule(Class<T> clazz) {
		Validate.isTrue(Module.class.isAssignableFrom(clazz), "That's not a Module. Are you even trying?");
		Validate.isTrue(modules.containsKey(clazz), "Module not enabled!");
		Object object = modules.get(clazz);
		Validate.isTrue(clazz.isAssignableFrom(object.getClass()));
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
		Field field = cmdMap.getClass().getDeclaredField("knownCommands");
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

	public Random getRandom() {
		return random;
	}

	public static <T> boolean areDependenciesPresent(Class<T> clazz) {
		if (clazz.isAnnotationPresent(Dependencies.class)) {
			for (Dependency dependency : clazz.getAnnotation(Dependencies.class).value()) {
				String pluginName = dependency.value();
				if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
					Logger.getLogger("Sblock").severe("Dependency " + pluginName + " is not enabled!");
					return false;
				}
			}
		}
		return true;
	}
}
