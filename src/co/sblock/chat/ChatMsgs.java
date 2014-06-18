package co.sblock.chat;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.ChatColor;

import co.sblock.chat.channel.CanonNicks;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.users.User;

/**
 * A container for all messages sent to <code>Player</code>s from various Chat subsections.
 * 
 * @author Dublek, Jikoo
 */
public class ChatMsgs {

	public static String onChannelJoin(User user, Channel channel) {
		String name = user.getPlayer().getDisplayName();
		String message = "pestering";
		ChatColor nameC = ChatColor.GREEN;
		if (channel.hasNick(user)) {
			name = channel.getNick(user);
			if (channel.getType().equals(ChannelType.RP)) {
				nameC = CanonNicks.getNick(name).getColor();
				message = CanonNicks.getNick(name).getPester();
				name = CanonNicks.getNick(name).getHandle();
			}
		}
		return nameC + name + ChatColor.YELLOW + " began " + message + " " + ChatColor.GOLD
				+ channel.getName() + ChatColor.YELLOW + " at "
				+ new SimpleDateFormat("HH:mm").format(new Date());
	}

	public static String onChannelLeave(User user, Channel channel) {
		return ChatMsgs.onChannelJoin(user, channel).replaceAll("began", "ceased");
	}

	public static String onChannelSetCurrent(String channelName) {
		return ChatColor.YELLOW + "Current channel set to " + ChatColor.GOLD + channelName;
	}

	public static String onChannelCreation(Channel channel) {
		return ChatColor.GOLD + channel.getName() + ChatColor.GREEN
				+ " has been created with access " + ChatColor.GOLD + channel.getAccess()
				+ ChatColor.GREEN + " and type " + ChatColor.GOLD + channel.getType();
	}

	public static String onChannelDisband(String channelName) {
		return ChatColor.GOLD + channelName + ChatColor.RED
				+ " has been disbanded! These are indeed dark times...";
	}

	public static String onUserMute(String name) {
		return ChatColor.YELLOW + name + ChatColor.RED + " has been muted in all channels.";
	}

	public static String onUserUnmute(String name) {
		return ChatColor.YELLOW + name + ChatColor.GREEN + " has been unmuted in all channels.";
	}

	public static String isMute() {
		return ChatColor.RED + "You are muted!";
	}

	public static String onUserKickAnnounce(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " has been kicked from " + ChatColor.GOLD
				+ channelName + ChatColor.YELLOW + "!";
	}

	public static String onUserBanAnnounce(String userName, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " has been " + ChatColor.BOLD
				+ "banned" + ChatColor.RESET + " from " + ChatColor.GOLD + channelName
				+ ChatColor.RED + "!";
	}

	public static String onUserUnbanAnnounce(String userName, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " has been " + ChatColor.BOLD
				+ "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD + channelName
				+ ChatColor.RED + "!";
	}

	public static String onUserApproved(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " has been approved in " + ChatColor.GOLD
				+ channelName + ChatColor.YELLOW + "!";
	}

	public static String onUserDeapproved(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " has been deapproved in " + ChatColor.GOLD
				+ channelName + ChatColor.YELLOW + "!";
	}

	public static String onChannelModAdd(String userName, String channelName) {
		return ChatColor.YELLOW + userName + " is now a mod in " + ChatColor.GOLD + channelName
				+ ChatColor.YELLOW + "!";
	}

