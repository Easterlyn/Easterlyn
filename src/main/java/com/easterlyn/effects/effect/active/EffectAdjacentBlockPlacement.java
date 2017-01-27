package com.easterlyn.effects.effect.active;

import com.easterlyn.Easterlyn;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachment;

/**
 * A base for effects that respect protection plugins by simulating block placements when modifying
 * adjacent blocks.
 * 
 * @author Jikoo
 */
public abstract class EffectAdjacentBlockPlacement extends EffectAdjacentBlockModifier {

	protected EffectAdjacentBlockPlacement(Easterlyn plugin, int cost, String name, Material... updateMaterials) {
		super(plugin, cost, name, updateMaterials);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		Player player = ((BlockBreakEvent) event).getPlayer();
		PermissionAttachment attachment = player.addAttachment(getPlugin());
		attachment.setPermission("nocheatplus.checks.blockplace", true);
		super.handleEvent(event, entity, level);
		player.removeAttachment(attachment);
	}

	protected boolean handleBlockSet(Player player, Block block, Material toMaterial) {
		// Capture state and change block - prevents certain plugins assuming block being placed is of the replaced material
		BlockState state = block.getState();
		block.setType(toMaterial, false);
		BlockPlaceEvent event = new BlockPlaceEvent(block, state, block, new ItemStack(toMaterial), player, true, EquipmentSlot.HAND);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (event.isCancelled()) {
			// Revert block changes if cancelled
			state.update(true, false);
			return false;
		}
		block.getState().update(false);
		return true;
	}

}
