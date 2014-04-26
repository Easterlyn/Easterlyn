package co.sblock.users;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;

import org.bukkit.ChatColor;

import co.sblock.chat.ChatMsgs;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelManager;
import co.sblock.chat.channel.ChannelType;
import co.sblock.machines.SblockMachines;
import co.sblock.utilities.regex.RegexUtils;

/**
 * ChatUser is the class used to alter all Chat-related data.
 * 
 * @author Jikoo, Dublek
 */
public class ChatData {

	/**
	 * Sets the Player's chat mute status and sends corresponding message.
	 * 
	 * @param user the User
	 * @param b true if the Player is being muted
	 */
	public static void setMute(User user, boolean b) {
		user.globalMute = b;
	}

	/**
	 * Gets the Player's mute status.
	 * 
	 * @param user the User
	 * @return true if the Player is muted
	 */
	public static boolean isMute(User user) {
		return user.globalMute;
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param user the User
	 * @param c the Channel to set as current
	 */
	public static void setCurrent(User user, Channel c) {
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel("null"), false);
			return;
		}
		if (c.isBanned(user)) {
			user.sendMessage(ChatMsgs.onUserBanAnnounce(user.getPlayerName(), c.getName()), false);
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(user)) {
			user.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()), false);
			return;
		}
		user.current = c.getName();
		if (!user.listening.contains(c.getName())) {
			ChatData.addListening(user, c);
		} else {
			user.sendMessage(ChatMsgs.onChannelSetCurrent(c.getName()), false);
		}
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param user the User
	 * @param c the Channel to set as current
	 */
	public static void setCurrent(User user, String s) {
		Channel c = ChannelManager.getChannelManager().getChannel(s);
		ChatData.setCurrent(user, c);
	}

	/**
	 * Gets the Channel the Player is currently sending messages to.
	 * 
	 * @param user the User
	 * 
	 * @return Channel
	 */
	public static Channel getCurrent(User user) {
		return ChannelManager.getChannelManager().getChannel(user.current);
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param user the User
	 * @param channel the Channel to add
	 * 
	 * @return true if the Channel was added
	 */
	public static boolean addListening(User user, Channel channel) {
		if (channel == null) {
			return false;
		}
		if (channel.isBanned(user)) {
			user.sendMessage(ChatMsgs.onUserBanAnnounce(user.getPlayerName(), channel.getName()), false);
			return false;
		}
		if (channel.getAccess().equals(AccessLevel.PRIVATE) && !channel.isApproved(user)) {
			user.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()), false);
			return false;
		}
		if (!user.listening.contains(channel)) {
			user.listening.add(channel.getName());
		}
		if (!channel.getListening().contains(user.getUUID())) {
			channel.addListening(user.getUUID());
			user.listening.add(channel.getName());
			channel.sendToAll(user, ChatMsgs.onChannelJoin(user, channel), false);
			return true;
		} else {
			user.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()), false);
			return false;
		}
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param user the User
	 * @param s the Channel name to add
	 * 
	 * @return true if the Channel was added
	 */
	public static boolean addListening(User user, String channelName) {
		Channel channel = ChannelManager.getChannelManager().getChannel(channelName);
		return addListening(user, channel);
	}

	/**
	 * Begin listening to a Set of channels. Used on login.
	 * 
	 * @param user the User
	 * @param channels
	 */
	public static void loginAddListening(User user, String[] channels) {
		for (String s : channels) {
			Channel c = ChannelManager.getChannelManager().getChannel(s);
			if (c != null && !c.isBanned(user)
					&& (c.getAccess() != AccessLevel.PRIVATE || c.isApproved(user))) {
				user.listening.add((String) s);
				c.addListening(user.getUUID());
			}
		}

		StringBuilder base = new StringBuilder(ChatColor.GREEN.toString())
				.append(user.getPlayer().getDisplayName()).append(ChatColor.YELLOW)
				.append(" began pestering <>").append(ChatColor.YELLOW).append(" at ")
				.append(new SimpleDateFormat("HH:mm").format(new Date()));
		// Heavy loopage ensues
		for (User u : UserManager.getUserManager().getUserlist()) {
			StringBuilder matches = new StringBuilder();
			for (String s : user.listening) {
				if (u.listening.contains(s)) {
					matches.append(ChatColor.GOLD).append(s).append(ChatColor.YELLOW).append(", ");
				}
			}
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.toString().replace("<>", matches.toString()));
				int comma = msg.toString().lastIndexOf(',');
				if (comma != -1) {
					u.sendMessage(msg.replace(comma, comma + 1, " and").toString(), false);
				}
			}
		}
	}

	/**
	 * Remove a Channel from the Player's listening List.
	 * 
	 * @param user the User
	 * @param cName the name of the Channel to remove
	 */
	public static void removeListening(User user, String cName) {
		Channel c = ChannelManager.getChannelManager().getChannel(cName);
		if (c == null) {
			user.sendMessage(ChatMsgs.errorInvalidChannel(cName), false);
			user.listening.remove(cName);
			return;
		}
		if (user.listening.remove(cName)) {
				c.sendToAll(user, ChatMsgs.onChannelLeave(user, c), false);
				c.removeListening(user.getUUID());
			if (cName.equals(user.current)) {
				user.current = null;
			}
		} else {
			user.sendMessage(ChatMsgs.errorNotListening(cName), false);
		}
	}

	/**
	 * Silently removes a Channel from the Player's listening list.
	 * 
	 * @param user the User
	 * @param channel the Channel to remove
	 */
	public static void removeListeningSilent(User user, Channel channel) {
		user.listening.remove(channel.getName());
		if (user.current != null && user.current.equals(channel.getName())) {
			user.current = null;
		}
		channel.removeListening(user.getUUID());
	}

	/**
	 * Tells a Channel the Player is leaving on quit.
	 * 
	 * @param user the User
	 * @param cName the name of the Channel to inform
	 */
	public static void removeListeningQuit(User user, String cName) {
		Channel c = ChannelManager.getChannelManager().getChannel(cName);
		if (c != null) {
			c.removeListening(user.getUUID());
		} else {
			user.listening.remove(cName);
		}
	}

	/**
	 * Gets the Set of names of Channels that the Player is listening to.
	 * 
	 * @param user the User
	 * 
	 * @return a Set<String> of Channel names.
	 */
	public static Set<String> getListening(User user) {
		return user.listening;
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param user the User
	 * @param c the Channel to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public static boolean isListening(User user, Channel c) {
		return user.listening.contains(c.getName());
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param user the User
	 * @param s the Channel name to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public static boolean isListening(User user, String s) {
		return user.listening.contains(s);
	}

	public static boolean getComputerAccess(User user) {
		if (!SblockChat.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		return SblockMachines.getMachines().getManager().isByComputer(user.getPlayer(), 10);
	}

	/**
	 * Method for handling all Player chat.
	 * 
	 * @param user the User
	 * @param msg the message being sent
	 * @param forceThirdPerson true if the message is to be prepended with a modifier
	 */
	public static void chat(User user, String msg, boolean forceThirdPerson) {

		// Check if the user can speak
		if (user.globalMute) {
			user.sendMessage(ChatMsgs.isMute(), false);
			return;
		}

		// default to current channel receiving message
		Channel sendto = ChannelManager.getChannelManager().getChannel(user.current);

		// check if chat is directed at another channel
		int space = msg.indexOf(' ');
		if (msg.indexOf("@") == 0 && space > 1) {
			// Check for alternate channel destination. Failing that, warn user.
			String newChannel = msg.substring(1, space);
			if (ChannelManager.getChannelManager().isValidChannel(newChannel)) {
				sendto = ChannelManager.getChannelManager().getChannel(newChannel);
				if (sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(user)) {
					// User not approved in channel
					user.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sendto.getName()), false);
					return;
				} else {
					// should reach this point for public channels and approved users
					msg = msg.substring(space + 1);
					if (msg.length() == 0) {
						// Do not display blank messages for @<channel> with no message
						return;
					}
				}
			} else {
				// Invalid channel specified
				user.sendMessage(ChatMsgs.errorInvalidChannel(newChannel), false);
				return;
			}
		} else if (sendto == null) {
			user.sendMessage(ChatMsgs.errorNoCurrent(), false);
			return;
		}
		
		if (sendto.getType() == ChannelType.RP && !sendto.hasNick(user)) {
			user.sendMessage(ChatMsgs.errorNickRequired(sendto.getName()), false);
			return;
		}

		// Trim whitespace created by formatting codes, etc.
		msg = RegexUtils.trimExtraWhitespace(msg);
		if (RegexUtils.appearsEmpty(msg.substring(0 , 2).equals("#>") ? msg.substring(2) : msg)) {
			return;
		}

		// Chat is being done via /me
		if (forceThirdPerson) {
			msg = "#>" + msg;
		}

		sendto.sendToAll(user, msg, true);
	}
}
