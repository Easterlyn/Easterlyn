package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import co.sblock.events.SblockEvents;

/**
 * Listener for ServerListPingEvents.
 * 
 * @author Jikoo
 */
public class ServerListPingListener implements Listener {

	/**
	 * The event handler for ServerListPingEvents.
	 * <p>
	 * If the IP pinging has played before, customize MOTD with their name.
	 * 
	 * @param event the ServerListPingEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onServerListPing(ServerListPingEvent event) {
		if (SblockEvents.getEvents().getStatus().hasMOTDChange()) {
			String MOTD = SblockEvents.getEvents().getStatus().getMOTDChange();
			event.setMotd(MOTD);
		}
		String addr = event.getAddress().getHostAddress();
		String playerName = SblockEvents.getEvents().getIPName(addr);
		SblockEvents.getEvents().getLogger().info(playerName + " pinged the server from " + addr);
		event.setMotd(event.getMotd().replace("Player", playerName));
	}
}
