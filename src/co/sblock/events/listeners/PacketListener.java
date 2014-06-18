package co.sblock.events.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.chat.chester.ChesterListener;
import co.sblock.events.SblockEvents;

/**
 * @author Jikoo
 */
public class PacketListener extends PacketAdapter {

	public PacketListener() {
		super(Sblock.getInstance(), PacketType.Play.Client.ENTITY_ACTION,
				PacketType.Play.Client.CHAT);
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
		} else if (event.getPacket().getType().equals(PacketType.Play.Client.CHAT)) {
			if (ChesterListener.getTriggers() == null) {
				return;
			}
			String message = event.getPacket().getStrings().read(0);
			for (String trigger : ChesterListener.getTriggers()) {
				if (message.equalsIgnoreCase(trigger)) {
					event.getPlayer().sendMessage(ColorDef.HAL + "What?");
					event.setCancelled(true);
				}
			}
		}
	}
}
