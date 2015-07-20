package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

import co.sblock.Sblock;
import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.events.event.SblockBreakEvent;
import co.sblock.utilities.experience.BlockDrops;
import co.sblock.utilities.experience.Experience;

/**
 * Mine or dig a 3x3 area at once.
 * 
 * @author Jikoo
 */
public class EffectTunnelBore extends Effect implements EffectBehaviorActive {

	private final BlockFace[] faces;
	private final BlockFace[] levels;
	public EffectTunnelBore() {
		super(2500, 1, 1, "Tunnel Bore");
		faces = new BlockFace[] { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST,
				BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_EAST,
				BlockFace.SOUTH_WEST, BlockFace.SOUTH_EAST };
		levels = new BlockFace[] {BlockFace.SELF, BlockFace.DOWN, BlockFace.UP};
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockBreakEvent.class);
	}

	@Override
	public void handleEvent(Event event, Player player, int level) {
		if (!(event instanceof BlockBreakEvent) || event instanceof SblockBreakEvent) {
			return;
		}

		BlockBreakEvent breakEvent = (BlockBreakEvent) event;

		if (breakEvent.getPlayer().isSneaking()) {
			// Sneak to mine single blocks
			return;
		}

		Block block = breakEvent.getBlock();

		PermissionAttachment attachment = player.addAttachment(Sblock.getInstance());
		attachment.setPermission("nocheatplus.checks.blockbreak", true);
		for (BlockFace yLevel : levels) {
			if (block.getY() == 0 && yLevel == BlockFace.DOWN) {
				continue;
			}
			Block relativeCenter = block.getRelative(yLevel);
			if (yLevel != BlockFace.SELF) {
				sblockBreak(relativeCenter, player);
			}
			if (player.getItemInHand() == null) {
				breakEvent.setCancelled(true);
				return;
			}
			for (BlockFace face : faces) {
				sblockBreak(relativeCenter.getRelative(face), player);
				if (player.getItemInHand() == null) {
					breakEvent.setCancelled(true);
					return;
				}
			}
		}
		player.updateInventory();
		player.removeAttachment(attachment);
	}

	private void sblockBreak(Block block, Player player) {
		if (block.getType() == Material.BARRIER || block.getType() == Material.BEDROCK
				|| block.getType() == Material.COMMAND || block.getType() == Material.ENDER_PORTAL
				|| block.getType() == Material.ENDER_PORTAL_FRAME
				|| block.getType() == Material.PORTAL || block.isEmpty()) {
			return;
		}

		SblockBreakEvent event = new SblockBreakEvent(block, player);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled() || block.isLiquid()) {
			return;
		}
		if (player.getGameMode() == GameMode.CREATIVE) {
			block.setType(Material.AIR);
			return;
		}
		ItemStack hand = player.getItemInHand();
		Collection<ItemStack> drops = BlockDrops.getDrops(hand, block);
		int exp = BlockDrops.getExp(hand, block);
		if (hand.getType().getMaxDurability() > 0 && (!hand.containsEnchantment(Enchantment.DURABILITY)
				|| Math.random() < 100.0 / (hand.getEnchantmentLevel(Enchantment.DURABILITY) + 1))) {
			//if (BlockDrops.isProperTool(hand, block))
			hand.setDurability((short) (hand.getDurability() + 1));
			if (hand.getDurability() > hand.getType().getMaxDurability()) {
				player.setItemInHand(null);
				player.getWorld().playEffect(player.getLocation(), org.bukkit.Effect.ITEM_BREAK, hand.getType());
				player.getWorld().playSound(player.getLocation(), Sound.ITEM_BREAK, 5, 1F);
			}
		}
		block.setType(Material.AIR);
		for (ItemStack is : drops) {
			player.getWorld().dropItem(player.getLocation(), is).setPickupDelay(0);
		}
		if (exp > 0) {
			Experience.changeExp(player, exp);
		}
	}

}
