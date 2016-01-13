package co.sblock.events.listeners.player;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

import co.sblock.Sblock;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for ServerListPingEvents.
 * 
 * @author Jikoo
 */
public class ServerListPingListener extends SblockListener {

	private final Events events;

	public ServerListPingListener(Sblock plugin) {
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
