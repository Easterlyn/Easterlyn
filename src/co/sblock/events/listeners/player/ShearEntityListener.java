package co.sblock.events.listeners.player;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for PlayerShearEntityEvents.
 * 
 * @author Jikoo
 */
public class ShearEntityListener implements Listener {

	/**
	 * EventHandler for PlayerShearEntityEvents.
	 * 
	 * @param event the PlayerShearEntityEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		ItemStack hand = event.getPlayer().getItemInHand();
		if (hand == null || !hand.hasItemMeta() || !hand.getItemMeta().hasEnchants()
				|| hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) < 1) {
			return;
		}
		ItemStack is = getDrop(event.getEntity());
		if (is == null) {
			return;
		}
		int fortune = hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS);
		double extradropchance = fortune == 1 ? .3 : fortune == 2 ? .25 : .2;
		int total = 0;
		for (int i = 0; i < fortune; i++) {
			if (Math.random() < extradropchance) {
				total++;
			}
		}
		if (total == 0) {
			return;
		}
		is.setAmount(total);
		event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, .5, 0), is);
	}

	@SuppressWarnings("deprecation")
	private ItemStack getDrop(Entity entity) {
		if (entity.getType() == EntityType.SHEEP) {
			return new ItemStack(Material.WOOL, (int) (Math.random() * 3) + 1, ((Sheep) entity).getColor().getWoolData());
		}
		if (entity.getType() == EntityType.MUSHROOM_COW) {
			return new ItemStack(Material.RED_MUSHROOM, 5);
		}
		return null;
	}
}
