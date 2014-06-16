package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.sblock.machines.MachineManager;
import co.sblock.machines.SblockMachines;

import com.nitnelave.CreeperHeal.events.CHBlockHealEvent;

/**
 * Listener for CHBlockHealEvents.
 * 
 * @author Jikoo
 */
public class CHBlockHealListener implements Listener {

    /**
     * EventHandler for CHBlockHealEvents.
     * 
     * @param event the CHBlockHealEvent
     */
    @EventHandler
    public void onCHBlockHeal(CHBlockHealEvent event) {
        MachineManager mgr = SblockMachines.getMachines().getManager();
        if (!mgr.shouldRestore(event.getBlock().getBlock())) {
            mgr.setRestored(event.getBlock().getBlock());
            event.setCancelled(true);
        }
        if (mgr.isExploded(event.getBlock().getBlock())) {
            return;
        }
        if (mgr.isMachine(event.getBlock().getBlock())) {
            if (event.shouldDrop()) {
                event.getBlock().drop(true);
            }
            event.setCancelled(true);
        }
    }
}
