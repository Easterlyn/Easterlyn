package co.sblock.events.packets;

import org.bukkit.ChatColor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerAction;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import co.sblock.Sblock;
import co.sblock.events.Events;

/**
 * @author Jikoo
 */
public class SyncPacketAdapter extends PacketAdapter {

	public SyncPacketAdapter() {
		super(Sblock.getInstance(), PacketType.Status.Server.OUT_SERVER_INFO, PacketType.Play.Client.ENTITY_ACTION);
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
			// and outdated client displaying.
			serverping.setVersionProtocol(9999);

			// Percent-based color: 0-49 = green, 50-74 = yellow, 75-100 = red
			int percent = serverping.getPlayersOnline() * 100 / serverping.getPlayersMaximum();
			ChatColor percentColor = percent > 75 ? ChatColor.RED : percent > 50 ? ChatColor.YELLOW
					: ChatColor.GREEN;
			// Sblock Alpha: 1.8 - X/30
			String versionName = ChatColor.GOLD + "Sblock Alpha" + ChatColor.DARK_GRAY + ": "
					+ ChatColor.GRAY + "1.8" + ChatColor.DARK_GRAY + " - " + percentColor
					+ serverping.getPlayersOnline() + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN
					+ serverping.getPlayersMaximum();
			serverping.setVersionName(versionName);
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
					&& Events.getInstance().getTasks().containsKey(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				Events.getInstance().fakeWakeUp(event.getPlayer());
			}
			return;
		}
	}
}
