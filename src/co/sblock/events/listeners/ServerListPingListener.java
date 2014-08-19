package co.sblock.events.listeners;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

import com.tmathmeyer.jadis.async.Promise;

import co.sblock.data.SblockData;
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
		} else {
			String addr = event.getAddress().getHostAddress();
			SblockData.getDB().getUserFromIP(addr, this.getMOTDSettingExecutor(event, event.getMotd()));
		}
	}

	private final Promise<String> getMOTDSettingExecutor(final ServerListPingEvent event, final String MOTD) {
		return new Promise<String>() {

			@Override
			public void getList(List<String> list) { }

			@Override
			public void getMap(Map<String, String> map) { }

			@Override
			public void getObject(String playerName, String unused) {
				event.setMotd(MOTD.replaceAll("Player", playerName));
			}

			@Override
			public void getSet(Set<String> set) { }
			
		};
	}
}
