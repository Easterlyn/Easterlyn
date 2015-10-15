package co.sblock.events.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.wrappers.WrappedChatComponent;

import co.sblock.Sblock;
import co.sblock.events.Events;
import co.sblock.events.packets.WrapperPlayServerPlayerListHeaderFooter;
import co.sblock.micromodules.Godule;
import co.sblock.micromodules.Slack;
import co.sblock.users.OnlineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for PlayerJoinEvents.
 * 
 * @author Jikoo
 */
public class JoinListener implements Listener {

	private final WrapperPlayServerPlayerListHeaderFooter list;

	public JoinListener() {
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
		Users.getGuaranteedUser(event.getPlayer().getUniqueId());

		Events.getInstance().addCachedIP(event.getPlayer().getAddress().getHostString(), event.getPlayer().getName());

		Slack.getInstance().postMessage(event.getPlayer().getName(), event.getPlayer().getUniqueId(),
				event.getPlayer().getName() + " logs in.", true);

		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = event.getPlayer();
				if (player == null) {
					return;
				}
				Users.team(player);
				OnlineUser user = Users.getGuaranteedUser(player.getUniqueId()).getOnlineUser();
				user.handleLoginChannelJoins();
				user.handleNameChange();
				Region region = user.getCurrentRegion();
				user.updateCurrentRegion(region);
				// On login, conditions for setting rpack are not met, must be done here
				player.setResourcePack(region.getResourcePackURL());
				user.updateFlight();
				Events.getInstance().getInvisibilityManager().updateVisibility(player);
				for (String command : user.getLoginCommands()) {
					player.chat(command);
				}

				list.sendPacket(player);

				if (player.hasPermission("sblock.god")) {
					Godule.getInstance().enable(user.getUserAspect());
				}
			}
		}.runTask(Sblock.getInstance());
	}
}
