package co.sblock.Sblock.Utilities.Inventory;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jikoo
 *
 */
public class InventoryManager {

	private static Map<String, Inventory> viewers = new HashMap<String, Inventory>();

	public static void storeAndClearInventory(Player p) {
		viewers.put(p.getName(), p.getInventory());
	}

	public static void restoreInventory(Player p) {
		if (viewers.containsKey(p.getName())) {
			p.getInventory().setContents(viewers.remove(p.getName()).getContents());
		}
	}

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
