package co.sblock.events.listeners.player;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import co.sblock.discord.Discord;
import co.sblock.events.Events;
import co.sblock.utilities.PermissionBridge;

/**
 * Proxy detection, because apparently this is an issue.
 * 
 * @author Jikoo
 */
public class AsyncPreLoginListener implements Listener {

	private final Cache<String, Boolean> ipCache;

	public AsyncPreLoginListener() {
		Permission permission;
		try {
			permission = new Permission("sblock.login.proxy", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.login.proxy");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.helper", true).recalculatePermissibles();

		ipCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).weakKeys()
				.weakValues().build();
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		final String ip = event.getAddress().getHostAddress();
		Boolean allowed = ipCache.getIfPresent(ip);
		if (allowed == null) {
			allowed = isAllowed(ip);
			ipCache.put(ip, allowed);
		}
		if (!allowed) {
			allowed =  PermissionBridge.getInstance().hasPermission(event.getUniqueId(), "sblock.login.proxy");
			if (allowed) {
				// Players with permission can allow a blocked IP for 30 minutes by logging in from it
				ipCache.put(ip, allowed);
			}
		}
		Collection<String> ips = Events.getInstance().getIPsFor(event.getUniqueId());
		if (ips.size() > 1) {
			OfflinePlayer offline = Bukkit.getOfflinePlayer(event.getUniqueId());
			String name = offline.getName() != null ? offline.getName() : event.getUniqueId().toString();
			Bukkit.getConsoleSender().sendMessage( ips.size() + " IPs on record for " + name
					+ ": " + StringUtils.join(ips.toArray(new String[ips.size()]), ", "));
		}
		if (!allowed) {
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage("Your IP address is flagged as unsafe by the Spamhaus XBL."
					+"\nPlease visit https://www.spamhaus.org/query/ip/" + ip + " to learn why."
					+ "\n\nIf you have a dynamic IP and this is a recurring problem,"
					+ "\nplease create a /report at your earliest convenience.");
			Discord.getInstance().postReport(event.getUniqueId().toString(),
					ip + " is flagged as unsafe by spamhaus.org/xbl, disconnecting " + event.getUniqueId());
			return;
		}
	}

	private boolean isAllowed(String ip) {
		if (ip.contains("127.0.0.1")) {
			return true;
		}
		final String[] split = ip.split("\\.");
		final StringBuilder lookup = new StringBuilder();
		for (int i = split.length - 1; i >= 0; --i) {
			lookup.append(split[i]);
			lookup.append(".");
		}
		lookup.append("xbl.spamhaus.org.");
		try {
			if (InetAddress.getByName(lookup.toString()) != null) {
				return false;
			}
		} catch (UnknownHostException e) {
			return true;
		}
		return true;
	}
}
