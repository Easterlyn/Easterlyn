package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for BlockFadeEvents.
 * 
 * @author Jikoo
 */
public class BlockFadeListener implements Listener {

    /**
     * EventHandler for BlockFadeEvents.
     * 
     * @param event the BlockFadeEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
        if (m != null) {
            event.setCancelled(m.handleFade(event));
        }
    }
}
