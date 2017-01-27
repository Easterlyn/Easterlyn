package com.easterlyn.events.packets;


import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.easterlyn.Easterlyn;
import com.easterlyn.micromodules.DreamTeleport;
import com.easterlyn.utilities.PermissionUtils;

/**
 * @author Jikoo
 */
public class SyncPacketAdapter extends PacketAdapter {

	private final DreamTeleport dream;

	public SyncPacketAdapter(Easterlyn plugin) {
		super(plugin, PacketType.Play.Client.ENTITY_ACTION, PacketType.Play.Server.TAB_COMPLETE,
				PacketType.Play.Client.TAB_COMPLETE);
		this.dream = plugin.getModule(DreamTeleport.class);

		PermissionUtils.addParent("sblock.commands.unfiltered", "sblock.denizen");
	}

	/**
	 * Edit packets outgoing to the client.
	 * 
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix.protocol.events.PacketEvent)
	 * 
	 * @param event the PacketEvent
	 */
	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("sblock.commands.unfiltered")) {
				return;
			}
			event.getPacket().getStringArrays().write(0,
					Arrays.stream(event.getPacket().getStringArrays().read(0))
							.filter(completion -> completion.length() < 1
									|| completion.indexOf('/') != 0
									|| completion.indexOf(':') < 0)
							.toArray(size -> new String[size]));
		}
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
			if (dream.isEnabled() && event.getPacket().getPlayerActions().read(0) == PlayerAction.STOP_SLEEPING
					&& dream.getSleepTasks().containsKey(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				dream.fakeWakeUp(event.getPlayer());
			}
			return;
		} else if (event.getPacketType() == PacketType.Play.Client.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("sblock.denizen")) {
				return;
			}
			String completing = event.getPacket().getStrings().read(0);
			int colon = completing.indexOf(':'), space = completing.indexOf(' ');
			if (colon > 0 && (space < 0 || space > colon)) {
				event.setCancelled(true);
			}
		}
	}

}
