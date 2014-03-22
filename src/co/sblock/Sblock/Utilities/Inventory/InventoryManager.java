package co.sblock.Sblock.Utilities.Inventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jikoo
 */
public class InventoryManager {

	/** A Map of all Players whose Inventory has been replaced. */
	private static Map<String, Inventory> inventories = new HashMap<String, Inventory>();

	/**
	 * Stores and clears a Player's Inventory.
	 * 
	 * @param p the Player
	 */
	public static void storeAndClearInventory(Player p) {
		// Do not delete a player's inventory if we screw up, hopefully.
		if (inventories.containsKey(p.getName())) {
			return;
		}

		// Cannot create or clone player inventories.
		// 4 rows regular inventory + 4 armor slots
		Inventory i = Bukkit.createInventory(p, 45);

		for (int j = 0; j < 40; j++) {
			if (p.getInventory().getItem(j) != null) {
				i.setItem(j, p.getInventory().getItem(j).clone());
				p.getInventory().setItem(j, null);
			}
		}
		inventories.put(p.getName(), i);
	}

	/**
	 * Restore a Player's Inventory.
	 * 
	 * @param p the Player
	 */
	public static void restoreInventory(Player p) {
		if (inventories.containsKey(p.getName())) {
			Inventory i = inventories.remove(p.getName());
			for (int j = 0; j < 40; j++) {
				p.getInventory().setItem(j, i.getItem(j));
			}
		}
	}

	/**
	 * Modify a stored Player's Inventory.
	 * 
	 * @param name the name of the Player
	 * @param add true if adding an item
	 * @param toModify the ItemStack to add or remove
	 * @return true if the modification was possible
	 */
	public static boolean modifyPlayerInventory(String name, boolean add, ItemStack toModify) {
		Inventory i = inventories.get(name);
		if (add) {
			if (i.firstEmpty() == -1) {
				return false;
			}
			i.addItem(toModify);
			inventories.put(name, i);
			return true;
		}
		if (i.contains(toModify.getType())) {
			i.removeItem(toModify);
			inventories.put(name, i);
			return true;
		}
		return false;
	}
}
