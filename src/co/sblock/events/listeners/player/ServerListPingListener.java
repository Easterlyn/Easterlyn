package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import co.sblock.events.Events;

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
		if (Events.getInstance().getStatus().hasMOTDChange()) {
			String MOTD = Events.getInstance().getStatus().getMOTDChange();
			event.setMotd(MOTD);
		}
		String addr = event.getAddress().getHostAddress();
		String playerName = Events.getInstance().getIPName(addr);
		if (playerName.equals("Player")) {
			// No thank you, spam from server monitors.
			return;
		}
		Events.getInstance().getLogger().info(playerName + " pinged the server from " + addr);
		event.setMotd(event.getMotd().replace("Player", playerName));
	}
}
