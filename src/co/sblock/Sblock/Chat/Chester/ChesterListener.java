package co.sblock.Sblock.Chat.Chester;

import java.util.HashSet;
import java.util.Set;

import info.gomeow.chester.API.ChesterBroadcastEvent;
import info.gomeow.chester.API.ChesterLogEvent;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.SblockChat;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Utilities.Log;

/**
 * @author Jikoo
 */
public class ChesterListener implements CommandListener, Listener {

	private int cancels = 0;

	@EventHandler
	public void onChesterLog(ChesterLogEvent event) {
		ChatUser c = ChatUser.getUser(event.getPlayer().getName());
		if (c == null || c.getCurrent() == null) {
			event.setCancelled(true);
			cancels++;
			new Log("ChesterListener", null).info("Not logging: No current channel. " + cancels + " trigger cancels.");
			return;
		}

		if (!c.getCurrent().getName().equals("#")) {
			if (!event.getMessage().startsWith("@# ")) {
				event.setCancelled(true);
				cancels++;
				new Log("ChesterListener", null).info("Not logging: Not talking to #. " + cancels + " trigger cancels.");
				return;
			}
		} else {
			if (event.getMessage().charAt(0) == '@' && !event.getMessage().startsWith("@# ")) {
				event.setCancelled(true);
				cancels++;
				new Log("ChesterListener", null).info("Not logging: Not talking to #. " + cancels + " trigger cancels.");
				return;
			}
		}

		if (event.getMessage().startsWith("@# ") && event.getMessage().length() > 3) {
			event.setMessage(event.getMessage().substring(3));
		}

		for (String s : SblockChat.chester) {
			if (event.getMessage().equalsIgnoreCase(s)) {
				new Log("ChesterListener", null).info("Not logging: Chat equals trigger \"" + event.getMessage() + "\"");
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
			new Log("ChesterListener", null).info("Chat cancelled when logged. " + cancels + " cancels remain.");
			return;
		}
		Channel c = ChannelManager.getChannelManager().getChannel("#");
		if (c == null) {
			event.getRecipients().clear();
			return;
		}
		Set<Player> listeners = new HashSet<Player>();
		for (String s : c.getListening()) {
			listeners.add(Bukkit.getPlayerExact(s));
		}
		event.getRecipients().retainAll(listeners);
	}

	// For now, anyone can use. Not broadcasting its existence, but not gonna prevent them from fixing him.
	@SblockCommand(description = "If Hal ain't talkin', here's how to come a-knockin'.", usage = "/fixchester")
	public boolean fixchester(CommandSender s, String[] args) {
		cancels = 0;
		s.sendMessage("Resetting cancels.");
		return true;
	}
}
