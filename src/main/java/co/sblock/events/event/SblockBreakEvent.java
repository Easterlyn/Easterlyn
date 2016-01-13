package co.sblock.events.event;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Event wrapper used to prevent certain methods from recursively calling themselves.
 * 
 * @author Jikoo
 */
public class SblockBreakEvent extends BlockBreakEvent {

	public SblockBreakEvent(Block theBlock, Player player) {
		super(theBlock, player);
	}
}