	public static String onUserSetGlobalNick(String userName, String nick) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " shall henceforth be know as "
				+ ChatColor.YELLOW + nick;
	}

	public static String onUserRmGlobalNick(String userName, String nick) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " is no longer known as "
				+ ChatColor.YELLOW + nick;
	}

	public static String onUserSetNick(String userName, String nick, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " is now known as "
				+ ChatColor.YELLOW + nick + ChatColor.YELLOW + " in " + channelName;
	}

	public static String onUserRmNick(String userName, String nick, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.BLUE + " is no longer known as "
				+ ChatColor.YELLOW + nick + ChatColor.YELLOW + " in " + channelName;
	}

	public static String onChannelCommandFail(String channelName) {
		return ChatColor.RED + "You do not have high enough access in " + ChatColor.GOLD
				+ channelName + ChatColor.RED + " to perform that action!";
	}

	public static String onChannelModRm(String userName, String channelName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " is no longer a mod in "
				+ ChatColor.GOLD + channelName + ChatColor.RED + "!";
	}

	public static String onUserDeniedPrivateAccess(String channelName) {
		return ChatColor.GOLD + channelName + ChatColor.RED + " is a private channel."
				+ ChatColor.YELLOW + " Ask a channel mod for access!";
	}

	public static String errorInvalidChannel(String channelName) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + channelName + ChatColor.RED
				+ " does not exist!";
	}

	public static String errorInvalidType(String typeName) {
		return ChatColor.GOLD + typeName + ChatColor.RED
				+ " is not a valid channel type!\nValid types: NORMAL, RP, NICK.";
	}

	public static String unsupportedOperation(String channelName) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + channelName + ChatColor.RED
				+ " does not support that operation.";
	}

	public static String errorInvalidAccess(String accessName) {
		return ChatColor.GOLD + accessName + ChatColor.RED
				+ " is not a valid access level!\nValid levels: PUBLIC, PRIVATE";
	}

	public static String errorInvalidUser(String userName) {
		return ChatColor.YELLOW + userName + ChatColor.RED + " does not exist! Get them to log in.";
	}

	public static String errorNoCurrent() {
		return ChatColor.RED + "You must set a current channel to chat! Use " + ChatColor.AQUA
				+ "/sc c <channel>";
	}

	public static String errorAlreadyListening(String channelName) {
		return ChatColor.RED + "You are already listening to channel " + ChatColor.GOLD
				+ channelName;
	}

	public static String errorNotListening(String channelName) {
		return ChatColor.RED + "You are not listening to channel " + ChatColor.GOLD + channelName;
	}

	public static String errorNickRequired(String channelName) {
		return ChatColor.GOLD + channelName + ChatColor.RED
				+ " is a roleplay channel, a nick is required. Use " + ChatColor.AQUA
				+ "/sc nick set";
	}

	public static String errorNickNotCanon(String nick) {
		return ChatColor.GOLD + nick + ChatColor.RED + " is not a canon nickname! Use "
				+ ChatColor.AQUA + "/sc nick list" + ChatColor.RED + " for a list.";
	}

	public static String errorNickInUse(String nick) {
		return ChatColor.GOLD + nick + ChatColor.RED + " is already in use!";
	}

	public static String errorChannelName() {
		return ChatColor.RED + "Channel names must start with '#' and cannot exceed 16 characters!";
	}

	public static String errorChannelExists() {
		return ChatColor.RED + "A channel by that name already exists!";
	}

	public static String errorRegionChannelJoin() {
		return ChatColor.RED + "You cannot join a region channel!";
	}

	public static String errorRegionChannelLeave() {
		return ChatColor.RED + "You cannot leave a region channel!";
	}

	public static String errorSuppressingGlobal() {
		return ChatColor.RED + "You cannot talk in a global channel while suppressing!\nUse "
				+ ChatColor.AQUA + "/sc suppress" + ChatColor.RED + " to toggle.";
	}

	public static String errorDisbandDefault() {
		return ChatColor.RED + "Hardcoded default channels cannot be disbanded.";
	}

	public static String helpGlobalMod() {
		return ChatColor.AQUA + "/sc global " + ChatColor.YELLOW + "subcommands:\n"
				+ ChatColor.AQUA + "mute <user>" + ChatColor.YELLOW
				+ ": Mute a user in all channels\n" + ChatColor.AQUA + "unmute <user>"
				+ ChatColor.YELLOW + ": Unmute a user in all channels\n" + ChatColor.AQUA
				+ "setnick <user> <nick>" + ChatColor.YELLOW
				+ ": Set a global nick for a player (Mostly for teh lulz)\n" + ChatColor.AQUA
				+ "rmnick <user>" + ChatColor.YELLOW + ": Remove a global nick from a player";
	}

	public static String helpChannelOwner() {
		return ChatColor.YELLOW + "Channel Owner " + ChatColor.AQUA + "/sc channel "
				+ ChatColor.YELLOW + "subcommands:\n" + ChatColor.AQUA + "mod <add/remove> <user>"
				+ ChatColor.YELLOW + ": Add or remove a channel mod\n" + ChatColor.AQUA
				+ "<ban/unban> <user>" + ChatColor.YELLOW + ": (Un)bans a user from the channel\n"
				+ ChatColor.AQUA + "disband" + ChatColor.YELLOW + ": Drop the channel!";
	}

	public static String helpChannelMod() {
		return ChatColor.YELLOW + "Channel Mod " + ChatColor.AQUA + "/sc channel "
				+ ChatColor.YELLOW + "subcommands:\n" + ChatColor.AQUA + "kick <user>"
				+ ChatColor.YELLOW + ": Kick a user from the channel\n" + ChatColor.AQUA
				+ "ban <user>" + ChatColor.YELLOW + ": Ban a user from the channel\n"
				+ ChatColor.AQUA + "getListeners" + ChatColor.YELLOW
				+ ": List all users currently listening to this channel\n" + ChatColor.AQUA
				+ "(de)approve <user>" + ChatColor.YELLOW
				+ ": (De)approve a user for this channel.";
	}

	public static String helpDefault() {
		return ChatColor.AQUA + "/sc " + ChatColor.YELLOW + "subcommands:\n" + ChatColor.AQUA
				+ "/sc c <channel>" + ChatColor.YELLOW
				+ ": Talking will send messages to <channel>.\n" + ChatColor.AQUA
				+ "/sc l <channel>" + ChatColor.YELLOW + ": Listen to <channel>.\n"
				+ ChatColor.AQUA + "/sc leave <channel>" + ChatColor.YELLOW
				+ ": Stop listening to <channel>.\n" + ChatColor.AQUA + "/sc list"
				+ ChatColor.YELLOW + ": List all channels you are listening to.\n" + ChatColor.AQUA
				+ "/sc listall" + ChatColor.YELLOW + ": List all channels.\n" + ChatColor.AQUA
				+ "/sc nick <set|remove|list> (nick)" + ChatColor.YELLOW
				+ ": Set a nick in a Nick/RP channel.\n" + ChatColor.AQUA + "/sc suppress"
				+ ChatColor.YELLOW + ": Toggle ignoring global channels.\n" + ChatColor.AQUA
				+ "/sc new <name> <access> <type>" + ChatColor.YELLOW + ": Create a new channel.\n"
				+ ChatColor.AQUA + "/sc channel" + ChatColor.YELLOW
				+ ": Channel moderation commands.";
	}

	public static String helpSCC() {
		return ChatColor.AQUA + "/sc c <channel>" + ChatColor.YELLOW
				+ ": Talking will send messages to <channel>.";
	}

	public static String helpSCL() {
		return ChatColor.AQUA + "/sc l <channel>" + ChatColor.YELLOW + ": Listen to <channel>.";
	}

	public static String helpSCLeave() {
		return ChatColor.AQUA + "/sc leave <channel>" + ChatColor.YELLOW
				+ ": Stop listening to <channel>.";
	}

	public static String helpSCNew() {
		return ChatColor.AQUA + "/sc new <name> <access> <type>" + ChatColor.YELLOW
				+ ": Create a new channel.\nAccess must be either PUBLIC or PRIVATE\n"
				+ "Type must be NORMAL, NICK, or RP";
	}

	public static String helpSCNick() {
		return ChatColor.AQUA + "/sc nick <set|remove|list> (nick)" + ChatColor.YELLOW
				+ ": Set a nick in a Nick/RP channel.";
	}
}
