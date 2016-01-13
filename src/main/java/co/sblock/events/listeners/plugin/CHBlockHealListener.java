package co.sblock.events.listeners.plugin;

import org.bukkit.event.EventHandler;

import com.nitnelave.CreeperHeal.events.CHBlockHealEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.module.Dependency;

/**
 * Listener for CHBlockHealEvents.
 * 
 * @author Jikoo
 */
@Dependency("CreeperHeal")
public class CHBlockHealListener extends SblockListener {

	private final Machines machines;

	public CHBlockHealListener(Sblock plugin) {
		super(plugin);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * EventHandler for CHBlockHealEvents.
	 * 
	 * @param event the CHBlockHealEvent
	 */
	@EventHandler
	public void onCHBlockHeal(CHBlockHealEvent event) {
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
