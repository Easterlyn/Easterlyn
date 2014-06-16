package co.sblock.events.listeners;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceBurnEvent;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;

/**
 * Listener for FurnaceBurnEvents.
 * 
 * @author Jikoo
 */
public class FurnaceBurnListener implements Listener {

    /**
     * EventHandler for when fuel is consumed by a furnace.
     * 
     * @param event the FurnaceBurnEvent
     */
    @EventHandler(ignoreCancelled = true)
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
        if (m != null) {
            event.setCancelled(m.handleBurnFuel(event));
            return;
        }

        if (((Furnace) event.getBlock().getState()).getInventory().getSmelting().getType()
                == Material.NETHER_BRICK_ITEM) {
            // Nether bricks are our dowels, the recipe created for them is quite useless.
            event.setCancelled(true);
        }
    }
}
