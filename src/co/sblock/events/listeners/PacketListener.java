package co.sblock.events.listeners;

import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedServerPing;

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
				PacketType.Play.Client.TAB_COMPLETE, PacketType.Status.Server.OUT_SERVER_INFO);
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
					&& SblockEvents.getEvents().getTasks().containsKey(event.getPlayer().getUniqueId())) {
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
		if (event.getPacketType() == PacketType.Status.Server.OUT_SERVER_INFO) {
			final WrappedServerPing serverping = event.getPacket().getServerPings().read(0);
	
			// Causes client to see our custom message, cause woo! N.B. Does result in ping breaking and outdated client displaying.
			serverping.setVersionProtocol(9999);
	
			// Percent-based color: 0-49 = green, 50-74 = yellow, 75-100 = red
			int percent = serverping.getPlayersOnline() * 100 / serverping.getPlayersMaximum();
			ChatColor percentColor = percent > 75 ? ChatColor.RED : percent > 50 ? ChatColor.YELLOW : ChatColor.GREEN;
	
			String versionName = ChatColor.GOLD + "Sblock Alpha" + ChatColor.DARK_GRAY + ": "
					+ ChatColor.GRAY + "1.7.10, 1.8" + ChatColor.DARK_GRAY + " - " + percentColor
					+ serverping.getPlayersOnline() + ChatColor.DARK_GRAY + "/" + ChatColor.GREEN
					+ serverping.getPlayersMaximum();
			serverping.setVersionName(versionName);
	
			String addr = event.getPlayer().getAddress().getAddress().toString().substring(1);
			String playerName = SblockEvents.getEvents().getIPName(addr);
			SblockEvents.getEvents().getLogger().info(playerName + " pinged the server from " + addr);
			serverping.setMotD(Bukkit.getMotd().replaceAll("Player", playerName));
			event.getPacket().getServerPings().write(0, serverping);
		}
	}
}
