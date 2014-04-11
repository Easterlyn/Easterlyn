package co.sblock.Sblock.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.CanonNicks;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.Database.SblockData;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.SblockEffects.PassiveEffect;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

/**
 * ChatUser is the class for storing all chat-related Player data.
 * 
 * @author Jikoo, Dublek
 */
public class ChatUser {

	/** The Player */
	private UUID playerID;

	/** The name of the Player's current focused Channel */
	private String current;

	/** The channels the Player is listening to */
	private Set<String> listening = new HashSet<String>();

	/** true if the Player is muted */
	private boolean globalMute;

	/** Map of task ID's. Key = Channel name, value = task ID. */
	private Map<String, Integer> tasks = new HashMap<String, Integer>();
	
	/** Keeps track of current Region for RegionChannel purposes */
	private Region currentRegion;
	
	/** Is the Player within range of a Computer? */
	private boolean computerAccess;

	public ChatUser(UUID playerID) {
		this.playerID = playerID;
		this.currentRegion = Region.getLocationRegion(this.getPlayer().getLocation());
	}

	/**
	 * Gets the Player.
	 * 
	 * @return the Player
	 */
	public Player getPlayer() {
		return Bukkit.getPlayer(playerID);
	}

	/**
	 * Gets the name of the Player.
	 * 
	 * @return the Player
	 */
	public String getPlayerName() {
		return Bukkit.getPlayer(playerID).getName();
	}

	/**
	 * Gets the UUID of the Player.
	 * 
	 * @return the UUID
	 */
	public UUID getUUID() {
		return this.playerID;
	}

	/**
	 * Gets the Player's current Region
	 * 
	 * @return the Region that the Player is in
	 */
	public Region getPlayerRegion() {
		return Region.getLocationRegion(this.getPlayer().getLocation());
	}


	/*
	 * CHAT & RELATED START
	 */

