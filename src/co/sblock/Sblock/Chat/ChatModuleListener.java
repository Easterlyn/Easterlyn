package co.sblock.Sblock.Chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.player.PlayerQuitEvent;

import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class ChatModuleListener implements Listener {

	@EventHandler
	public void onPlayerLogin(PlayerLoginEvent event) {
		if (event.getResult() == Result.KICK_BANNED) {
			event.setKickMessage(new ChatStorage().getBan(event.getPlayer().getName()));
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onPlayerJoin(PlayerJoinEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			UserManager.getUserManager().addUser(event.getPlayer());
			u = SblockUser.getUser(event.getPlayer().getName());
		}
		Channel c = ChannelManager.getChannelList().get("#");
		if (!c.getListening().contains(u.getPlayerName())) {
			c.userJoin(u);
		}
		if (!u.getCurrent().equals(c)) {
			u.setCurrent(c);
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (SblockUser.getUser(event.getPlayer().getName()) != null) {
			event.setCancelled(true);
			if (event.getMessage().indexOf("/") == 0) {
				event.getPlayer().performCommand(
						event.getMessage().substring(1));
			} else {
				SblockUser.getUser(event.getPlayer().getName()).chat(event);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerQuit(PlayerQuitEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			return; // We don't want to make another db call just to announce quit.
		}
		for (String s : u.getListening()) {
			ChatModule.getInstance().getChannelManager().getChannel(s)
					.userLeave(u);
		}
		UserManager.getUserManager().removeUser(event.getPlayer());
	}

	/*
	 * @EventHandler public void onPlayerTagEvent(PlayerReceiveNameTagEvent
	 * event) { Player p = event.getNamedPlayer();
	 * 
	 * if (p.hasPermission("group.horrorterror")) {
	 * event.setTag(ColorDef.RANK_ADMIN + p.getName()); } else if
	 * (p.hasPermission("group.denizen")) { event.setTag(ColorDef.RANK_MOD +
	 * p.getName()); } else if (p.hasPermission("group.helper")) {
	 * event.setTag(ColorDef.RANK_HELPER + p.getName()); } else if
	 * (p.hasPermission("group.godtier")) { event.setTag(ColorDef.RANK_GODTIER +
	 * p.getName()); } else if (p.hasPermission("group.donator")) {
	 * event.setTag(ColorDef.RANK_DONATOR + p.getName()); } else if
	 * (p.hasPermission("group.hero")) { event.setTag(ColorDef.RANK_HERO +
	 * p.getName()); }
	 * 
	 * }
	 */
}
