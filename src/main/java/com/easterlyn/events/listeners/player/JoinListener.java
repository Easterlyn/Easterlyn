package com.easterlyn.events.listeners.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.discord.Discord;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * Listener for PlayerJoinEvents.
 *
 * @author Jikoo
 */
public class JoinListener extends EasterlynListener {

	private final Discord discord;
	private final Events events;
	private final Users users;
	private final PacketContainer list;

	public JoinListener(Easterlyn plugin) {
		super(plugin);
		this.discord = plugin.getModule(Discord.class);
		this.events = plugin.getModule(Events.class);
		this.users = plugin.getModule(Users.class);
		Language lang = plugin.getModule(Language.class);
		this.list = new PacketContainer(PacketType.Play.Server.PLAYER_LIST_HEADER_FOOTER);
		this.list.getChatComponents().write(0, WrappedChatComponent.fromText(lang.getValue("events.join.tab.header")));
		this.list.getChatComponents().write(1, WrappedChatComponent.fromText(lang.getValue("events.join.tab.footer")));
	}

	/**
	 * The event handler for PlayerJoinEvents.
	 *
	 * @param event the PlayerJoinEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerJoin(final PlayerJoinEvent event) {

		PlayerUtils.removeFromCache(event.getPlayer().getUniqueId());

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

				discord.postMessage(null, player.getDisplayName() + " logs in.", true);

				User user = users.getUser(player.getUniqueId());

				Location teleport = user.getLoginLocation();
				if (teleport != null) {
					player.teleport(teleport);
					user.setLoginLocation(null);
				}
				user.handleLoginChannelJoins(announce);
				user.handleNameChange();
				user.updateFlight();
				events.getInvisibilityManager().updateVisibility(player);
				for (String command : user.getLoginCommands()) {
					player.chat(command);
				}
				try {
					ProtocolLibrary.getProtocolManager().sendServerPacket(player, list);
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
		}.runTaskLater(getPlugin(), 2L);
	}

}
