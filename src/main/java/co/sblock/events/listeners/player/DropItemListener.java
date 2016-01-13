package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerDropItemEvent;

import co.sblock.Sblock;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.type.computer.EmailWriter;
import co.sblock.machines.type.computer.Programs;

/**
 * Listener for PlayerDropItemEvents.
 * 
 * @author Jikoo
 */
public class DropItemListener extends SblockListener {

	private final EmailWriter email;

	public DropItemListener(Sblock plugin) {
		super(plugin);
		this.email = (EmailWriter) Programs.getProgramByName("EmailWriter");
	}

	/**
	 * EventHandler for PlayerDropItemEvents.
	 * 
	 * @param event the PlayerDropItemEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onItemDrop(PlayerDropItemEvent event) {
		if (email.isLetter(event.getItemDrop().getItemStack())) {
			event.setCancelled(true);
			return;
		}
	}
}
