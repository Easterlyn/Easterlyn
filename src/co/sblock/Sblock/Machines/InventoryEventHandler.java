/**
 * 
 */
package co.sblock.Sblock.Machines;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

/**
 * @author Jikoo
 *
 */
public class InventoryEventHandler {

	private Map<String, Inventory> viewers = new HashMap<String, Inventory>();

	public void onComputerOpen(Player p) {
		viewers.put(p.getName(), p.getInventory());
		p.getInventory().setContents(null);
//		p.openInventory(this.getComputerLayout(p.getName()));
	}

	public boolean onInventoryClick(InventoryClickEvent event) {
		if (viewers.containsKey(event.getWhoClicked().getName())) {
			Icon.getIcon(event.getCurrentItem().getType())
					.execute((Player) event.getWhoClicked());
			return true;
		} else {
			return false;
		}
	}

	public void onInventoryClose(InventoryCloseEvent event) {
		if (viewers.containsKey(event.getPlayer().getName())) {
			event.getPlayer().getInventory()
				.setContents(viewers.get(event.getPlayer()
						.getName()).getContents());
			viewers.remove(event.getPlayer().getName());
		}
	}

//	private Inventory getComputerLayout(String pName) {
//		SblockUser user = SblockUser.getUser(pName);
//		Inventory i = Bukkit.createInventory(user.getPlayer(), 9, user.getNick() + "'s Computer");
//		for (String s : user.getInstalledPrograms()) {
//			i.addItem(Icon.valueOf(s).getIcon());
//		}
//	}
}
