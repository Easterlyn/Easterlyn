package co.sblock.events.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;

import com.tmathmeyer.jadis.async.Promise;

import co.sblock.Sblock;
import co.sblock.chat.ColorDef;
import co.sblock.chat.chester.ChesterListener;
import co.sblock.data.SblockData;
import co.sblock.events.SblockEvents;
import co.sblock.events.packets.WrapperStatusServerOutServerInfo;

/**
 * @author Jikoo
 */
public class PacketListener extends PacketAdapter {

	public PacketListener() {
		super(Sblock.getInstance(), PacketType.Play.Client.ENTITY_ACTION, PacketType.Play.Client.CHAT,
				PacketType.Status.Server.OUT_SERVER_INFO);
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

	/* (non-Javadoc)
	 * @see com.comphenix.protocol.events.PacketAdapter#onPacketSending(com.comphenix.protocol.events.PacketEvent)
	 */
	@Override
	public void onPacketSending(final PacketEvent event) {
		if (event.getPacketType() != PacketType.Status.Server.OUT_SERVER_INFO) {
			return;
		}
		final WrappedServerPing serverping = event.getPacket().getServerPings().read(0);
		if (serverping.getVersionProtocol() == 9999) {
			return;
		}

		// Causes client to see our custom message, cause woo! N.B. Does result in ping breaking and outdated client displaying.
		serverping.setVersionProtocol(9999);

		// Percent-based color: 0-49 = green, 50-74 = yellow, 75-100 = red
		int percent = serverping.getPlayersOnline() * 100 / serverping.getPlayersMaximum();
		ChatColor percentColor = percent > 75 ? ChatColor.RED : percent > 50 ? ChatColor.YELLOW : ChatColor.GREEN;

		String versionName = ChatColor.GOLD + "Sblock Alpha 1.7.10 " + percentColor + serverping.getPlayersOnline()
				+ ChatColor.DARK_GRAY + "/" + ChatColor.GREEN + serverping.getPlayersMaximum();
		serverping.setVersionName(versionName);

		event.setCancelled(true);

		Bukkit.getScheduler().runTaskAsynchronously(getPlugin(), new Runnable() {
			@Override
			public void run() {
				String addr = event.getPlayer().getAddress().getAddress().toString().substring(1);
				SblockData.getDB().getUserFromIP(addr, getMOTDSettingExecutor(event, serverping));
			}
		});
	}

	private final Promise<String> getMOTDSettingExecutor(final PacketEvent event, final WrappedServerPing serverping) {
		return new Promise<String>() {

			@Override
			public void getList(List<String> list) { }

			@Override
			public void getMap(Map<String, String> map) { }

			@Override
			public void getObject(String playerName, String unused) {
				serverping.setMotD(Bukkit.getMotd().replaceAll("Player", playerName));
				WrapperStatusServerOutServerInfo packet = new WrapperStatusServerOutServerInfo();
				packet.setServerPing(serverping);
				packet.sendPacket(event.getPlayer());
			}

			@Override
			public void getSet(Set<String> set) { }
		};
	}
}
