package co.sblock.events.listeners.player;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Wool;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for PlayerShearEntityEvents.
 * 
 * @author Jikoo
 */
public class ShearEntityListener extends SblockListener {

	public ShearEntityListener(Sblock plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for PlayerShearEntityEvents.
	 * 
	 * @param event the PlayerShearEntityEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerShearEntity(PlayerShearEntityEvent event) {
		ItemStack hand = event.getPlayer().getInventory().getItemInMainHand();
		if (hand != null && hand.getType() == Material.SHEARS) {
			hand = event.getPlayer().getInventory().getItemInMainHand();
		} else {
			hand = event.getPlayer().getInventory().getItemInOffHand();
		}
		if (hand == null || !hand.hasItemMeta()
				|| !hand.containsEnchantment(Enchantment.LOOT_BONUS_BLOCKS)
				|| hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) < 1) {
			return;
		}
		ItemStack is = getDrop(event.getEntity());
		if (is == null) {
			return;
		}
		int total = getPlugin().getRandom().nextInt(hand.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS) + 1);
		if (total == 0) {
			return;
		}
		is.setAmount(total);
		event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation().add(0, .5, 0), is);
	}

	private ItemStack getDrop(Entity entity) {
		if (entity.getType() == EntityType.SHEEP) {
			return new Wool(((Sheep) entity).getColor()).toItemStack();
		}
		if (entity.getType() == EntityType.MUSHROOM_COW) {
			return new ItemStack(Material.RED_MUSHROOM);
		}
		return null;
	}
}
