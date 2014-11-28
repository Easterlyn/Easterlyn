package co.sblock.events.listeners;

import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

/**
 * Listener for InventoryCreativeEvents. Used to clean input items from creative clients, preventing
 * server/client crashes.
 * 
 * @author Jikoo
 */
public class InventoryCreativeListener implements Listener {

	private final Material[] blacklist = new Material[] {Material.BARRIER, Material.BEDROCK,
			Material.COMMAND, Material.COMMAND_MINECART, Material.ENDER_PORTAL, Material.MOB_SPAWNER};

	/**
	 * EventHandler for InventoryCreativeEvents. Triggered when a creative client spawns an item.
	 * <p>
	 * The click fired is always left, cursor is always the item being created/placed, current is
	 * always the item being replaced. This holds true even for item drops, pick block, etc.
	 * 
	 * @param event the InventoryCreativeEvent
	 */
	@EventHandler
	public void onInventoryCreative(InventoryCreativeEvent event) {
		if (event.getWhoClicked().hasPermission("group.felt")) {
			return;
		}

		if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
			return;
		}

		if (event.getCursor().getType() == Material.BANNER) {
			// Banners actually come with NBT tags when using pick-block. We'll just avoid them for now.
			return;
		}

		// Blacklist
		for (Material m : blacklist) {
			if (event.getCursor().getType() == m) {
				event.setCancelled(true);
			}
		}

		// By not using the original ItemStack, we remove all lore and attributes spawned.
		ItemStack cleanedItem = new ItemStack(event.getCursor().getType());
		// Why Bukkit doesn't have a constructor ItemStack(MaterialData) I don't know.
		cleanedItem.setData(event.getCursor().getData());

		// No invalid durabilities.
		if (event.getCursor().getDurability() < 0) {
			cleanedItem.setDurability((short) 0);
		} else {
			cleanedItem.setDurability(event.getCursor().getDurability());
		}

		// No overstacking, no negative amounts (negative dispensed by dropper/dispenser = infinite)
		if (event.getCursor().getAmount() > event.getCursor().getMaxStackSize()) {
			event.getCursor().setAmount(event.getCursor().getMaxStackSize());
		} else if (event.getCursor().getAmount() < 1) {
			event.getCursor().setAmount(1);
		} else {
			cleanedItem.setAmount(event.getCursor().getAmount());
		}

		// Creative enchanted books are allowed a single enchant
		if (event.getCursor().getType() == Material.ENCHANTED_BOOK && event.getCursor().hasItemMeta()) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
			for (Map.Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) event.getCursor().getItemMeta()).getStoredEnchants().entrySet()) {
				meta.addStoredEnchant(entry.getKey(), entry.getValue(), false);
				break;
			}
			cleanedItem.setItemMeta(meta);
		}

		event.setCursor(cleanedItem);
	}
}
