package co.sblock.events.listeners.player;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.WrappedChatComponent;

import co.sblock.Sblock;
import co.sblock.discord.Discord;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;
import co.sblock.events.packets.WrapperPlayServerPlayerListHeaderFooter;
import co.sblock.micromodules.Godule;
import co.sblock.users.OnlineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class JoinListener extends SblockListener {

	private final Discord discord;
	private final Events events;
	private final WrapperPlayServerPlayerListHeaderFooter list;

	public JoinListener(Sblock plugin) {
		super(plugin);
		this.discord = plugin.getModule(Discord.class);
		this.events = plugin.getModule(Events.class);
		list = new WrapperPlayServerPlayerListHeaderFooter();
		list.setHeader(WrappedChatComponent.fromText(ChatColor.DARK_AQUA + "Welcome to " + ChatColor.GOLD + "Sblock Alpha"));
		list.setFooter(WrappedChatComponent.fromText(ChatColor.YELLOW + "Enjoy your stay!"));
	}

	/**
	 * The event handler for PlayerJoinEvents.
	 * 
	 * @param event the PlayerJoinEvent
	 */
	@EventHandler
	public void onPlayerJoin(final PlayerJoinEvent event) {
		event.setJoinMessage(null);
		// CHAT: check message beforehand and don't announce channels if muted
		Users.getGuaranteedUser(getPlugin(), event.getPlayer().getUniqueId());

		events.addCachedIP(event.getPlayer().getAddress().getHostString(), event.getPlayer().getName());

		discord.postMessage(event.getPlayer().getName(),
				event.getPlayer().getName() + " logs in.", true);

		final UUID uuid = event.getPlayer().getUniqueId();

		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					return;
				}
				Users.team(player);
				OnlineUser user = Users.getGuaranteedUser(getPlugin(), player.getUniqueId()).getOnlineUser();
				user.handleLoginChannelJoins();
				user.handleNameChange();
				Region region = user.getCurrentRegion();
				user.updateCurrentRegion(region);
				// On login, conditions for setting rpack are not met, must be done here
				player.setResourcePack(region.getResourcePackURL());
				user.updateFlight();
				events.getInvisibilityManager().updateVisibility(player);
				for (String command : user.getLoginCommands()) {
					player.chat(command);
				}

				list.sendPacket(player);

				if (player.hasPermission("sblock.god")) {
					Godule.getInstance().enable(user.getUserAspect());
				}
			}
		}.runTaskLater(getPlugin(), 2L);
	}
}
