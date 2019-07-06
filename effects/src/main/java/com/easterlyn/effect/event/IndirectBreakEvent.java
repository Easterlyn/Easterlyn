package com.easterlyn.effect.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Special BlockBreakEvent called to prevent tunnel bore from running recursively.
 */
public class IndirectBreakEvent extends BlockBreakEvent {

	public IndirectBreakEvent(Block block, Player player) {
		super(block, player);
	}

}
