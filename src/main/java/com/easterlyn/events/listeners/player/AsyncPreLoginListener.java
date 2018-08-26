package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.discord.Discord;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.player.PermissionBridge;
import com.easterlyn.utilities.player.PermissionUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent.Result;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Proxy detection, because apparently this is an issue.
 *
 * @author Jikoo
 */
public class AsyncPreLoginListener extends EasterlynListener {

	private final Discord discord;
	private final Events events;
	private final Cache<String, Boolean> ipCache;

	public AsyncPreLoginListener(Easterlyn plugin) {
		super(plugin);
		PermissionUtils.addParent("easterlyn.events.login.proxy", UserRank.MEMBER.getPermission());

		this.discord = plugin.getModule(Discord.class);
		this.events = plugin.getModule(Events.class);

		this.ipCache = CacheBuilder.newBuilder().expireAfterWrite(1L, TimeUnit.HOURS).weakKeys()
				.weakValues().build();
	}

	@EventHandler
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
		final String ip = event.getAddress().getHostAddress();
		Boolean allowed = ipCache.getIfPresent(ip);
		if (allowed == null) {
			allowed = isAllowed(ip, event.getUniqueId());
			ipCache.put(ip, allowed);
		}
		if (!allowed) {
			if (allowed = PermissionBridge.hasPermission(event.getUniqueId(), "easterlyn.events.login.proxy")) {
				// Players with permission can allow a blocked IP for 30 minutes by logging in from it
				ipCache.put(ip, true);
			}
		}
		if (!allowed) {
			event.setLoginResult(Result.KICK_OTHER);
			event.setKickMessage("Your IP address is flagged as unsafe by the Spamhaus XBL."
					+"\nPlease visit https://www.spamhaus.org/query/ip/" + ip + " to learn why."
					+ "\n\nIf you have a dynamic IP and this is a recurring problem,"
					+ "\nplease create a /report at your earliest convenience.");
			return;
		}
		Collection<String> ips = events.getIPsFor(event.getUniqueId());
		if (ips.size() > 1) {
			OfflinePlayer offline = Bukkit.getOfflinePlayer(event.getUniqueId());
			String name = offline.getName() != null ? offline.getName() : event.getUniqueId().toString();
			Bukkit.getConsoleSender().sendMessage( ips.size() + " IPs on record for " + name
					+ ": " + StringUtils.join(ips.toArray(new String[0]), ", "));
		}
	}

	private boolean isAllowed(String ip, UUID uuid) {
		if (ip.contains("127.0.0.1")) {
			return true;
		}
		if (this.events.getSpamhausWhitelist().contains(ip)) {
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
		discord.postReport(ip + " is flagged as unsafe by spamhaus.org/xbl, disconnecting " + uuid);
		return true;
	}

}
