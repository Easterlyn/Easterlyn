package co.sblock.Sblock.Chat;

import java.text.SimpleDateFormat;

import java.util.Date;

import org.bukkit.ChatColor;

import co.sblock.Sblock.Chat.Channel.CanonNicks;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelType;

/**
 * A container for all messages sent to <code>Player</code>s from various Chat subsections.
 * 
 * @author Dublek, Jikoo
 */
public class ChatMsgs {

	public static String onChannelJoin(ChatUser u, Channel c) {
		String name = u.getGlobalNick();
		String message = "pestering";
		ChatColor nameC = ChatColor.GREEN;
		if (c.hasNick(u)) {
			name = c.getNick(u);
			if (c.getType().equals(ChannelType.RP)) {
				nameC = CanonNicks.getNick(name).getColor();
				message = CanonNicks.getNick(name).getPester();
				name = CanonNicks.getNick(name).getHandle();
			}
		}
		return nameC + name + ChatColor.YELLOW + " began "
				+ message + " " + ChatColor.GOLD
				+ c.getName() + ChatColor.YELLOW + " at "
				+ new SimpleDateFormat("HH:mm").format(new Date());
	}

	public static String onChannelLeave(ChatUser u, Channel c) {
		return ChatMsgs.onChannelJoin(u, c).replaceAll("began", "ceased");
	}

	public static String onChannelCreation(Channel c) {
		return ChatColor.GOLD + c.getName() + ChatColor.GREEN
				+ " has been created with access " + ChatColor.GOLD+ c.getAccess() + ChatColor.GREEN
				+ " and type " + ChatColor.GOLD + c.getType();
	}

	public static String onChannelDisband(String c) {
		return ChatColor.GOLD + c + ChatColor.RED
				+ " has been disbanded! These are indeed dark times...";
	}

	public static String onUserMute() {
		return ChatColor.RED + "You have been muted in all channels.";
	}

	public static String onUserMute(String name) {
		return ChatColor.YELLOW + name + ChatColor.RED + " has been muted in all channels.";
	}

	public static String onUserUnmute() {
		return ChatColor.GREEN + "You have been unmuted in all channels.";
	}

	public static String onUserUnmute(String name) {
		return ChatColor.YELLOW + name + ChatColor.GREEN + " has been unmuted in all channels.";
	}

	public static String isMute() {
		return ChatColor.RED + "You are muted!";
	}

	public static String onUserKick(String s) {
		return ChatColor.YELLOW + "You have been kicked from "
				+ ChatColor.GOLD + s + ChatColor.YELLOW + "!";
	}

	public static String onUserKickAnnounce(String name, String s) {
		return ChatColor.YELLOW + name + " has been kicked from "
				+ ChatColor.GOLD + s + ChatColor.YELLOW + "!";
	}

