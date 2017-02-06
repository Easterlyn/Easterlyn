package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.EasterlynListener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

/**
 * Listener for ServerListPingEvents.
 * 
 * @author Jikoo
 */
public class ServerListPingListener extends EasterlynListener {

	private final Events events;

	public ServerListPingListener(Easterlyn plugin) {
		super(plugin);
		this.events = plugin.getModule(Events.class);
	}

	/**
	 * The event handler for ServerListPingEvents.
	 * <p>
	 * If the IP pinging has played before, customize MOTD with their name.
	 * 
	 * @param event the ServerListPingEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onServerListPing(ServerListPingEvent event) {
		if (events.getStatus().hasMOTDChange()) {
			String MOTD = events.getStatus().getMOTDChange();
			event.setMotd(MOTD);
		}
		String addr = event.getAddress().getHostAddress();
		String playerName = events.getIPName(addr);
		if (playerName.equals("Player")) {
			// No thank you, spam from server monitors.
			return;
		}
		events.getLogger().info(playerName + " pinged the server from " + addr);
		event.setMotd(event.getMotd().replace("Player", playerName));
	}

}
