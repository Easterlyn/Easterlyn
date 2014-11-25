package co.sblock.events.listeners;

import java.util.regex.Pattern;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.events.SblockEvents;

/**
 * @author Jikoo
 */
public class PacketListener extends PacketAdapter {

	private final String[] triggers = new String[] {"hal", "dirk"};
	private final Pattern plugin = Pattern.compile("/(bukkit:)?(about|ver(sion)?)\\s");
	public PacketListener() {
		super(Sblock.getInstance(), PacketType.Play.Client.ENTITY_ACTION, PacketType.Play.Client.CHAT,
				PacketType.Play.Client.TAB_COMPLETE);
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
		if (event.getPacket().getType() == PacketType.Play.Client.ENTITY_ACTION) {
			if (event.getPacket().getIntegers().read(1) == 3
					&& SblockEvents.getEvents().tasks.containsKey(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				SblockEvents.getEvents().fakeWakeUp(event.getPlayer());
			}
			return;
		}
		if (event.getPacket().getType() == PacketType.Play.Client.CHAT) {
			String message = event.getPacket().getStrings().read(0);
			for (String trigger : triggers) {
				if (message.equalsIgnoreCase(trigger)) {
					event.getPlayer().sendMessage(ColorDef.HAL + "What?");
					event.setCancelled(true);
					return;
				}
			}
			return;
		}
		if (event.getPacket().getType() == PacketType.Play.Client.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("group.denizen")) {
				return;
			}
			if (plugin.matcher(event.getPacket().getStrings().read(0)).find()) {
				event.setCancelled(true);
			}
		}
	}
}