	public static String onUserKickFail(String s) {
		return ChatColor.RED + "You do not have permission to kick people in "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserKickedAlready(String name, String s) {
		return ChatColor.YELLOW + name + ChatColor.RED +
				" is not chatting in " + ChatColor.GOLD + s +
				ChatColor.RED + "!";
	}

	public static String onUserBan(String s) {
		return ChatColor.RED + "You have been " + ChatColor.BOLD
				+ "banned" + ChatColor.RESET + ChatColor.RED + " from "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserBanAnnounce(String u, String s) {
		return ChatColor.YELLOW + u + ChatColor.RED
				+ " has been " + ChatColor.BOLD + "banned"
				+ ChatColor.RESET + " from " + ChatColor.GOLD
				+ s + ChatColor.RED + "!";
	}

	public static String onUserBanFail(String s) {
		return ChatColor.RED + "You do not have permission to ban people in "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserBannedAlready(String u, String s) {
		return ChatColor.YELLOW + u + ChatColor.RED +
				" is already banned in " + ChatColor.GOLD
				+ s + ChatColor.RED + "!";
	}

	public static String onUserUnban(String s) {
		return ChatColor.RED + "You have been " + ChatColor.BOLD
				+ "unbanned" + ChatColor.RESET + " from " + ChatColor.GOLD
				+ s + ChatColor.RED + "!";
	}

	public static String onUserUnbanAnnounce(String u, String s) {
		return ChatColor.YELLOW + u + ChatColor.RED +
				" has been " + ChatColor.BOLD + "unbanned" + ChatColor.RESET +
				" from " + ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserUnbanFail(String s) {
		return ChatColor.RED + "You do not have permission to unban people in "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserUnbannedAlready(String u, String s) {
		return ChatColor.YELLOW + u + ChatColor.RED +
				" is not banned in " + ChatColor.GOLD + s
				+ ChatColor.RED + "!";
	}

	public static String onUserMod(String s) {
		return ChatColor.GREEN + "You are now a mod in " + ChatColor.GOLD
				+ s + ChatColor.GREEN + "!";
	}

	public static String onUserModAnnounce(String user, String s) {
		return ChatColor.YELLOW + user + " is now a mod in " + 
				ChatColor.GOLD + s + ChatColor.YELLOW + "!";
	}

	public static String onUserModFail(String s) {
		return ChatColor.RED + "You do not have permission to (de)mod people in "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserModAlready(String user, String s) {
		return ChatColor.YELLOW + user + ChatColor.RED + " is already a mod in "
				+ ChatColor.GOLD + s + ChatColor.RED + "!";
	}

	public static String onUserSetGlobalNick(String user, String nick) {
		return ChatColor.YELLOW + user + ChatColor.BLUE + " shall henceforth be know as "
				+ ChatColor.YELLOW + nick;
	}

	public static String onUserRmGlobalNick(String user, String nick) {
		return ChatColor.YELLOW + user + ChatColor.BLUE + " is no longer known as "
				+ ChatColor.YELLOW + nick;
	}

	public static String onUserSetNick(String user, String nick, String s) {
		return ChatColor.YELLOW + user + ChatColor.BLUE + " is now known as "
				+ ChatColor.YELLOW + nick + " in " + s;
	}

	public static String onUserRmNick(String user, String nick, String s) {
		return ChatColor.YELLOW + user + ChatColor.BLUE + " is no longer known as "
				+ ChatColor.YELLOW + nick + " in " + s;
	}

	public static String onUserRmMod(String u, String s) {
		return null;
	}

	public static String onUserRmModAnnounce(String target, String s) {
		return null;
	}

	public static String isBanned(String s) {
		return ChatColor.RED + "You are banned from channel " + ChatColor.GOLD
				+ s + ChatColor.RED + "!";
	}

	public static String onUserDeniedPrivateAccess(String s) {
		return ChatColor.GOLD + s + ChatColor.RED
				+ " is a private channel!\n" + ChatColor.YELLOW
				+ "Request access with the command " + ChatColor.BLUE
				+ "/sc request " + s;
	}

	public static String errorInvalidChannel(String c) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + c
				+ ChatColor.RED + " does not exist!";
	}

	public static String errorInvalidType(String s) {
		return ChatColor.GOLD + s + ChatColor.RED +
				" is not a valid channel type!\nValid types: NORMAL, RP, NICK.";
	}

	public static String unsupportedOperation(String s) {
		return ChatColor.RED + "Channel " + ChatColor.GOLD + s
				+ ChatColor.RED + " does not support that operation.";
	}

	public static String errorInvalidAccess(String s) {
		return ChatColor.GOLD + s + ChatColor.RED +
				" is not a valid access level!\nValid levels: PUBLIC, PRIVATE";
	}

	public static String errorInvalidUser(String username) {
		return ChatColor.YELLOW + username + ChatColor.RED
				+ " does not exist! Get them to log in once.";
	}

	public static String errorAlreadyInChannel(String s) {
		return ChatColor.RED + "You are already listening to "
				+ ChatColor.GOLD + s;
	}

	public static String errorNoCurrent() {
		return ChatColor.RED + "You must have set a current channel to chat! Use "
				+ ChatColor.GREEN + "/sc c <channel>";
	}

	public static String errorNotListening(String s) {
		return ChatColor.RED + "You are not listening to channel "
				+ ChatColor.GOLD + s;
	}

	public static String errorNickUnsupported() {
		return ChatColor.RED + "This channel does not support nicks!";
	}

	public static String errorNickRequired(String name) {
		return ChatColor.GOLD + name + ChatColor.RED +
				" is a roleplaying channel. You must have a nickname to talk!";
	}

	public static String errorNickNotCanon(String nick) {
		return ChatColor.GOLD + nick + ChatColor.RED + 
				" is not a canon nickname!";
	}

	public static String errorNickInUse(String nick) {
		return ChatColor.GOLD + nick + ChatColor.RED + " is already in use!";
	}

	public static String errorChannelNameTooLong() {
		return ChatColor.RED + "Channel names may not exceed 16 characters in length!";
	}

	public static String errorRegionChannel() {
		return ChatColor.RED + "You cannot do this operation in a Region channel!";
	}

	public static String errorRegionChannelJoin() {
		return ChatColor.RED + "You cannot join a region channel!";
	}

	public static String errorRegionChannelLeave() {
		return ChatColor.RED + "You cannot join a region channel!";
	}

	public static String helpGlobalMod() {
		return ChatColor.YELLOW + "Mod Global Commands:\n"
				+ ChatColor.AQUA + "global mute <$user>"
				+ ChatColor.YELLOW + ": Mute a user in all channels\n"
				+ ChatColor.AQUA + "global unmute <$user>"
				+ ChatColor.YELLOW + ": Unmute a user in all channels\n"
				+ ChatColor.AQUA + "global setnick <$user> <nick>"
				+ ChatColor.YELLOW + ": Set a global nick for a player (Mostly for teh lulz)\n"
				+ ChatColor.AQUA + "global rmnick <$user>"
				+ ChatColor.YELLOW + ": Remove a global nick from a player";
	}

	public static String helpChannelOwner() {
		return ChatColor.YELLOW + "Channel Owner commands:\n"
				+ ChatColor.AQUA + "channel mod <add/remove> <$user>"
				+ ChatColor.YELLOW + ": Add or remove a channel mod\n"
				+ ChatColor.AQUA + "channel <ban/unban> <$user>"
				+ ChatColor.YELLOW + ": (Un)bans a user from the channel\n"
				+ ChatColor.AQUA + "disband"
				+ ChatColor.YELLOW + ": Drop the channel!";
	}

	public static String helpChannelMod() {
		return ChatColor.YELLOW + "Channel Mod commands:\n"
				+ ChatColor.AQUA + "channel kick <$user>"
				+ ChatColor.YELLOW + ": Kick a user from the channel\n"
				+ ChatColor.AQUA + "channel ban <$user>"
				+ ChatColor.YELLOW + ": Ban a user from the channel\n"
				+ ChatColor.AQUA + "channel setalias <$alias>"
				+ ChatColor.YELLOW + ": Set an alias for the channel\n"
				+ ChatColor.AQUA + "channel rmalias"
				+ ChatColor.YELLOW + ": Remove the channel alias\n"
				+ ChatColor.AQUA + "channel getListeners"
				+ ChatColor.YELLOW + ": List all users currently listening to this channel";
	}

	public static String helpDefault() {
		return ChatColor.AQUA + "/sc "
				+ ChatColor.YELLOW + "subcommands:\n"
				+ ChatColor.AQUA + "c <channel>"
				+ ChatColor.YELLOW + ": Talking will send messages to <channel>.\n"
				+ ChatColor.AQUA + "l <channel>"
				+ ChatColor.YELLOW + ": Listen to <channel>.\n"
				+ ChatColor.AQUA + "leave <channel>"
				+ ChatColor.YELLOW + ": Stop listening to <channel>.\n"
				+ ChatColor.AQUA + "nick <set/remove> <nick>"
				+ ChatColor.YELLOW + ": Set a nick for a Nck or RP channel.\n"
				+ ChatColor.AQUA + "list"
				+ ChatColor.YELLOW + ": List all channels you are listening to.\n"
				+ ChatColor.AQUA + "listall"
				+ ChatColor.YELLOW + ": List all channels.\n"
				+ ChatColor.AQUA + "new <name> <access> <type>"
				+ ChatColor.YELLOW + ": Create a new channel.\n"
				+ ChatColor.AQUA + "channel"
				+ ChatColor.YELLOW + ": Channel moderation commands.";
	}

	public static String helpSCC() {
		return ChatColor.AQUA + "/sc c <channel>"
				+ ChatColor.YELLOW + ": Talking will send messages to <channel>.";
	}

	public static String helpSCL() {
		return ChatColor.AQUA + "/sc l <channel>"
				+ ChatColor.YELLOW + ": Listen to <channel>.";
	}

	public static String helpSCLeave() {
		return ChatColor.AQUA + "/sc leave <channel>"
				+ ChatColor.YELLOW + ": Stop listening to <channel>.";
	}

	public static String helpSCNew() {
		return ChatColor.AQUA + "/sc new <name> <access> <type>" + ChatColor.YELLOW
				+ ": Create a new channel.\nAccess must be either PUBLIC or PRIVATE\n"
				+ "Type must be NORMAL, NICK, or RP";
	}

	public static String helpSCNick() {
		return ChatColor.AQUA + "nick <set/remove> <nick>"
			+ ChatColor.YELLOW + ": Set a nick for a Nck or RP channel.";
	}

	public static String helpSCGlobal() {
		return null;
	}

	public static String darkMysteries() {
		return ChatColor.BLACK + "There are mysteries into which it behooves one not to delve too deeply...";
	}
}
