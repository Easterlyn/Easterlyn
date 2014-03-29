package co.sblock.Sblock.Chat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

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
	private String playerName;

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

	public ChatUser(String name) {
		this.playerName = name;
		this.currentRegion = Region.getLocationRegion(this.getPlayer().getLocation());
	}

	/**
	 * Gets the Player.
	 * 
	 * @return the Player
	 */
	public Player getPlayer() {
		return Bukkit.getPlayerExact(playerName);
	}

	/**
	 * Gets the name of the Player.
	 * 
	 * @return the Player
	 */
	public String getPlayerName() {
		return this.playerName;
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
		SblockUser user = SblockUser.getUser(this.playerName);
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
		if (!c.getListening().contains(this.playerName)) {
			c.sendToAll(this, ChatMsgs.onChannelJoin(this, c), "channel");
			c.addListening(this.playerName);
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
				c.addListening(playerName);
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
				c.removeListening(this.getPlayerName());
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
			c.removeListening(this.getPlayerName());
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
		SblockUser user = SblockUser.getUser(this.playerName);
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
	public void chat(AsyncPlayerChatEvent event) {
		// receives message from SblockChatListener
		// determine channel. if message doesn't begin with @$channelname, then
		// this.current confirm destination channel

		// confirm user has perm to send to channel (channel.cansend()) and also
		// muteness
		// output of channel, string

		ChatUser sender = ChatUserManager.getUserManager().getUser(event.getPlayer().getName());

		if (sender.isMute()) {
			sender.sendMessage(ChatMsgs.isMute());
			return;
		}

		String msg = event.getMessage();
		Channel sendto = SblockChat.getChat().getChannelManager().getChannel(sender.current);

		if (msg.indexOf("@") == 0 && msg.indexOf(" ") > 1) {
			// Check for alternate channel destination. Failing that, warn user.
			int space = msg.indexOf(" ");
			String newChannel = msg.substring(1, space);
			if (SblockChat.getChat().getChannelManager().isValidChannel(newChannel)) {
				sendto = SblockChat.getChat().getChannelManager().getChannel(newChannel);
				if (sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(sender)) {
					// User not approved in channel
					sender.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sendto.getName()));
					return;
				} else {
					// should reach this point for publicchannel and approved users
					msg = msg.substring(space + 1);
					if (msg.length() == 0) {
						// Do not display blank messages for @<channel> with no message
						return;
					}
				}
			} else {
				// Invalid channel specified
				sender.sendMessage(ChatMsgs.errorInvalidChannel(newChannel));
				return;
			}
		} else if (current == null) {
			sender.sendMessage(ChatMsgs.errorNoCurrent());
			event.setCancelled(true);
			return;
		}
		
		if (sendto instanceof RPChannel) {
			if (!sendto.hasNick(sender)) {
				sender.sendMessage(ChatMsgs.errorNickRequired(sendto.getName()));
				return;
			}
		}
		this.formatMessage(sender, sendto, msg);
	}

	/**
	 * Format a chat message for sending to a Channel.
	 * 
	 * @param sender the SblockUser speaking
	 * @param c the Channel to send the message to
	 * @param s the message to send
	 */
	public void formatMessage(ChatUser sender, Channel c, String s) {
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

		boolean isThirdPerson = s.charAt(0) == '#';

		if (!isThirdPerson) {
			channelF = this.getOutputChannelF(sender, c);
		}
		if (isThirdPerson) {
			s = s.substring(1);
		}
		nameF = this.getOutputNameF(sender, isThirdPerson, c);
		
		if(c.getType() == ChannelType.RP) {
			//apply quirk to s
			s = CanonNicks.getNick(c.getNick(sender)).applyQuirk(s);
		} else if (c.isChannelMod(sender)) {
			// color formatting
			s = ChatColor.translateAlternateColorCodes('&', s);
		}
		output = channelF + nameF + s;
		if (isThirdPerson) {
			c.sendToAll(sender, output, "me");
		} else {
			c.sendToAll(sender, output, "chat");
		}
	}

	/**
	 * Send a message from a Channel to this Player.
	 * 
	 * @param s the message to send
	 * @param c the Channel to send to.
	 * @param type the type of chat for handling purposes
	 */
	@SuppressWarnings("deprecation")
	public void sendMessageFromChannel(String s, Channel c, String type) {
		Player p = this.getPlayer();
		if (p == null) {
			SblockData.getDB().saveUserData(playerName);
			c.removeListening(playerName);
			return;
		}
		// final output, sends message to user
		// alert for if its player's name is applied here i.e. {!}
		// then just send it and be done!
		switch (type) {
		case "chat":
			int nameStart = s.toLowerCase().indexOf(this.playerName.toLowerCase());
			if (nameStart > 10 + this.playerName.length() + c.getName().length()) {
				s = s.replaceFirst("\\] .{2}?<.{2}?([\\w]+)", ChatColor.BLUE + "{!}$1");
				p.playEffect(p.getLocation(), Effect.BOW_FIRE, 0);
			}
		case "me":
		case "channel":
		default:
			p.sendMessage(s);
			break;
		}
		// this.getPlayer().sendMessage(s);
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
		out = ChatColor.WHITE + "[" + color + channel.getName() + ChatColor.WHITE + "] ";
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
		String outputName = sender.getGlobalNick();
		switch (c.getType()) {
		case RP:
		case NICK:
			if (c.hasNick(sender)) {
				outputName = c.getNick(sender);
			}
			break;
		default:
			break;
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
			if(c.hasNick(sender)) {
				colorP = CanonNicks.getNick(outputName).getColor();
			} else {
				sender.sendMessage(ChatMsgs.errorNickRequired(c.getName()));
			}
		}
		colorW = Region.getRegionColor(getPlayerRegion());

		out = (isThirdPerson ? ChatColor.BLUE + "> " : colorW + "<") + colorP + outputName
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
				txt + this.playerName + div + sUser.getClassType() + " of " + sUser.getAspect() + "\n" + 
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
		return ChatUserManager.getUserManager().getUser(userName);
	}

	/**
	 * Check to see if a Player by the specified name has played before.
	 * 
	 * @param name the name to check
	 * 
	 * @return true if a Player by the specified name has logged into the server
	 */
	public static boolean isValidUser(String name) {
		return Bukkit.getOfflinePlayer(name).hasPlayedBefore();
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
