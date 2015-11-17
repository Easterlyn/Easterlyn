package co.sblock.events.packets;


import java.util.Arrays;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import co.sblock.Sblock;
import co.sblock.events.Events;

import net.md_5.bungee.api.ChatColor;

/**
 * @author Jikoo
 */
public class SyncPacketAdapter extends PacketAdapter {

	private final String version;
	public SyncPacketAdapter() {
		super(Sblock.getInstance(), PacketType.Status.Server.OUT_SERVER_INFO, PacketType.Play.Client.ENTITY_ACTION,
				PacketType.Play.Server.TAB_COMPLETE, PacketType.Play.Client.TAB_COMPLETE);

		// Sblock Alpha: 1.8 - X/Y
		version = ChatColor.GOLD + "Sblock Alpha" + ChatColor.DARK_GRAY + ": " + ChatColor.GRAY
				+ "1.8" + ChatColor.DARK_GRAY + " - %s%s" + ChatColor.DARK_GRAY + "/"
				+ ChatColor.GREEN + "%s";
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
		if (event.getPacketType() == PacketType.Status.Server.OUT_SERVER_INFO) {
			WrappedServerPing serverping = event.getPacket().getServerPings().read(0);

			// Causes client to see our custom message, cause woo! N.B. Does result in ping breaking
			// and outdated server displaying.
			serverping.setVersionProtocol(0);

			int online = serverping.getPlayersOnline(), max = serverping.getPlayersMaximum();

			// Percent-based color: 0-49 = green, 50-74 = yellow, 75-100 = red
			int percent = online * 100 / max;
			ChatColor percentColor = percent > 75 ? ChatColor.RED : percent > 50 ? ChatColor.YELLOW
					: ChatColor.GREEN;

			// Format and away we go
			serverping.setVersionName(String.format(version, percentColor, online, max));
		} else if (event.getPacketType() == PacketType.Play.Server.TAB_COMPLETE) {
			if (event.getPlayer().hasPermission("sblock.denizen")) {
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
			if (event.getPacket().getPlayerActions().read(0) == PlayerAction.STOP_SLEEPING
					&& Events.getInstance().getSleepTasks().containsKey(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				Events.getInstance().fakeWakeUp(event.getPlayer());
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
