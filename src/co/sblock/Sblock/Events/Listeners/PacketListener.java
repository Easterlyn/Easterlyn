package co.sblock.Sblock.Events.Listeners;

import org.bukkit.event.Listener;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Events.SblockEvents;

/**
 * @author Jikoo
 */
public class PacketListener extends PacketAdapter implements Listener {

	public PacketListener() {
		super(Sblock.getInstance(), PacketType.Play.Client.ENTITY_ACTION, PacketType.Play.Client.WINDOW_CLICK);
	}

	/**
	 * Check a packet from the client.
	 * 
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketReceiving(PacketEvent)
	 * 
	 * @param event the PacketEvent
	 */
	@Override
	public void onPacketReceiving(PacketEvent event) {
		if (event.getPacket().getType().equals(PacketType.Play.Client.ENTITY_ACTION)) {
			if (event.getPacket().getIntegers().read(1) == 3
					&& SblockEvents.getEvents().tasks.containsKey(event.getPlayer().getName())) {
				event.setCancelled(true);
				SblockEvents.getEvents().fakeWakeUp(event.getPlayer());
			}
		}

		if (event.getPacket().getType().equals(PacketType.Play.Client.WINDOW_CLICK)) {
			// TODO punch designix handling (Keiko?)
		}
	}
}
