package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * A base for effects that respect protection plugins by simulating block placements when modifying
 * adjacent blocks.
 *
 * @author Jikoo
 */
public abstract class EffectAdjacentBlockPlacement extends EffectAdjacentBlockModifier {

	EffectAdjacentBlockPlacement(EasterlynEffects plugin, String name, int cost) {
		super(plugin, name, cost);
	}

	boolean handleBlockSet(Player player, Block block, Material toMaterial) {
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
