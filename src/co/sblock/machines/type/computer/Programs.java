package co.sblock.machines.type.computer;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import org.reflections.Reflections;

/**
 * A container for all Programs.
 * 
 * @author Jikoo
 */
public class Programs {

	private static final Map<String, Program> byName;

	static {
		byName = new HashMap<>();

		Reflections reflections = new Reflections("co.sblock.machines.type.computer");
		for (Class<? extends Program> type : reflections.getSubTypesOf(Program.class)) {
			if (Modifier.isAbstract(type.getModifiers())) {
				continue;
			}
			try {
				Program program = type.newInstance();
				if (program.getIcon() == null) {
					continue;
				}
				byName.put(type.getSimpleName(), program);
			} catch (InstantiationException | IllegalAccessException e) {
				// Improperly set up Machine
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
		return null;
	}

	/**
	 * Compares two ItemStacks, ignoring lore.
	 * 
	 * @param is1 the first ItemStack
	 * @param is2 the second ItemStack
	 * @return true if the ItemStacks are similar aside from lore
	 */
	private static boolean looseCompare(ItemStack is1, ItemStack is2) {
		if (is1.getType() != is2.getType()) {
			return false;
		}
		if (is1.hasItemMeta() && is2.hasItemMeta()) {
			ItemMeta meta1 = is1.getItemMeta();
			meta1.setLore(null);
			ItemMeta meta2 = is2.getItemMeta();
			meta2.setLore(null);
			return meta1.equals(meta2);
		}
		return true;
	}

	/**
	 * Gets a Program by its Computer installer.
	 * 
	 * @param intaller the installer ItemStack
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
