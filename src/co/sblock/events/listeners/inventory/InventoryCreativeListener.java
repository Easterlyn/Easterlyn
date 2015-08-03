package co.sblock.events.listeners.inventory;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.utilities.inventory.InventoryUtils;

/**
 * Listener for InventoryCreativeEvents. Used to clean input items from creative clients, preventing
 * server/client crashes.
 * 
 * @author Jikoo
 */
public class InventoryCreativeListener implements Listener {

	private final Material[] blacklist = new Material[] { Material.BARRIER, Material.BEDROCK,
			Material.COMMAND, Material.COMMAND_MINECART, Material.ENDER_PORTAL,
			Material.ENDER_PORTAL_FRAME, Material.JUKEBOX, Material.EXPLOSIVE_MINECART,
			Material.MOB_SPAWNER, Material.MONSTER_EGG, Material.MONSTER_EGGS, Material.TNT };

	/**
	 * EventHandler for InventoryCreativeEvents. Triggered when a creative client spawns an item.
	 * <p>
	 * The click fired is always left, cursor is always the item being created/placed, current is
	 * always the item being replaced. This holds true even for item drops, pick block, etc.
	 * 
	 * @param event the InventoryCreativeEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onInventoryCreative(InventoryCreativeEvent event) {
		if (event.getWhoClicked().hasPermission("sblock.felt")) {
			return;
		}

		if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
			return;
		}

		// Blacklist
		for (Material m : blacklist) {
			if (event.getCursor().getType() == m) {
				event.setCancelled(true);
			}
		}

		ItemStack cleanedItem = InventoryUtils.cleanNBT(event.getCursor());

		// No invalid durabilities.
		if (event.getCursor().getDurability() < 0) {
			cleanedItem.setDurability((short) 0);
		} else {
			cleanedItem.setDurability(event.getCursor().getDurability());
		}

		// No overstacking, no negative amounts
		if (event.getCursor().getAmount() > event.getCursor().getMaxStackSize()) {
			event.getCursor().setAmount(event.getCursor().getMaxStackSize());
		} else if (event.getCursor().getAmount() < 1) {
			event.getCursor().setAmount(1);
		} else {
			cleanedItem.setAmount(event.getCursor().getAmount());
		}

		event.setCursor(cleanedItem);
	}
}