	/**
	 * Check to see if the Player is within range of a Computer.
	 * 
	 * @return true if the Player is within 10 meters of a Computer.
	 */
	public boolean hasComputerAccess() {
		SblockUser user = SblockUser.getUser(this.playerID);
		if (user.getPassiveEffects().containsKey(PassiveEffect.COMPUTER)) {
			return true;
		}
		for (Location l : SblockMachines.getMachines().getManager().getMachines(MachineType.COMPUTER)) {
			// distanceSquared <= maximumDistance^2. In this case, maximumDistance = 10.
			if (this.getPlayer().getLocation().distanceSquared(l) <= 100) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the Player's global nickname.
	 * 
	 * @return the Player's global nickname
	 */
	public String getGlobalNick() {
		return this.getPlayer().getDisplayName();
	}

	/**
	 * Sets the Player's nickname.
	 * 
	 * @param newNick the new nickname for the Player
	 */
	public void setGlobalNick(String newNick) {
		this.getPlayer().setDisplayName(newNick);
	}

	/**
	 * Sets the Player's chat mute status and sends corresponding message.
	 * 
	 * @param b true if the Player is being muted
	 */
	public void setMute(boolean b) {
		this.globalMute = b;
		if (b) {
			this.sendMessage(ChatMsgs.onUserMute());
		} else {
			this.sendMessage(ChatMsgs.onUserUnmute());
		}
	}

	/**
	 * Gets the Player's mute status.
	 * 
	 * @return true if the Player is muted
	 */
	public boolean isMute() {
		return globalMute;
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param c the Channel to set as current
	 */
	public void setCurrent(Channel c) {
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"));
			return;
		}
		if (c.isBanned(this)) {
			this.getPlayer().sendMessage(ChatMsgs.isBanned(c.getName()));
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()));
			return;
		}
		if (!this.isListening(c)) {
			this.addListening(c);
		}
		this.current = c.getName();
		this.sendMessage(ChatMsgs.onChannelJoin(this, this.getCurrent()));
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param c the Channel to set as current
	 */
	public void setCurrent(String s) {
		Channel c = ChannelManager.getChannelManager().getChannel(s);
		if (c == null) {
			return;
		}
		if (c.isBanned(this)) {
			this.getPlayer().sendMessage(ChatMsgs.isBanned(c.getName()));
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()));
			return;
		}
		this.current = c.getName();
		if (!this.isListening(c)) {
			this.addListening(c);
			return;
		}
		this.sendMessage(ChatMsgs.onChannelJoin(this, this.getCurrent()));
	}

	/**
	 * Gets the Channel the Player is currently sending messages to.
	 * 
	 * @return Channel
	 */
	public Channel getCurrent() {
		return SblockChat.getChat().getChannelManager().getChannel(current);
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param c the Channel to add
	 * 
	 * @return true if the Channel was added
	 */
	public boolean addListening(Channel c) {
		if (c == null) {
			return false;
		}
		if (c.isBanned(this)) {
			this.getPlayer().sendMessage(ChatMsgs.isBanned(c.getName()));
			return false;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()));
			return false;
		}
		if (!this.isListening(c)) {
			this.listening.add(c.getName());
		}
		if (!c.getListening().contains(this.playerID)) {
			c.sendToAll(this, ChatMsgs.onChannelJoin(this, c), "channel");
			c.addListening(this.playerID);
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyInChannel(c.getName()));
			return false;
		}
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param s the Channel name to add
	 * 
	 * @return true if the Channel was added
	 */
	public boolean addListening(String s) {
		Channel c = ChannelManager.getChannelManager().getChannel(s);
		return this.addListening(c);
	}

	/**
	 * Begin listening to a Set of channels. Used on login.
	 * 
	 * @param channels
	 */
	public void loginAddListening(String[] channels) {
		for (Object s : channels) {
			Channel c = ChannelManager.getChannelManager().getChannel((String) s);
			if (c != null && !c.isBanned(this)
					&& (c.getAccess() != AccessLevel.PRIVATE || c.isApproved(this))) {
				this.listening.add((String) s);
				c.addListening(playerID);
			}
		}

		StringBuilder base = new StringBuilder(ChatColor.GREEN.toString())
				.append(getPlayer().getDisplayName()).append(ChatColor.YELLOW)
				.append(" began pestering <>").append(ChatColor.YELLOW).append(" at ")
				.append(new SimpleDateFormat("HH:mm").format(new Date()));
		// Heavy loopage ensues
		for (ChatUser u : ChatUserManager.getUserManager().getUserlist()) {
			StringBuilder matches = new StringBuilder();
			for (String s : listening) {
				if (u.listening.contains(s)) {
					matches.append(ChatColor.GOLD).append(s).append(ChatColor.YELLOW).append(", ");
				}
			}
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.toString().replace("<>", matches.toString()));
				int comma = msg.toString().lastIndexOf(',');
				if (comma != -1) {
					u.sendMessage(msg.replace(comma, comma + 1, " and").toString());
				}
			}
		}
	}

	/**
	 * Remove a Channel from the Player's listening List.
	 * 
	 * @param cName the name of the Channel to remove
	 */
	public void removeListening(String cName) {
		Channel c = SblockChat.getChat().getChannelManager().getChannel(cName);
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(cName));
			this.listening.remove(cName);
			return;
		}
		if (this.listening.remove(cName)) {
				c.sendToAll(this, ChatMsgs.onChannelLeave(this, c), "channel");
				c.removeListening(this.playerID);
			if (cName.equals(current)) {
				current = null;
			}
		} else {
			this.sendMessage(ChatMsgs.errorNotListening(cName));
		}
	}

	/**
	 * Tells a Channel the Player is leaving on quit.
	 * 
	 * @param cName the name of the Channel to inform
	 */
	public void removeListeningQuit(String cName) {
		Channel c = SblockChat.getChat().getChannelManager()
				.getChannel(cName);
		if (c != null) {
			c.removeListening(this.getUUID());
		} else {
			this.listening.remove(cName);
		}
	}

	/**
	 * Gets the Set of names of Channels that the Player is listening to.
	 * 
	 * @return a Set<String> of Channel names.
	 */
	public Set<String> getListening() {
		return listening;
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param c the Channel to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public boolean isListening(Channel c) {
		return listening.contains(c.getName());
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param s the Channel name to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public boolean isListening(String s) {
		return listening.contains(s);
	}

	/**
	 * Gets the Player's current Region.
	 * 
	 * @return the Region the Player is in.
	 */
	public Region getCurrentRegion() {
		return currentRegion;
	}

	/**
	 * Update current Region and change RegionChannel.
	 * 
	 * @param newR the Region being transitioned into
	 */
	public void updateCurrentRegion(Region newR) {
		if (newR == currentRegion) {
			if (!listening.contains("#" + currentRegion.toString())) {
				Channel c = ChannelManager.getChannelManager().getChannel("#" + currentRegion.toString());
				this.addListening(c);
			}
			return;
		}
		Channel newC = ChannelManager.getChannelManager().getChannel("#" + newR.toString());
		if (current == null || current.equals("#" + currentRegion.toString())) {
			current = newC.getName();
		}
		this.removeListening("#" + currentRegion.toString());
		this.addListening(newC);
		currentRegion = newR;
	}

	public boolean getComputerAccess() {
		if (!SblockChat.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		return computerAccess;
	}

	public void setComputerAccess() {
		SblockUser user = SblockUser.getUser(this.playerID);
		if (user.getPassiveEffects().containsKey(PassiveEffect.COMPUTER)
				|| SblockMachines.getMachines().getManager().isByComputer(getPlayer(), 10)) {
			computerAccess = true;
		} else {
			computerAccess = false;
		}
	}

	/**
	 * Method for handling all Player chat.
	 * 
	 * @param event the relevant AsyncPlayerChatEvent
	 */
	public void chat(String msg, boolean forceThirdPerson) {

		// Check if the user can speak
		if (isMute()) {
			sendMessage(ChatMsgs.isMute());
			return;
		}

		// default to current channel recieving message
		Channel sendto = SblockChat.getChat().getChannelManager().getChannel(current);

		// check if chat is directed at another channel
		int space = msg.indexOf(' ');
		if (msg.indexOf("@") == 0 && space > 1) {
			// Check for alternate channel destination. Failing that, warn user.
			String newChannel = msg.substring(1, space);
			if (SblockChat.getChat().getChannelManager().isValidChannel(newChannel)) {
				sendto = SblockChat.getChat().getChannelManager().getChannel(newChannel);
				if (sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(this)) {
					// User not approved in channel
					sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sendto.getName()));
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
				sendMessage(ChatMsgs.errorInvalidChannel(newChannel));
				return;
			}
		} else if (current == null) {
			sendMessage(ChatMsgs.errorNoCurrent());
			return;
		}
		
		if (sendto instanceof RPChannel) {
			if (!sendto.hasNick(this)) {
				sendMessage(ChatMsgs.errorNickRequired(sendto.getName()));
				return;
			}
		}

		// Chat is being done via /me
		if (forceThirdPerson) {
			msg = "#>" + msg;
		}

		this.sendMessageToChannel(sendto, msg);
	}

	/**
	 * Format a chat message and send it to a Channel.
	 * 
	 * @param sender the SblockUser speaking
	 * @param c the Channel to send the message to
	 * @param s the message to send
	 */
	public void sendMessageToChannel(Channel c, String s) {

		// check for third person modifier (#)
		boolean isThirdPerson = s.length() > 2 && s.charAt(0) == '#' && s.charAt(1) == '>';

		// strip third person modifier from chat
		if (isThirdPerson) {
			s = s.substring(2);
		}

		if(c.getType() == ChannelType.RP) {
			//apply quirk to message
			s = CanonNicks.getNick(c.getNick(this)).applyQuirk(s);
		} else if (c.isChannelMod(this)) {
			// color formatting - applies only to channel mods.
			s = ChatColor.translateAlternateColorCodes('&', s);
		}

		// apply channel and display name formatting
		// [$channel] <$player> $message normal chat
		// [$channel]>$player: $message /me chat
		String output = getOutputChannelF(this, c) + this.getOutputNameF(this, isThirdPerson, c) + s;

		// send message to everyone in channel
		c.sendToAll(this, output, isThirdPerson ? "me" : "chat");
	}

	/**
	 * Send a message from a Channel to this Player.
	 * 
	 * @param message the message to send
	 * @param c the Channel to send to.
	 * @param type the type of chat for handling purposes
	 */
	public void sendMessageFromChannel(String message, Channel c, String type) {
		Player p = this.getPlayer();

		// Check to make sure user is online
		if (p == null) {
			SblockData.getDB().saveUserData(playerID);
			c.removeListening(playerID);
			return;
		}

		// final output, sends message to user
		if (!type.equals("channel")) {
			// Checking for highlights within the message commences
			if (!message.startsWith(getOutputChannelF(this, c) + this.getOutputNameF(this, type.equals("me"), c))) {
				// Chat is not being sent by this ChatUser, attempt highlight matches

				StringBuilder regex = new StringBuilder();
				String nick = ChatColor.stripColor(c.getNick(this));
				if (nick.equals(p.getName())) {
					regex.append('(').append(ignoreCaseRegex(p.getName())).append(')');
				} else {
					regex.append("((").append(ignoreCaseRegex(nick)).append(")|(")
					.append(ignoreCaseRegex(p.getName())).append("))");
				}
				// Regex completed, should be similar to (([Nn][Ii][Cc][Kk])|([Nn][Aa][Mm][Ee]))

				StringBuilder msg = new StringBuilder();
				Matcher match = Pattern.compile(regex.toString()).matcher(message);
				int lastEnd = 0;
				// For every match, prepend aqua chat color and append previous color
				while (match.find()) {
					msg.append(message.substring(lastEnd, match.start()));
					String last = ChatColor.getLastColors(msg.toString());
					msg.append(ChatColor.AQUA).append(match.group()).append(last);
					lastEnd = match.end();
				}
				if (lastEnd < message.length()) {
				msg.append(message.substring(lastEnd));
				}
				message = msg.toString();

				if (lastEnd > 0) {
					// Matches were found, commence highlight format changes.
					message = message.replaceFirst("\\[(.{1,18})\\]", ChatColor.AQUA + "!!$1" + ChatColor.AQUA +"!!");
					// Funtimes sound effects here
					switch ((int) (Math.random() * 20)) {
					case 0:
						p.playSound(p.getLocation(), Sound.ENDERMAN_STARE, 1, 2);
						break;
					case 1:
						p.playSound(p.getLocation(), Sound.WITHER_SPAWN, 1, 2);
						break;
					case 2:
					case 3:
						p.playSound(p.getLocation(), Sound.ANVIL_LAND, 1, 1);
						break;
					default:
						p.playSound(p.getLocation(), Sound.LEVEL_UP, 1, 2);
					}
				}
			}
		}
		p.sendMessage(message);
	}

	private String ignoreCaseRegex(String s) {
		StringBuilder regex = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			regex.append('[');
			char ch = s.charAt(i);
			if (Character.isLetter(ch)) {
				regex.append(Character.toUpperCase(ch)).append(Character.toLowerCase(ch));
			} else {
				regex.append(ch);
			}
			regex.append(']');
		}
		return regex.toString();
	}

	// Here begins output formatting. Abandon all hope ye who enter

	/**
	 * Gets chat prefixing based on conditions.
	 * 
	 * @param sender the SblockUser sending the message
	 * @param channel the Channel receiving the message
	 * 
	 * @return the prefix for specified conditions
	 */
	public String getOutputChannelF(ChatUser sender, Channel channel) {
		// colors for [$channel] applied here
		String out = "";

		ChatColor color = ColorDef.CHATRANK_MEMBER;
		if (channel.isOwner(sender)) {
			color = ColorDef.CHATRANK_OWNER;
		} else if (channel.isChannelMod(sender)) {
			color = ColorDef.CHATRANK_MOD;
		}
		out = ChatColor.WHITE + "[" + color + channel.getName() + ChatColor.WHITE + "]";
		return out;
	}

	/**
	 * Gets chat prefixing based on conditions.
	 * 
	 * @param sender the SblockUser sending the message
	 * @param isThirdPerson whether or not to provide a third person prefix
	 * @param channel the Channel receiving the message
	 * 
	 * @return the prefix for specified conditions
	 */
	public String getOutputNameF(ChatUser sender, boolean isThirdPerson, Channel c) {
		// colors for <$name> applied here
		String out = "";
		String outputName = c.getNick(sender);

		ChatColor colorP = ColorDef.RANK_HERO;
		ChatColor colorW = ColorDef.DEFAULT;

		if (sender.getPlayer().hasPermission("group.horrorterror"))
			colorP = ColorDef.RANK_ADMIN;
		else if (sender.getPlayer().hasPermission("group.denizen"))
			colorP = ColorDef.RANK_MOD;
		else if (sender.getPlayer().hasPermission("group.felt"))
			colorP = ColorDef.RANK_FELT;
		else if (sender.getPlayer().hasPermission("group.helper"))
			colorP = ColorDef.RANK_HELPER;
		else if (sender.getPlayer().hasPermission("group.godtier"))
			colorP = ColorDef.RANK_GODTIER;
		else if (sender.getPlayer().hasPermission("group.donator"))
			colorP = ColorDef.RANK_DONATOR;

		if (c instanceof RPChannel) {
			if(c.hasNick(sender)) {
				colorP = CanonNicks.getNick(outputName).getColor();
			} else {
				sender.sendMessage(ChatMsgs.errorNickRequired(c.getName()));
			}
		}
		colorW = Region.getRegionColor(getPlayerRegion());

		out = (isThirdPerson ? ChatColor.BLUE + "> " : colorW + " <") + colorP + outputName
				+ ChatColor.WHITE
				+ (isThirdPerson ? ChatColor.BLUE + ": " : colorW + "> ") + ChatColor.WHITE;
		// sender.getPlayer().sendMessage(out);
		return out;
	}

	/**
	 * Sends a message to the Player.
	 * 
	 * @param string the message to send
	 */
	public void sendMessage(String string) {
		this.getPlayer().sendMessage(string);
	}

	/**
	 * Important SblockUser data formatted to be easily readable when printed.
	 * 
	 * @return a representation of the most important data stored by this SblockUser
	 */
	public String toString() {
		ChatColor sys = ChatColor.DARK_AQUA;
		ChatColor txt = ChatColor.YELLOW;
		String div = sys + ", " + txt;
		SblockUser sUser = UserManager.getUserManager().getUser(getPlayerName());
		
		String s = sys + "-----------------------------------------\n" + 
				txt + this.getPlayer().getName() + div + sUser.getClassType() + " of " + sUser.getAspect() + "\n" + 
				sUser.getMPlanet() + div + sUser.getDPlanet() + div + sUser.getTower() + div + sUser.isSleeping() + "\n" + 
				this.isMute() + div + this.current + div + this.getListening().toString() + "\n" +
				this.currentRegion + div + sUser.getPreviousLocationString() + "\n" +
				sUser.getUserIP() + "\n" +
				sUser.getTimePlayed() + div + sUser.getPlayer().getLastPlayed() + "\n" +
				sys + "-----------------------------------------";
		return s;
	}

	/**
	 * Gets a ChatUser by Player name.
	 * 
	 * @param userName the name to match
	 * 
	 * @return the ChatUser specified or null if invalid.
	 */
	public static ChatUser getUser(String userName) {
		Player p = Bukkit.getPlayer(userName);
		if (p == null) {
			return null;
		}
		return ChatUserManager.getUserManager().getUser(p.getUniqueId());
	}

	public static ChatUser getUser(UUID userID) {
		return ChatUserManager.getUserManager().getUser(userID);
	}

	/**
	 * Stop all pending tasks for this SblockUser.
	 */
	public void stopPendingTasks() {
		for (int task : tasks.values()) {
			Bukkit.getScheduler().cancelTask(task);
		}
	}
}
