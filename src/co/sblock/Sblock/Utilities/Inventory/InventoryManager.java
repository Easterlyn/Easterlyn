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

	/**
	 * A <code>Map</code> of all <code>Players</code> whose
	 * <code>Inventory</code> has been replaced
	 */
	private static Map<String, Inventory> viewers = new HashMap<String, Inventory>();

	/**
	 * Stores a <code>Player</code>'s <code>Inventory</code>, then clears their
	 * ingame <code>Inventory</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 */
	public static void storeAndClearInventory(Player p) {
		viewers.put(p.getName(), p.getInventory());
	}

	/**
	 * Restore a <code>Player</code>'s <code>Inventory</code>.
	 * 
	 * @param p
	 *            the <code>Player</code>
	 */
	public static void restoreInventory(Player p) {
		if (viewers.containsKey(p.getName())) {
			p.getInventory().setContents(viewers.remove(p.getName()).getContents());
		}
	}

	/**
	 * Modify a stored <code>Player</code>'s <code>Inventory</code>.
	 * 
	 * @param name
	 *            the name of the <code>Player</code>
	 * @param add
	 *            <code>true</code> if adding an item
	 * @param toModify
	 *            the <code>ItemStack</code> to add or remove
	 * @return <code>true</code> if the modification was possible
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
			i.remove(toModify);
			viewers.put(name, i);
			return true;
		}
		return false;
	}
}
