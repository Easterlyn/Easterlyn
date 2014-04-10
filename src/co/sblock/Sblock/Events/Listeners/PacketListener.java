package co.sblock.Sblock.Events.Listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Events.SblockEvents;

/**
 * @author Jikoo
 */
public class PacketListener extends PacketAdapter {

	public PacketListener() {
		super(Sblock.getInstance(), PacketType.Play.Client.ENTITY_ACTION);
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
	}
}
