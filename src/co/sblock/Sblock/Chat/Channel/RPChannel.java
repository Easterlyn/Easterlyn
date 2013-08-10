package co.sblock.Sblock.Chat.Channel;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Jikoo
 *
 */
public class RPChannel extends NickChannel {

	public RPChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	@Override
	public ChannelType getType() {
		return ChannelType.RP;
	}

	@Override
	public void setNick(String nick, SblockUser sender) {
		if (!Nick.isCanon(nick)) {
			sender.sendMessage(ChatColor.BLUE + nick + ChatColor.RED +
					" is not a valid roleplaying nick!");
		} else {
			this.nickList.put(nick, sender.getPlayerName());
			sender.sendMessage(ChatColor.YELLOW + "Your nick has been set to \""
					+ ChatColor.BLUE + nick + "\" in "
					+ ChatColor.GOLD + this.getName());
		}
	}

	@Override
	public void sendToAll(SblockUser sender, String s, String type) {
		if (this.getNick(sender) == null) {
			sender.sendMessage(ChatMsgs.errorNickRequired(this.getName()));
			return;
		}
		Set<String> failures = new HashSet<String>();
		for (String name : this.listening) {
			SblockUser u = UserManager.getUserManager().getUser(name);
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