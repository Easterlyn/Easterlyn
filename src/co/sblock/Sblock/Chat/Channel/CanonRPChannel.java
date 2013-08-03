/**
 * 
 */
package co.sblock.Sblock.Chat.Channel;

import org.bukkit.ChatColor;

import co.sblock.Sblock.UserData.SblockUser;

/**
 * @author Jikoo
 * 
 */
public class CanonRPChannel extends RPChannel {

	/**
	 * @param name
	 *            the channel name
	 * @param a
	 *            the access level of the channel
	 * @param creator
	 *            the channel's creator
	 */
	public CanonRPChannel(String name, AccessLevel a, String creator) {
		super(name, a, creator);
	}

	@Override
	public void setNick(String nick, SblockUser sender) {
		if (CanonNicks.getNick(nick).name().equals(CanonNicks.CUSTOM.name())) {
			sender.sendMessage(ChatColor.BLUE + nick + ChatColor.RED +
					" is not a valid roleplaying nick!");
		} else {
			this.nickList.put(nick, sender.getPlayerName());
			sender.sendMessage(ChatColor.YELLOW + "Your nick has been set to \""
					+ ChatColor.BLUE + nick + "\" in "
					+ ChatColor.GOLD + this.getName());
		}
	}
}
