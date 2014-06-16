package co.sblock.events.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;

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
        String MOTD;
        if (SblockEvents.getEvents().getStatus().hasMOTDChange()) {
            MOTD = SblockEvents.getEvents().getStatus().getMOTDChange();
        } else {
            MOTD = event.getMotd().replaceAll("Player",
                    SblockData.getDB().getUserFromIP(event.getAddress().getHostAddress()));
        }
        event.setMotd(MOTD);
    }
}
