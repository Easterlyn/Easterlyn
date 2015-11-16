package co.sblock.events.listeners.inventory;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.utilities.InventoryUtils;

/**
 * Listener for InventoryCreativeEvents. Used to clean input items from creative clients, preventing
 * server/client crashes.
 * 
 * @author Jikoo
 */
public class InventoryCreativeListener implements Listener {

	private final Material[] blacklist = new Material[] { Material.ACTIVATOR_RAIL,
			Material.BARRIER, Material.BEDROCK, Material.COMMAND, Material.COMMAND_MINECART,
			Material.DETECTOR_RAIL, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
			Material.EXPLOSIVE_MINECART, Material.HOPPER_MINECART, Material.JUKEBOX,
			Material.MINECART, Material.MOB_SPAWNER, Material.MONSTER_EGG, Material.MONSTER_EGGS,
			Material.POWERED_MINECART, Material.POWERED_RAIL, Material.RAILS,
			Material.STORAGE_MINECART, Material.TNT };

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

		if (cleanedItem == event.getCursor()) {
			return;
		}

		// No invalid durabilities.
		if (cleanedItem.getDurability() < 0) {
			cleanedItem.setDurability((short) 0);
		}

		// No overstacking, no negative amounts
		if (cleanedItem.getAmount() > cleanedItem.getMaxStackSize()) {
			cleanedItem.setAmount(cleanedItem.getMaxStackSize());
		} else if (cleanedItem.getAmount() < 1) {
			cleanedItem.setAmount(1);
		}

		// TODO this may be irrelevant: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/craftbukkit/browse/nms-patches/PlayerConnection.patch#1329
		event.setCursor(cleanedItem);
	}
}
