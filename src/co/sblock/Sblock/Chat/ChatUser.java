package co.sblock.Sblock.Chat;

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

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.CanonNicks;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.ChannelType;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.SblockEffects.EffectsModule;
import co.sblock.Sblock.SblockEffects.PassiveEffect;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.UserData.UserManager;

/**
 * <code>ChatUser</code> is the class for storing all chat-related
 * <code>Player</code> data.
 * 
 * @author Jikoo, Dublek
 */
public class ChatUser {

	/** The <code>Player</code> */
	private String playerName;

	/** The name of the <code>Player</code>'s current focused <code>Channel</code> */
	private String current;

	/** The channels the <code>Player</code> is listening to */
	private Set<String> listening = new HashSet<String>();

	/** <code>true</code> if the <code>Player</code> is muted */
	private boolean globalMute;

	/** Map of task ID's. Key = <code>Channel</code> name, value = task ID. */
	private Map<String, Integer> tasks = new HashMap<String, Integer>();
	
	/** Keeps track of current <code>Region</code> for <code>RegionChannel</code> purposes */
	private Region currentRegion;
	
	/** Is the Player within range of a Computer? */
	private boolean computerAccess;

	public ChatUser(String name) {
		this.playerName = name;
		if (this.current == null || listening.size() == 0) {
			this.syncSetCurrentChannel("#");
		}
	}

	/**
	 * Gets the <code>Player</code>.
	 * 
	 * @return the <code>Player</code>
	 */
	public Player getPlayer() {
		return Bukkit.getPlayerExact(playerName);
	}

	/**
	 * Gets the name of the <code>Player</code>.
	 * 
	 * @return the <code>Player</code>
	 */
	public String getPlayerName() {
		return this.playerName;
	}

	/**
	 * Gets the <code>Player</code>'s current <code>Region</code>
	 * @return the <code>Region</code> that the <code>Player</code> is in
	 */
	public Region getPlayerRegion() {
		return Region.getLocationRegion(this.getPlayer().getLocation());
	}


	/*
	 * CHAT & RELATED START
	 */

