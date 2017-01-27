package com.easterlyn.events.listeners.player;

import java.util.UUID;

import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.discord.Discord;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.events.packets.WrapperPlayServerPlayerListHeaderFooter;
import com.easterlyn.users.Region;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class JoinListener extends SblockListener {

	private final Discord discord;
	private final Events events;
	private final Users users;
	private final WrapperPlayServerPlayerListHeaderFooter list;

	public JoinListener(Easterlyn plugin) {
		super(plugin);
		this.discord = plugin.getModule(Discord.class);
		this.events = plugin.getModule(Events.class);
		this.users = plugin.getModule(Users.class);
		Language lang = plugin.getModule(Language.class);
		this.list = new WrapperPlayServerPlayerListHeaderFooter();
		this.list.setHeader(WrappedChatComponent.fromText(lang.getValue("events.join.tab.header")));
		this.list.setFooter(WrappedChatComponent.fromText(lang.getValue("events.join.tab.footer")));
	}

	/**
	 * The event handler for PlayerJoinEvents.
	 * 
	 * @param event the PlayerJoinEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent event) {
		final boolean announce = event.getJoinMessage() != null;
		event.setJoinMessage(null);
		users.getUser(event.getPlayer().getUniqueId());

		events.addCachedIP(event.getPlayer().getAddress().getHostString(), event.getPlayer().getName());

		final UUID uuid = event.getPlayer().getUniqueId();

		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					return;
				}

				discord.postMessage(discord.getBotName(), player.getDisplayName() + " logs in.", true);

				User user = users.getUser(player.getUniqueId());

				Location teleport = user.getLoginLocation();
				if (teleport != null) {
					player.teleport(teleport);
					user.setLoginLocation(null);
				}
				user.handleLoginChannelJoins(announce);
				user.handleNameChange();
				Region region = Region.getRegion(player.getWorld().getName());
				if (region.isDream()) {
					Region old = user.getCurrentRegion();
					region = old.isDream() ? old : user.getDreamPlanet();
				}
				user.updateCurrentRegion(region, true);
				user.updateFlight();
				events.getInvisibilityManager().updateVisibility(player);
				for (String command : user.getLoginCommands()) {
					player.chat(command);
				}

				list.sendPacket(player);
			}
		}.runTaskLater(getPlugin(), 2L);
	}

}
