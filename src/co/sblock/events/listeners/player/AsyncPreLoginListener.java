package co.sblock.events.listeners.player;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import co.sblock.events.Events;
import co.sblock.micromodules.Slack;

/**
 * Proxy detection, because apparently this is an issue.
 * 
 * @author Jikoo
 */
public class AsyncPreLoginListener implements Listener {

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		try {
			final String ip = event.getAddress().getHostAddress();
			final String[] split = ip.split("\\.");
			final StringBuilder lookup = new StringBuilder();
			for (int i = split.length - 1; i >= 0; --i) {
				lookup.append(split[i]);
				lookup.append(".");
			}
			if (!ip.contains("127.0.0.1")) {
				lookup.append("xbl.spamhaus.org.");
				if (InetAddress.getByName(lookup.toString()) != null) {
					event.setLoginResult(Result.KICK_OTHER);
					event.setKickMessage("Your IP address is flagged as unsafe by the Spamhaus XBL."
							+"\n\nPlease visit https://www.spamhaus.org/query/ip/" + ip + " to learn why.");
					Slack.getInstance().postReport(null, event.getUniqueId(),
							ip + " is flagged as unsafe by spamhaus.org/xbl, disconnecting " + event.getUniqueId());
					return;
				}
			}
		} catch (Exception ex) {}
		Collection<String> ips = Events.getInstance().getIPsFor(event.getUniqueId());
		if (ips.size() > 1) {
			OfflinePlayer offline = Bukkit.getOfflinePlayer(event.getUniqueId());
			String name = offline.getName() != null ? offline.getName() : event.getUniqueId().toString();
			Bukkit.getConsoleSender().sendMessage( ips.size() + " IPs on record for " + name + ": " + StringUtils.join(ips.toArray(new String[ips.size()]), ", "));
		}
	}
}