	/**
	 * Check to see if the <code>Player</code> is within range of a
	 * <code>Computer</code>.
	 * 
	 * @return <code>true</code> if the <code>Player</code> is within 10 meters
	 *         of a <code>Computer</code>.
	 */
	public boolean hasComputerAccess() {
		if (EffectsModule.getInstance().getEffectManager().scan(this.getPlayer()).contains("Computer")) {
			return true;
		}
		for (Location l : MachineModule.getInstance().getManager().getMachines(MachineType.COMPUTER)) {
			// distanceSquared <= maximumDistance^2. In this case, maximumDistance = 10.
			if (this.getPlayer().getLocation().distanceSquared(l) <= 100) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the <code>Player</code>'s global nickname.
	 * 
	 * @return the <code>Player</code>'s global nickname
	 */
	public String getGlobalNick() {
		return this.getPlayer().getDisplayName();
	}

	/**
	 * Sets the Player's nickname.
	 * 
	 * @param newNick
	 *            the new nickname for the <code>Player</code>
	 */
	public void setGlobalNick(String newNick) {
		this.getPlayer().setDisplayName(newNick);
	}

	/**
	 * Sets the <code>Player</code>'s chat mute status and sends corresponding
	 * message.
	 * 
	 * @param b
	 *            <code>true</code> if the <code>Player</code> is being muted
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
	 * Gets the <code>Player</code>'s mute status.
	 * 
	 * @return <code>true</code> if the <code>Player</code> is muted
	 */
	public boolean isMute() {
		return globalMute;
	}

	/**
	 * Sets the <code>Player</code>'s current <code>Channel</code>.
	 * 
	 * @param c
	 *            the <code>Channel</code> to set as current
	 */
	public void setCurrent(Channel c) {
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"));
			return;
		}
		if (c.isBanned(this)) {
			this.getPlayer().sendMessage(ChatMsgs.isBanned(c));
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c));
			return;
		}
		if (!this.isListening(c)) {
			this.addListening(c);
		}
		this.current = c.getName();
		this.sendMessage(ChatMsgs.onChannelJoin(this, this.getCurrent()));
	}

	/**
	 * Gets the <code>Channel</code> the <code>Player</code> is currently
	 * sending messages to.
	 * 
	 * @return <code>Channel</code>
	 */
	public Channel getCurrent() {
		return ChatModule.getChatModule().getChannelManager().getChannel(current);
	}

	/**
	 * Adds a <code>Channel</code> to the <code>Player</code>'s current
	 * <code>List</code> of <code>Channel</code>s listened to.
	 * 
	 * @param c
	 *            the <code>Channel</code> to add
	 * @return true if the <code>Channel</code> was added
	 */
	public boolean addListening(Channel c) {
		if (c == null) {
			return false;
		}
		if (c.isBanned(this)) {
			this.getPlayer().sendMessage(ChatMsgs.isBanned(c));
			return false;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c));
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
	 * Remove a <code>Channel</code> from the <code>Player</code>'s listening
	 * <code>List</code>.
	 * 
	 * @param cName
	 *            the name of the <code>Channel</code> to remove
	 */
	public void removeListening(String cName) {
		Channel c = ChatModule.getChatModule().getChannelManager().getChannel(cName);
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
	 * Tells a <code>Channel</code> the <code>Player</code> is leaving on quit.
	 * 
	 * @param cName
	 *            the name of the <code>Channel</code> to inform
	 */
	public void removeListeningQuit(String cName) {
		Channel c = ChatModule.getChatModule().getChannelManager()
				.getChannel(cName);
		if (c != null) {
			c.sendToAll(this, ChatMsgs.onChannelLeave(this, c), "channel");
			c.removeListening(this.getPlayerName());
		}
	}

	/**
	 * Gets the <code>Set</code> of names of <code>Channel</code>s that the
	 * <code>Player</code> is listening to.
	 * 
	 * @return a <code>Set<String></code> of <code>Channel</code> names.
	 */
	public Set<String> getListening() {
		return listening;
	}

	/**
	 * Check if the <code>Player</code> is listening to a specific
	 * <code>Channel</code>.
	 * 
	 * @param c
	 *            the <code>Channel</code> to check for
	 * @return <code>true</code> if the <code>Player</code> is listening to c
	 */
	public boolean isListening(Channel c) {
		return listening.contains(c.getName());
	}

	/**
	 * Gets the <code>Player</code>'s current <code>Region</code>.
	 * 
	 * @return the <code>Region</code> the <code>Player</code> is in.
	 */
	public Region getCurrentRegion() {
		return currentRegion;
	}

	/**
	 * Set the <code>Player</code>'s current <code>Region</code> to the
	 * specified <code>Region</code>. Only for use on login.
	 * 
	 * @see #updateCurrentRegion
	 * 
	 * @param r
	 *            the <code>Region</code> to set
	 */
	public void setCurrentRegion(Region r) {
		currentRegion = r;
	}

	/**
	 * Update current <code>Region</code> and change <code>RegionChannel</code>.
	 * 
	 * @param newR
	 *            the <code>Region</code> being transitioned into
	 */
	public void updateCurrentRegion(Region newR) {
		Channel oldC = ChannelManager.getChannelManager().getChannel("#" + this.getCurrentRegion().toString());
		Channel newC = ChannelManager.getChannelManager().getChannel("#" + newR.toString());
		if (current.equals(oldC.getName())) {
			current = newC.getName();
		}
		this.removeListening(oldC.getName());
		this.addListening(ChannelManager.getChannelManager().getChannel("#" + newR.toString()));
		currentRegion = newR;
	}

	public boolean getComputerAccess() {
		if (!ChatModule.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		return computerAccess;
	}

	public void setComputerAccess() {
		if (EffectsModule.getInstance().getEffectManager().scan(this.getPlayer()).contains(PassiveEffect.COMPUTER)
				|| MachineModule.getInstance().getManager().isByComputer(getPlayer(), 10)) {
			computerAccess = true;
		} else {
			computerAccess = false;
		}
	}
	

	// -----------------------------------------------------------------------------------------------------------------------

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

		ChatUser sender = ChatUserManager.getUserManager().getUser(event.getPlayer().getName());
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
			//sender.sendMessage("\"" + newChannel + "\"");
			if (ChatModule.getChatModule().getChannelManager().isValidChannel(newChannel)) {
				sendto = ChatModule.getChatModule().getChannelManager().getChannel(newChannel);
				if (sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(sender)) {
					// User not approved in channel
					sender.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sendto));
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
		
		if(sendto instanceof RPChannel)	{
			if(!sendto.hasNick(sender))	{
				sender.sendMessage(ChatMsgs.errorNickRequired(sendto.getName()));
				return;
			}
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

		boolean isThirdPerson = (s.charAt(0) == '\u0023') ? true : false;

		if (!isThirdPerson) {
			channelF = this.getOutputChannelF(sender, c);
		}
		if (isThirdPerson) {
			s = s.substring(1);
		}
		nameF = this.getOutputNameF(sender, isThirdPerson, c);
		
		if(c.getType().equals(ChannelType.RP)) {
			//apply quirk to s
			s = CanonNicks.getNick(c.getNick(sender)).applyQuirk(s);
		}
		else if (c.isChannelMod(sender)) {
			// color formatting
			s = ChatColor.translateAlternateColorCodes('\u0026', s);
		}
		output = channelF + nameF + s;
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
	public String getOutputChannelF(ChatUser sender, Channel channel) {
		// colors for [$channel] applied here
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
	public String getOutputNameF(ChatUser sender, boolean isThirdPerson, Channel c) {
		// colors for <$name> applied here
		String out = "";
		String outputName = sender.getGlobalNick();
		  switch (c.getType()) {
		  case RP:
		  case NICK:
		   if (c.hasNick(sender)) {
		    outputName = c.getNick(sender);
		    break;
		   }
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
			if(c.hasNick(sender))	{
				colorP = CanonNicks.valueOf(outputName).getColor();
			}
			else	{
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
	 * Sends a message to the <code>Player</code>.
	 * 
	 * @param string
	 *            the message to send
	 */
	public void sendMessage(String string) {
		this.getPlayer().sendMessage(string);
	}

	/**
	 * Important <code>SblockUser</code> data formatted to be easily readable
	 * when printed.
	 * 
	 * @return a representation of the most important data stored by this
	 *         <code>SblockUser</code>
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		ChatColor sys = ChatColor.DARK_AQUA;
		ChatColor txt = ChatColor.YELLOW;
		String div = sys + ", " + txt;
		SblockUser sUser = UserManager.getUserManager().getUser(getPlayerName());
		
		String s = sys + "-----------------------------------------\n" + 
				txt + this.playerName + div + sUser.getClassType() + " of " + sUser.getAspect() + "\n" + 
				sUser.getMPlanet() + div + sUser.getDPlanet() + div + sUser.getTower() + div + sUser.isSleeping() + "\n" + 
				this.isMute() + div + this.getCurrent().getName() + div + this.getListening().toString() + "\n" +
				this.getCurrentRegion().toString() + div + sUser.getPreviousLocationString() + "\n" +
				sUser.getUserIP() + "\n" +
				sUser.getTimePlayed() + div + sUser.getPlayer().getLastPlayed() + "\n" +
				sys + "-----------------------------------------";
		return s;
	}

	/**
	 * Gets a <code>ChatUser</code> by <code>Player</code> name.
	 * 
	 * @param userName
	 *            the name to match
	 * @return the <code>ChatUser</code> specified or <code>null</code> if
	 *         invalid.
	 */
	public static ChatUser getUser(String userName) {
		return ChatUserManager.getUserManager().getUser(userName);
	}

	/**
	 * Check to see if a <code>Player</code> by the specified name has played
	 * before.
	 * 
	 * @param name
	 *            the name to check
	 * @return <code>true</code> if a <code>Player</code> by the specified name
	 *         has logged into the server
	 */
	public static boolean isValidUser(String name) {
		return Bukkit.getOfflinePlayer(name).hasPlayedBefore();
	}

	/**
	 * Stop all pending tasks for this <code>SblockUser</code>.
	 */
	public void stopPendingTasks() {
		for (int task : tasks.values()) {
			Bukkit.getScheduler().cancelTask(task);
		}
	}

	/**
	 * Join a <code>Channel</code> synchronously. <code>BukkitTask</code> ID is
	 * added to task list for cleanup. For use on login - instantly causing a
	 * join results in failure for unknown reasons.
	 * 
	 * @param channelName
	 *            the name of the <code>Channel</code> to join
	 */
	public void syncJoinChannel(String channelName) {
		tasks.put(channelName, Bukkit.getScheduler()
				.scheduleSyncDelayedTask(Sblock.getInstance(),
						new ChannelJoinSynchronizer(this, channelName)));
	}

	/**
	 * A small <code>Runnable</code> used to enter a <code>Channel</code>
	 * synchronously.
	 */
	public class ChannelJoinSynchronizer implements Runnable {
		/** The name of the <code>Channel</code> to join */
		private String channelName;
		/** The <code>SblockUser</code> who is joining */
		private ChatUser user;

		/**
		 * Constructor for <code>ChannelJoinSynchronizer</code>.
		 * 
		 * @param user
		 *            the <code>SblockUser</code> joining a <code>Channel</code>
		 * @param channelName
		 *            the name of the <code>Channel</code> to join
		 */
		public ChannelJoinSynchronizer(ChatUser user, String channelName) {
			this.user = user;
			this.channelName = channelName;
		}

		/**
		 * Join the <code>Channel</code> and clean up entry from
		 * <code>BukkitTask</code> list.
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			Channel c = ChatModule.getChatModule()
					.getChannelManager().getChannel(channelName);
			if (c != null && !user.isListening(c) && user.getPlayer().isOnline()) {
				user.addListening(c);
			}
			tasks.remove(channelName);
		}
	}

	/**
	 * Set a <code>Channel</code> to current synchronously.
	 * <code>BukkitTask</code> ID is added to task list for cleanup. For use on
	 * login - instantly causing a join results in failure for unknown reasons.
	 * 
	 * @param channelName
	 *            the name of the <code>Channel</code> to join
	 */
	public void syncSetCurrentChannel(String channelName) {
		tasks.put(channelName, Bukkit.getScheduler()
				.scheduleSyncDelayedTask(Sblock.getInstance(),
						new ChannelSetCurrentSynchronizer(this, channelName)));
	}

	/**
	 * A small <code>Runnable</code> used to set the current
	 * <code>Channel</code> synchronously.
	 */
	public class ChannelSetCurrentSynchronizer implements Runnable {
		/** The name of the <code>Channel</code> to join */
		private String channelName;
		/** The <code>SblockUser</code> who is joining */
		private ChatUser user;

		/**
		 * Constructor for <code>ChannelSetCurrentSynchronizer</code>.
		 * 
		 * @param user
		 *            the <code>SblockUser</code> joining a <code>Channel</code>
		 * @param channelName
		 *            the name of the <code>Channel</code> to join
		 */
		public ChannelSetCurrentSynchronizer(ChatUser user, String channelName) {
			this.user = user;
			this.channelName = channelName;
		}

		/**
		 * Set current and clean up entry from <code>BukkitTask</code> list.
		 * 
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			Channel c = ChatModule.getChatModule()
					.getChannelManager().getChannel(channelName);
			if (c != null && user.getPlayer().isOnline()) {
				user.setCurrent(c);
			}
			tasks.remove(channelName);
		}
	}
}
