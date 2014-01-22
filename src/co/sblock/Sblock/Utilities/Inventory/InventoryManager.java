package co.sblock.Sblock.Utilities.Inventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jikoo
 */
public class InventoryManager {

	/** A Map of all Players whose Inventory has been replaced. */
	private static Map<String, Inventory> viewers = new HashMap<String, Inventory>();

	/**
	 * Stores and clears a Player's Inventory.
	 * 
	 * @param p the Player
	 */
	public static void storeAndClearInventory(Player p) {
		viewers.put(p.getName(), p.getInventory());
	}

	/**
	 * Restore a Player's Inventory.
	 * 
	 * @param p the Player
	 */
	public static void restoreInventory(Player p) {
		if (viewers.containsKey(p.getName())) {
			p.getInventory().setContents(viewers.remove(p.getName()).getContents());
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
		Inventory i = viewers.get(name);
		if (add) {
			if (i.firstEmpty() == -1) {
				return false;
			}
			i.addItem(toModify);
			viewers.put(name, i);
			return true;
		}
		if (i.contains(toModify)) {
			i.remove(toModify); // Adam no this is not what you thought it was
			viewers.put(name, i);
			return true;
		}
		return false;
	}
}
