package co.sblock.Sblock.Chat2;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ColorDef;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.CanonNicks;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

public class ChatUser {

	/**
	 * Method for handling all <code>Player</code> chat.
	 * 
	 * @param event
	 *            the relevant <code>AsyncPlayerChatEvent</code>
	 */
	public void chat(AsyncPlayerChatEvent event) {
		// receives message from SblockChatListener
		// determine channel. if message doesn't begin with @$channelname, then
		// this.current confirm destination channel

		// confirm user has perm to send to channel (channel.cansend()) and also
		// muteness
		// output of channel, string

		SblockUser sender = UserManager.getUserManager().getUser(event.getPlayer().getName());
		String fullmsg = event.getMessage();
		String outputmessage = fullmsg;
		Channel sendto = ChatModule.getChatModule().getChannelManager().getChannel(sender.current);

		if (sender.isMute()) {
			sender.sendMessage(ChatMsgs.isMute());
			return;
		}

		if (fullmsg.indexOf("@") == 0) { // Check for alternate channel destination
			int space = fullmsg.indexOf(" ");
			String newChannel = fullmsg.substring(1, space);
			sender.sendMessage("\"" + newChannel + "\"");
			if (ChatModule.getChatModule().getChannelManager().isValidChannel(newChannel)) {
				sendto = ChatModule.getChatModule().getChannelManager().getChannel(newChannel);
				if (sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(sender)) {
					// User not approved in channel
					sender.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sender, sendto));
					return;
				} else {
					// should reach this point for publicchannel and approved users
					outputmessage = fullmsg.substring(space + 1);
				}
			} else {
				// invalidChannel
				sender.sendMessage(ChatMsgs.errorInvalidChannel(newChannel));
				return;
			}
		} else if (current == null) {
			sender.sendMessage(ChatMsgs.errorNoCurrent());
			return;
		}
		this.formatMessage(sender, sendto, outputmessage);
	}

	/**
	 * Format a chat message for sending to a <code>Channel</code>.
	 * 
	 * @param sender
	 *            the <code>SblockUser</code> speaking
	 * @param c
	 *            the <code>Channel</code> to send the message to
	 * @param s
	 *            the message to send
	 */
	public void formatMessage(SblockUser sender, Channel c, String s) {
		// remember, [$channel]<$player> $message

		// perhaps call getOutputChannelF and getOutputNameF?
		// though I should def include a ColorDefinitons class -- DONE

		// check for a global nick, prolly only occurs if admin is being
		// tricksty

		// next add or strip colors in message. based on perm
		// this part may change as I start working on other channeltypes
		// check for thirdperson # modifier and reformat appropriately
		// finally, channel.sendtochannel

		String channelF = "";
		String nameF = "";
		String output = "";
		// colorformatting

		boolean isThirdPerson = false;
		isThirdPerson = (s.indexOf("#") == 0) ? true : false;

		if (!isThirdPerson) {
			channelF = this.getOutputChannelF(sender, c);
		}
		if (isThirdPerson) {
			s = s.substring(1);
		}
		nameF = this.getOutputNameF(sender, isThirdPerson, c);
		output = channelF + nameF + s;
		// sender.getPlayer().sendMessage(output);
		// This bypass will remain as long as the stupid
		// thing can't tell what it's listening to

		if (c.isChannelMod(sender)) {
			output = ChatColor.translateAlternateColorCodes('\u0026', output);
		}

		if (isThirdPerson) {
			c.sendToAll(sender, output, "me");
		} else {
			c.sendToAll(sender, output, "chat");
		}

	}

	/**
	 * Send a message from a <code>Channel</code> to this <code>Player</code>.
	 * 
	 * @param s
	 *            the message to send
	 * @param c
	 *            the <code>Channel</code> to send to.
	 * @param type
	 *            the type of chat for handling purposes
	 */
	@SuppressWarnings("deprecation")
	public void sendMessageFromChannel(String s, Channel c, String type) {
		// final output, sends message to user
		// alert for if its player's name is applied here i.e. {!}
		// then just send it and be done!
		switch (type) {
		case "chat":
			if (ChatColor.stripColor(s).toLowerCase()
					.indexOf(this.getPlayerName().toLowerCase()) > s
					.indexOf(">")) {
				String output = "";
				output = s.substring(0, s.indexOf("]") + 1) + ChatColor.BLUE
						+ "{!}"
						+ s.substring(s.indexOf("<"), s.indexOf(">") + 1)
						+ ChatColor.WHITE + s.substring(s.indexOf(">") + 1);
				this.getPlayer().sendMessage(output);
				this.getPlayer().playEffect(this.getPlayer().getLocation(),
						Effect.BOW_FIRE, 0);
			} else {
				this.getPlayer().sendMessage(s);
			}
			break;
		case "me":
		case "channel":
		default:
			this.getPlayer().sendMessage(s);
			break;
		}
		// this.getPlayer().sendMessage(s);
	}

	// Here begins output formatting. Abandon all hope ye who enter

	/**
	 * Gets chat prefixing based on conditions.
	 * 
	 * @param sender
	 *            the <code>SblockUser</code> sending the message
	 * @param channel
	 *            the <code>Channel</code> receiving the message
	 * @return the prefix for specified conditions
	 */
	public String getOutputChannelF(SblockUser sender, Channel channel) {
		// colors for [$channel] applied here
		// SburbChat code. Handle with care
		String out = "";

		ChatColor color = ColorDef.CHATRANK_MEMBER;
		if (channel.isOwner(sender)) {
			color = ColorDef.CHATRANK_OWNER;
		} else if (channel.isChannelMod(sender)) {
			color = ColorDef.CHATRANK_MOD;
		}
		out = ChatColor.WHITE + "[" + color + channel.getName()
				+ ChatColor.WHITE + "] ";
		// sender.getPlayer().sendMessage(out);
		return out;
	}

	/**
	 * Gets chat prefixing based on conditions.
	 * 
	 * @param sender
	 *            the <code>SblockUser</code> sending the message
	 * @param isThirdPerson
	 *            whether or not to provide a third person prefix
	 * @param channel
	 *            the <code>Channel</code> receiving the message
	 * @return the prefix for specified conditions
	 */
	public String getOutputNameF(SblockUser sender, boolean isThirdPerson,
			Channel c) {
		// colors for <$name> applied here
		// SburbChat code. Handle with care
		String out = "";
		String outputName = sender.getPlayerName();
		if(c.hasNick(sender))	{
			outputName = c.getNick(sender);
		}
		ChatColor colorP = ColorDef.RANK_HERO;
		ChatColor colorW = ColorDef.DEFAULT;

		if (sender.getPlayer().hasPermission("group.horrorterror"))
			colorP = ColorDef.RANK_ADMIN;
		else if (sender.getPlayer().hasPermission("group.denizen"))
			colorP = ColorDef.RANK_MOD;
		else if (sender.getPlayer().hasPermission("group.helper"))
			colorP = ColorDef.RANK_HELPER;
		else if (sender.getPlayer().hasPermission("group.godtier"))
			colorP = ColorDef.RANK_GODTIER;
		else if (sender.getPlayer().hasPermission("group.donator"))
			colorP = ColorDef.RANK_DONATOR;

		if (c instanceof RPChannel) {
			if(c.hasNick(sender))	{
				outputName = c.getNick(sender);
				colorP = CanonNicks.valueOf(outputName).getColor();
			}
			else	{
				sender.sendMessage(ChatMsgs.errorNickRequired(c.getName()));
			}
		}
		colorW = Region.getRegionColor(getPlayerRegion());

		out = (isThirdPerson ? ">" : colorW + "<") + colorP + outputName
				+ ChatColor.WHITE
				+ (isThirdPerson ? "" : colorW + "> " + ChatColor.WHITE);
		// sender.getPlayer().sendMessage(out);
		return out;
	}

	/**
	 * Sends a message to the <code>Player</code>.
	 * 
	 * @param string
	 *            the message to send
	 */
	public void sendMessage(String string) {
		this.getPlayer().sendMessage(string);
	}
}
