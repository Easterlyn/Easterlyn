package co.sblock.Sblock.Chat.Chester;

import java.util.HashSet;
import java.util.Set;

import info.gomeow.chester.API.ChesterBroadcastEvent;
import info.gomeow.chester.API.ChesterLogEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.SblockChat;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;

/**
 * @author Jikoo
 */
public class ChesterListener implements Listener {

	private int cancels = 0;

	@EventHandler
	public void onChesterLog(ChesterLogEvent event) {
		ChatUser c = ChatUser.getUser(event.getPlayer().getName());
		if (c == null) {
			event.setCancelled(true);
			cancels++;
			return;
		}
		if (c.getCurrent() != null && !c.getCurrent().getName().equals("#")
				|| !event.getMessage().startsWith("@# ")) {
			event.setCancelled(true);
			cancels++;
			return;
		}
		if (event.getMessage().startsWith("@# ") && event.getMessage().length() > 3) {
			event.setMessage(event.getMessage().substring(3));
		}
		for (String s : SblockChat.chester) {
			if (event.getMessage().equalsIgnoreCase(s)) {
				event.setCancelled(true);
				return;
			}
		}
	}

	@EventHandler
	public void onChesterTalk(ChesterBroadcastEvent event) {
		if (cancels > 0) {
			event.getRecipients().clear();
			cancels--;
			return;
		}
		Channel c = ChannelManager.getChannelManager().getChannel("#");
		if (c == null) {
			event.getRecipients().clear();
		}
		Set<Player> listeners = new HashSet<Player>();
		for (String s : c.getListening()) {
			listeners.add(Bukkit.getPlayerExact(s));
		}
		event.getRecipients().retainAll(listeners);
	}
}
