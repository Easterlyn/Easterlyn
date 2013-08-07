package co.sblock.Sblock.Chat;

import java.util.logging.Logger;

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

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			UserManager.getUserManager().addUser(event.getPlayer());
		} else {
			Channel c = ChannelManager.getChannelList().get("#");
			u.setCurrent(c);
			c.userJoin(u);
			for (String s : u.getListening()) {
				ChannelManager.getChannelList().get(s).userJoin(u);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		Logger.getLogger("Minecraft").info("onPlayerChat");
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

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		SblockUser u = SblockUser.getUser(event.getPlayer().getName());
		if (u == null) {
			UserManager.getUserManager().addUser(event.getPlayer());
		} else {
			for (String s : u.getListening()) {
				ChatModule.getInstance().getChannelManager().getChannel(s)
						.userLeave(u);
			}
		}
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
