/**
 * 
 */
package co.sblock.Sblock.Machines;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * @author Jikoo
 *
 */
public class InventoryEventHandler implements Listener {

	private Map<String, Inventory> viewers = new HashMap<String, Inventory>();

//	@EventHandler
//	public void onComputerOpen(PlayerInteractEvent event) {
//		if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK)
//				&& event.getClickedBlock().getType().equals(Material.JUKEBOX)) {
//			viewers.put(event.getPlayer().getName(), event.getPlayer().getInventory());
//			event.getPlayer().getInventory().setContents(null);
////			p.openInventory(this.getComputerLayout(p.getName()));
//		}
//	}

	@EventHandler
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

	public boolean modifyPlayerInventory(String name, boolean add, ItemStack toModify) {
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

//	private Inventory getComputerLayout(String pName) {
//		SblockUser user = SblockUser.getUser(pName);
//		Inventory i = Bukkit.createInventory(user.getPlayer(), 9, user.getNick() + "'s Computer");
//		for (String s : user.getInstalledPrograms()) {
//			i.addItem(Icon.valueOf(s).getIcon());
//		}
//	}
}
