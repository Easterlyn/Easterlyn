package com.easterlyn.machines.type.computer;

import com.easterlyn.Easterlyn;
import com.easterlyn.machines.Machines;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.reflections.Reflections;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * A container for all Programs.
 * 
 * @author Jikoo
 */
public class Programs {

	private static final Map<String, Program> byName;

	static {
		byName = new HashMap<>();

		Reflections reflections = new Reflections("com.easterlyn.machines.type.computer");
		// FIXME bad practice
		Machines machines = ((Easterlyn) Bukkit.getPluginManager().getPlugin("Easterlyn")).getModule(Machines.class);
		for (Class<? extends Program> type : reflections.getSubTypesOf(Program.class)) {
			if (Modifier.isAbstract(type.getModifiers())) {
				continue;
			}
			try {
				Constructor<? extends Program> constructor = type.getConstructor(Machines.class);
				Program program = constructor.newInstance(machines);
				if (program.getIcon() == null) {
					continue;
				}
				byName.put(type.getSimpleName(), program);
			} catch (InstantiationException | IllegalAccessException | NoSuchMethodException
					| SecurityException | IllegalArgumentException | InvocationTargetException e) {
				// Improperly set up Program
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets a Program by name.
	 * 
	 * @param name the name of the Program
	 * @return the Program, or null if invalid
	 */
	public static Program getProgramByName(String name) {
		if (!byName.containsKey(name)) {
			return null;
		}
		return byName.get(name);
	}

	/**
	 * Gets a Program by its Computer icon.
	 * 
	 * @param icon the icon ItemStack
	 * @return the Program, or null if no matches are found
	 */
	public static Program getProgramByIcon(ItemStack icon) {
		if (icon == null) {
			return null;
		}
		for (Program program : byName.values()) {
			if (looseCompare(icon, program.getIcon())) {
				return program;
			}
		}
		for (Program program : byName.values()) {
			if (icon.getType() == program.getIcon().getType()) {
				return program;
			}
		}
		return null;
	}

	/**
	 * Compares two ItemStacks by name or material data.
	 * 
	 * @param is1 the first ItemStack
	 * @param is2 the second ItemStack
	 * @return true if the ItemStacks have the same name or type
	 */
	private static boolean looseCompare(ItemStack is1, ItemStack is2) {
		if (is1.hasItemMeta() && is2.hasItemMeta()) {
			ItemMeta meta1 = is1.getItemMeta();
			ItemMeta meta2 = is2.getItemMeta();
			if (meta1.hasDisplayName() && meta2.hasDisplayName()
					&& meta1.getDisplayName().equals(meta2.getDisplayName())) {
				return true;
			}
		}
		return is1.getData().equals(is2.getData());
	}

	/**
	 * Gets a Program by its Computer installer.
	 * 
	 * @param installer the installer ItemStack
	 * @return the Program, or null if no matches are found
	 */
	public static Program getProgramByInstaller(ItemStack installer) {
		if (installer == null) {
			return null;
		}
		for (Program program : byName.values()) {
			if (installer.isSimilar(program.getInstaller())) {
				return program;
			}
		}
		return null;
	}

	/**
	 * Gets a Collection of all Programs registered.
	 * 
	 * @return the Programs
	 */
	public static Collection<Program> getPrograms() {
		return byName.values();
	}
}
