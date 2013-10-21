package co.sblock.Sblock.Chat.Channel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ChatUser;
import co.sblock.Sblock.Chat.ChatUserManager;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Jikoo
 *
 */
public class RPChannel extends NickChannel {

	private Map<ChatUser, String> nickList = new HashMap<ChatUser, String>();

	public RPChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	@Override
	public void setNick(ChatUser sender, String nick) {		
		if (CanonNicks.getNick(nick) == null) {
			sender.sendMessage(ChatColor.BLUE + nick + ChatColor.RED +
					" is not a valid roleplaying nick!");
			return;
		} 
		for(String n : nickList.values())	{
			if (n.equalsIgnoreCase(nick))	{
				sender.sendMessage(ChatColor.BLUE + nick + ChatColor.RED + " is already in use!");
				return;
			}
		}		
		nickList.put(sender, nick);
		sender.sendMessage(ChatColor.YELLOW + "Your nick has been set to \""
				+ ChatColor.BLUE + nick + "\" in "
				+ ChatColor.GOLD + this.getName());
		
		for(String user : this.getListening()){
			ChatUserManager.getUserManager().getUser(user).sendMessage(ChatMsgs.onUserSetNick(sender.getPlayerName(), nick, this));
		}
	}

	@Override
	public void sendToAll(ChatUser sender, String s, String type) {
		if (this.getNick(sender) == null) {
			sender.sendMessage(ChatMsgs.errorNickRequired(this.getName()));
			return;
		}
		Set<String> failures = new HashSet<String>();
		for (String name : this.listening) {
			ChatUser u = ChatUserManager.getUserManager().getUser(name);
			if (u != null) {
				u.sendMessageFromChannel(s, this, type);
			} else {
				failures.add(name);
			}
		}
		for (String failure : failures) {
			this.listening.remove(failure);
		}
		Sblogger.infoNoLogName(s);
	}
}