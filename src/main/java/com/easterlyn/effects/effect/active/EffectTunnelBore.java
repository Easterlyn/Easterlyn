package com.easterlyn.effects.effect.active;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.events.BlockUpdateManager;
import com.easterlyn.events.Events;
import com.easterlyn.events.event.EasterlynBreakEvent;
import com.easterlyn.utilities.BlockDrops;
import com.easterlyn.utilities.Experience;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;

/**
 * Mine or dig a 3x3 area at once.
 *
 * @author Jikoo
 */
public class EffectTunnelBore extends Effect implements BehaviorActive {

	private final BlockUpdateManager budManager;
	private final BlockFace[] faces;
	private final BlockFace[] levels;

	public EffectTunnelBore(Easterlyn plugin) {
		super(plugin, 1500, 1, 1, "Tunnel Bore");
		this.budManager = plugin.getModule(Events.class).getBlockUpdateManager();
		this.faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_EAST, BlockFace.SOUTH_WEST,
				BlockFace.SOUTH_EAST };
		this.levels = new BlockFace[] { BlockFace.DOWN, BlockFace.SELF, BlockFace.UP };
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(BlockBreakEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		if (!(event instanceof BlockBreakEvent) || event instanceof EasterlynBreakEvent) {
			return;
		}

		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		Player player = breakEvent.getPlayer();

		if (player.isSneaking()) {
			// Sneak to mine single blocks
			return;
		}

		Block block = breakEvent.getBlock();
		float hardness = block.getType().getHardness();
		PermissionAttachment attachment = player.addAttachment(getPlugin());
		attachment.setPermission("nocheatplus.checks.blockbreak", true);
		for (BlockFace yLevel : levels) {
			if (block.getY() == 0 && yLevel == BlockFace.DOWN) {
				continue;
			}
			Block relativeCenter = block.getRelative(yLevel);
			if (yLevel != BlockFace.SELF) {
				if (easterlynBreak(hardness, relativeCenter, player)) {
					return;
				}
			}
			for (BlockFace face : faces) {
				if (easterlynBreak(hardness, relativeCenter.getRelative(face), player)) {
					return;
				}
			}
		}
		player.updateInventory();
		player.removeAttachment(attachment);
	}

	private boolean easterlynBreak(float requiredHardness, Block block, Player player) {
		float blockHardness = block.getType().getHardness();
		if (blockHardness > requiredHardness || blockHardness < 0 || block.getState() instanceof InventoryHolder) {
			return false;
		}

		EasterlynBreakEvent event = new EasterlynBreakEvent(block, player);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled() || block.isLiquid()) {
			return false;
		}
		if (player.getGameMode() == GameMode.CREATIVE) {
			block.setType(Material.AIR, false);
			budManager.queueBlock(block);
			return false;
		}
		// Item breaking blocks has to be in main hand
		ItemStack hand = player.getInventory().getItemInMainHand();
		Collection<ItemStack> drops = BlockDrops.getDrops(getPlugin(), player, hand, block);
		int exp = BlockDrops.getExp(hand, block);
		block.setType(Material.AIR, false);
		budManager.queueBlock(block);
		for (ItemStack is : drops) {
			player.getWorld().dropItem(player.getLocation(), is).setPickupDelay(0);
		}
		if (exp > 0) {
			Experience.changeExp(player, exp);
		}
		ItemMeta handMeta = hand.getItemMeta();
		if (handMeta != null && !handMeta.isUnbreakable() && handMeta instanceof Damageable
				&& (!hand.containsEnchantment(Enchantment.DURABILITY)
				|| ThreadLocalRandom.current().nextDouble() < 1.0 / (hand.getEnchantmentLevel(Enchantment.DURABILITY) + 2))) {
			Damageable damageable = (Damageable) handMeta;
			damageable.setDamage(damageable.getDamage() + 1);
			hand.setItemMeta(handMeta);
			return damageable.getDamage() > hand.getType().getMaxDurability();
		}
		return false;
	}

}
