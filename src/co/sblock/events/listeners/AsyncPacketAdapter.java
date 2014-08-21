package co.sblock.events.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import co.sblock.Sblock;
import co.sblock.data.SblockData;
import co.sblock.events.SblockEvents;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;
import com.tmathmeyer.jadis.async.Promise;

/**
 * 
 * 
 * @author Jikoo
 */
public class AsyncPacketAdapter extends PacketAdapter {

	public AsyncPacketAdapter() {
		super(Sblock.getInstance(), PacketType.Status.Server.OUT_SERVER_INFO);
	}

	@Override
	public void onPacketSending(PacketEvent event) {
		if (event.getPacketType() != PacketType.Status.Server.OUT_SERVER_INFO) {
			return;
		}
		final WrappedServerPing serverping = event.getPacket().getServerPings().read(0);
		if (serverping.getVersionProtocol() == 9999) {
			return;
		}
		event.setCancelled(true);

		// Causes client to see our custom message, cause woo! N.B. Does result in ping breaking and outdated client displaying.
		serverping.setVersionProtocol(9999);

		// Percent-based color: 0-49 = green, 50-74 = yellow, 75-100 = red
		int percent = serverping.getPlayersOnline() * 100 / serverping.getPlayersMaximum();
		ChatColor percentColor = percent > 75 ? ChatColor.RED : percent > 50 ? ChatColor.YELLOW : ChatColor.GREEN;

		String versionName = ChatColor.GOLD + "Sblock Alpha" + ChatColor.DARK_GRAY + ": "
				+ ChatColor.GRAY + "1.7.10" + ChatColor.DARK_GRAY + " - " + percentColor
				+ serverping.getPlayersOnline() + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN
				+ serverping.getPlayersMaximum();
		serverping.setVersionName(versionName);

		String addr = event.getPlayer().getAddress().getAddress().toString().substring(1);

		if (event.getAsyncMarker() != null) {
			// Signal delay of packet sending
			event.getAsyncMarker().incrementProcessingDelay();
		}
		SblockData.getDB().getUserFromIP(addr, getMOTDSettingExecutor(event, serverping));
		// Signal completion of handling packet
		ProtocolLibrary.getProtocolManager().getAsynchronousManager().signalPacketTransmission(event);
	}

	private final Promise<String> getMOTDSettingExecutor(final PacketEvent event, final WrappedServerPing serverping) {
		return new Promise<String>() {

			@Override
			public void getList(List<String> list) { }

			@Override
			public void getMap(Map<String, String> map) { }

			@Override
			public void getObject(String playerName, String unused) {
				if (!playerName.equals("Player")) {
					SblockEvents.getEvents().getLogger().info(playerName + " pinged the server from "
							+ event.getPlayer().getAddress().getAddress().toString().substring(1));
					serverping.setMotD(Bukkit.getMotd().replaceAll("Player", playerName));
				}
			}

			@Override
			public void getSet(Set<String> set) { }
		};
	}
}
