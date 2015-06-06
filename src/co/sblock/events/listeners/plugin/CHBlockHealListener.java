package co.sblock.events.listeners.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nitnelave.CreeperHeal.events.CHBlockHealEvent;

import co.sblock.machines.Machines;
import co.sblock.module.Dependency;

/**
 * Listener for CHBlockHealEvents.
 * 
 * @author Jikoo
 */
@Dependency("CreeperHeal")
public class CHBlockHealListener implements Listener {

	/**
	 * EventHandler for CHBlockHealEvents.
	 * 
	 * @param event the CHBlockHealEvent
	 */
	@EventHandler
	public void onCHBlockHeal(CHBlockHealEvent event) {
		Machines machines = Machines.getInstance();
		if (!machines.shouldRestore(event.getBlock().getBlock())) {
			machines.setRestored(event.getBlock().getBlock());
			event.setCancelled(true);
		}
		if (machines.isExploded(event.getBlock().getBlock())) {
			machines.setRestored(event.getBlock().getBlock());
			return;
		}
		if (machines.isMachine(event.getBlock().getBlock())) {
			if (event.shouldDrop()) {
				event.getBlock().drop(true);
			}
			event.setCancelled(true);
		}
	}
}
