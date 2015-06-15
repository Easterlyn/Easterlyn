package co.sblock.events.listeners.player;

import java.net.InetAddress;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import co.sblock.events.Events;
import co.sblock.utilities.messages.Slack;

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
					event.setKickMessage("Your IP address (" + ip + ") is flagged as unsafe by spamhaus.org/xbl"
							+"\n");
					Slack.getInstance().postReport(null, event.getUniqueId(),
							ip + " is flagged as unsafe by spamhaus.org/xbl, disconnecting " + event.getUniqueId());
					return;
				}
			}
		} catch (Exception ex) {}
		Collection<String> ips = Events.getInstance().getIPsFor(event.getUniqueId());
		if (ips.size() > 1) {
			Slack.getInstance().postReport(null, event.getUniqueId(), ips.size() + " IPs on record: " + StringUtils.join(ips.toArray(new String[ips.size()])));
		}
	}
}
