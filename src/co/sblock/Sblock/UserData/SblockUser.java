package co.sblock.Sblock.UserData;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.CanonNicks;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ColorDef;
import co.sblock.Sblock.Chat.ChatModule;
import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.SblockEffects.EffectsModule;
import co.sblock.Sblock.SblockEffects.PassiveEffect;

/**
 * <code>SblockUser</code> is the class for storing all <code>Player</code>
 * data.
 * 
 * @author Jikoo, Dublek, FireNG
 */
public class SblockUser {

	/** The <code>Player</code> */
	private String playerName;

	/** The <code>Player</code>'s chosen class */
	private UserClass classType = UserClass.HEIR;

	/** The <code>Player</code>'s chosen aspect */
	private UserAspect aspect = UserAspect.BREATH;

	/** The <code>Player</code>'s chosen Medium planet. */
	private MediumPlanet mPlanet = MediumPlanet.LOWAS;

	/** The <code>Player</code>'s chosen dream planet. */
	private DreamPlanet dPlanet = DreamPlanet.PROSPIT;

	/** The <code>Player</code>'s tower number */
	private byte tower = (byte)(8 * Math.random());

	/** <code>true</code> if the <code>Player</code> is in dreamstate */
	private boolean sleeping = false;

	/** The <code>Player</code>'s location prior to sleeping to swap worlds */
	private Location previousLocation;

	/** The name of the <code>Player</code>'s current focused <code>Channel</code> */
	private String current;

	/** The channels the <code>Player</code> is listening to */
	private Set<String> listening = new HashSet<String>();

	/** <code>true</code> if the <code>Player</code> is muted */
	private boolean globalMute;

	/** The <code>Player</code>'s global nickname */
	private String globalNick;

	/** The total time the <code>Player</code> has spent logged in */
	private long timePlayed = 0L;

	/** The <code>Player</code>'s last login */
	private Date login;

	/** The <code>Player</code>'s IP address */
	private String userIP;

	/** Map of task ID's. Key = <code>Channel</code> name, value = task ID. */
	private Map<String, Integer> tasks = new HashMap<String, Integer>();
	
	/** Keeps track of current <code>Region</code> for <code>RegionChannel</code> purposes */
	private Region currentRegion;
	
	/** Is the Player within range of a Computer? */
	private boolean computerAccess;

	/** Programs installed to the player's computer */
	private Set<Integer> programs = new HashSet<Integer>();

	/** UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing */
	private byte uhc = 1;

	/**
	 * Creates a <code>SblockUser</code> object for a <code>Player</code>.
	 * 
	 * @param playerName
	 *            the name of the <code>Player</code> to create a <code>SblockUser</code> for
	 */
	public SblockUser(String playerName) {
		this.playerName = playerName;
		this.globalNick = playerName;
		login = new Date();
		DatabaseManager.getDatabaseManager().loadUserData(this);
		if (this.current == null || listening.size() == 0) {
			this.syncSetCurrentChannel("#");
		}
		this.setUserIP();
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
	 * Gets the <code>OfflinePlayer</code>.
	 * 
	 * @return the <code>OfflinePlayer</code>
	 */
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(playerName);
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
	 * Gets the <code>Player</code>'s chosen <code>UserClass</code>.
	 * 
	 * @return the <code>UserClass</code>, <code>null</code> if unchosen
	 */
	public UserClass getClassType() {
		return this.classType;
	}

	/**
	 * Gets the <code>Player</code>'s chosen <code>UserAspect</code>.
	 * 
	 * @return the <code>UserAspect</code>, <code>null</code> if unchosen
	 */
	public UserAspect getAspect() {
		return this.aspect;
	}

	/**
	 * Gets the <code>Player</code>'s chosen <code>MediumPlanet</code>.
	 * 
	 * @return the <code>Player</code>'s <code>MediumPlanet</code>
	 */
	public MediumPlanet getMPlanet() {
		return this.mPlanet;
	}

	/**
	 * Gets the <code>Player</code>'s chosen <code>DreamPlanet</code>.
	 * 
	
	 * @return the <code>Player</code>'s <code>DreamPlanet</code>
	 */
	public DreamPlanet getDPlanet() {
		return this.dPlanet;
	}

	/**
	 * Gets the tower number generated for the <code>Player</code>.
	 * 
	 * 
	 * @return the number of the tower the player will "dream" to
	 */
	public byte getTower() {
		return this.tower;
	}

	/**
	 * Gets the <code>Player</code>'s dreamstate.
	 * 
	 * 
	 * @return <code>true</code> if the <code>Player</code> is in dreamstate
	 */
	public boolean isSleeping() {
		return this.sleeping;
	}

	/**
	 * Gets the <code>Player</code>'s current <code>Region</code>
	 * @return the <code>Region</code> that the <code>Player</code> is in
	 */
	public Region getPlayerRegion() {
		return Region.getLocationRegion(this.getPlayer().getLocation());
	}

	/**
	 * Sets the <code>Player</code>'s <code>UserClass</code>.
	 * 
	 * @param uclass
	 *            the new <code>UserClass</code>
	 */
	public void setPlayerClass(String uclass) {
		this.classType = UserClass.getClass(uclass);
	}

	/**
	 * Sets the <code>Player</code>'s <code>UserAspect</code>.
	 * 
	 * @param aspect
	 *            the new <code>UserAspect</code>
	 */
	public void setAspect(String aspect) {
		this.aspect = UserAspect.getAspect(aspect);
	}

	/**
	 * Sets the <code>Player</code>'s <code>MediumPlanet</code>.
	 * 
	 * @param mPlanet
	 *            the new <code>MediumPlanet</code>
	 */
	public void setMediumPlanet(String mPlanet) {
		this.mPlanet = MediumPlanet.getPlanet(mPlanet);
	}

	/**
	 * Sets the <code>Player</code>'s <code>DreamPlanet</code>.
	 * 
	 * @param dPlanet
	 *            the new <code>DreamPlanet</code>
	 */
	public void setDreamPlanet(String dPlanet) {
		this.dPlanet = DreamPlanet.getPlanet(dPlanet);
	}

	/**
	 * Sets the tower number generated for the <code>Player</code>.
	 * 
	 * @param tower
	 *            the number of the tower the <code>Player</code> will "dream" to
	 */
	public void setTower(byte tower) {
		this.tower = tower;
	}

	/**
	 * Sets the <code>Player</code>'s dreamstate.
	 * 
	 * @param sleeping
	 *            <code>true</code> if the <code>Player</code> is in dreamstate
	 */
	public void setIsSleeping(boolean sleeping) {
		this.sleeping = sleeping;
		this.getPlayer().setAllowFlight(sleeping);
		this.getPlayer().setFlying(sleeping);
	}

	/**
	 * Sets the <code>Player</code>'s <code>Location</code> from the last
	 * <code>World</code> that they visited.
	 * 
	 * @param l
	 *            The <code>Player</code>'s previous <code>Location</code>
	 */
	public void setPreviousLocation(Location l) {
		l.setX(l.getBlockX() + .5);
		l.setY(l.getBlockY());
		l.setZ(l.getBlockZ() + .5);
		l.setYaw(l.getYaw() - l.getYaw() % 64);
		l.setPitch(0);
		this.previousLocation = l;

	}

	/**
	 * Sets the <code>Player</code>'s previous <code>Location</code> from a
	 * <code>String</code>. Only for use in <code>DatabaseManager</code>.
	 * 
	 * @param string
	 */
	public void setPreviousLocationFromString(String string) {
		String[] loc = string.split(",");
		World w = Bukkit.getWorld(loc[0]);
		if (w != null) {
			this.previousLocation = new Location(w,
					Double.parseDouble(loc[1]) + .5,
					Double.parseDouble(loc[2]) + .5, // no dreaming though halfslab floors
					Double.parseDouble(loc[3]) + .5);
		} else {
			this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
		}
	}

	/**
	 * Gets the <code>Location</code> of the <code>Player</code> prior to sleep
	 * teleportation.
	 * 
	 * @return the previousLocation
	 */
	public Location getPreviousLocation() {
		return previousLocation;
	}

	/**
	 * The <code>String</code> representation of the <code>Player</code>'s
	 * <code>Location</code> prior to last sleep teleport.
	 * 
	 * @return String
	 */
	public String getPreviousLocationString() {
		return previousLocation.getWorld().getName() + ","
				+ previousLocation.getBlockX() + ","
				+ previousLocation.getBlockY() + ","
				+ previousLocation.getBlockZ();
	}

	/**
	 * Sets the <code>Player</code>'s total time ingame from a
	 * <code>String</code>. For use in <code>DatabaseManager</code> only.
	 * 
	 * @param s
	 *            String
	 */
	public void setTimePlayed(String s) {
		if (s != null) {
			String[] nums = s.split(":");
			for (int i = 0; i < nums.length; i++) {
				switch (i) {
				case 1:
					this.timePlayed += Long.valueOf(nums[nums.length-i]);
					break;
				case 2:
					this.timePlayed += Long.valueOf(nums[nums.length-i]) * 1000L;
					break;
				case 3:
					this.timePlayed += Long.valueOf(nums[nums.length-i]) * 60000L;
					break;
				case 4:
					this.timePlayed += Long.valueOf(nums[nums.length-i]) * 3600000L;
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * The <code>String</code> representation of the <code>Player</code>'s total
	 * time ingame.
	 * 
	 * @return the <code>Player</code>'s time ingame
	 */
	public String getTimePlayed() {
		long hrs = TimeUnit.MILLISECONDS.toHours(this.timePlayed);
		long mins = TimeUnit.MILLISECONDS.toMinutes(timePlayed) -
				TimeUnit.HOURS.toMinutes(hrs);
		long secs = TimeUnit.MILLISECONDS.toSeconds(timePlayed) -
				TimeUnit.HOURS.toSeconds(hrs) - TimeUnit.MINUTES.toSeconds(mins);
		long millis = this.timePlayed - TimeUnit.HOURS.toMillis(hrs) -
				TimeUnit.MINUTES.toMillis(mins) - TimeUnit.SECONDS.toMillis(secs);
		return String.format("%02d:%02d:%02d:%03d", hrs, mins, secs, millis);
	}

	/**
	 * Updates the <code>Player</code>'s total time ingame. For use on logout
	 * only!
	 */
	public void updateTimePlayed() {
		this.timePlayed = this.timePlayed + new Date().getTime()
				- this.login.getTime();
	}

	/**
	 * TODO TODO
	 * 
	 * @return isGodTier
	 */
	public boolean isGodTier() {
		return false;
	}

	/**
	 * Returns the <code>Player</code>'s UHC mode.
	 * <p>
	 * UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing
	 * 
	 * @return the <code>Player</code>'s UHC mode
	 */
	public byte getUHCMode() {
		return uhc;
	}

	/**
	 * Sets the <code>Player</code>'s UHC mode.
	 * <p>
	 * UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing
	 * 
	 * @param b
	 *            the UHC mode to set
	 */
	public void setUHCMode(Byte b) {
		// DB returns 0 if null
		if (b != 0) {
			uhc = b;
		}
	}

	/**
	 * Gets a <code>Set</code> of all <code>Computer</code> programs accessible
	 * by the <code>Player</code>.
	 * 
	 * @return the programs installed
	 */
	public Set<Integer> getPrograms() {
		return this.programs;
	}

	/**
	 * Add an <code>Entry</code> to the <code>Set</code> of programs accessible
	 * by the <code>Player</code> at their <code>Computer</code>.
	 * 
	 * @param i
	 *            the number of the program to add
	 */
	public void addProgram(int i) {
		this.programs.add(i);
	}

	/**
	 * Restore the <code>Player</code>'s installed programs from a
	 * <code>String</code>. For use in <code>DatabaseManager</code> only.
	 * 
	 * @param s
	 *            the string containing programs previously installed
	 */
	public void setPrograms(String s) {
		if (s == null || s.isEmpty()) {
			return;
		}
		for (String s1 : s.split(",")) {
			addProgram(Integer.valueOf(s1));
		}
	}

	/**
	 * Gets a <code>String</code> representation of the <code>Player</code>'s
	 * installed programs.
	 * 
	 * 
	 * @return representation of the contents of programs
	 */
	public String getProgramString() {
		StringBuilder sb = new StringBuilder();
		for (int i : getPrograms()) {
			sb.append(i).append('\u002C');
		}
		if (sb.length() == 0) {
			return null;
		}
		return sb.substring(0, sb.length() - 1);
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
	public String getNick() {
		return globalNick;
	}

	/**
	 * Sets the Player's nickname.
	 * 
	 * @param newNick
	 *            the new nickname for the <code>Player</code>
	 */
	public void setNick(String newNick) {
		this.globalNick = newNick;
	}

	/**
	 * Gets a <code>String</code> representation of the <code>Player</code>'s
	 * IP.
	 * 
	 * @return the <code>Player</code>'s IP
	 */
	public String getUserIP() {
		return userIP;
	}

	/**
	 * Sets the <code>SblockUser</code>'s IP if the <code>Player</code> is
	 * online.
	 */
	public void setUserIP() {
		if (this.getPlayer().isOnline())
			userIP = this.getPlayer().getAddress().getAddress()
					.getHostAddress();
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
			this.sendMessage(ChatMsgs.onUserMute(this));
		} else {
			this.sendMessage(ChatMsgs.onUserUnmute(this));
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
			this.getPlayer().sendMessage(ChatMsgs.isBanned(this, c));
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(this, c));
			return;
		}
		if (!this.isListening(c)) {
			this.addListening(c);
		}
		this.current = c.getName();
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
			this.getPlayer().sendMessage(ChatMsgs.isBanned(this, c));
			return false;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(this, c));
			return false;
		}
		if (!this.isListening(c)) {
			this.listening.add(c.getName());
		}
		if (!c.getListening().contains(this.playerName)) {
			c.addListening(this.playerName);
			c.sendToAll(this, ChatMsgs.onChannelJoin(this, c), "channel");
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
		Channel c = ChatModule.getChatModule().getChannelManager()
				.getChannel(cName);
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(cName));
			this.listening.remove(cName);
			return;
		}
		if (this.listening.remove(cName)) {
				c.sendToAll(this, ChatMsgs.onChannelLeave(this, c),
						"channel");
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
	public boolean getComputerAccess()	{
		return computerAccess;
	}
	public void setComputerAccess()	{
		if(EffectsModule.getInstance().getEffectManager().scan(this.getPlayer()).contains(PassiveEffect.COMPUTER
				/* || distance to Player's Computer Machine <= 10*/))	{
			computerAccess = true;
		}
		else	{
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
		
		String s = sys + "-----------------------------------------\n" + 
				txt + this.playerName + div + this.getClassType() + " of " + this.getAspect() + "\n" + 
				this.getMPlanet() + div + this.getDPlanet() + div + this.getTower() + div + this.isSleeping() + "\n" + 
				this.isMute() + div + this.getCurrent().getName() + div + this.getListening().toString() + "\n" +
				this.getCurrentRegion().toString() + div + this.getPreviousLocationString() + "\n" +
				this.getUserIP() + "\n" +
				this.getTimePlayed() + div + this.getPlayer().getLastPlayed() + "\n" +
				sys + "-----------------------------------------";
		return s;
	}

	/**
	 * Gets a <code>SblockUser</code> by <code>Player</code> name.
	 * 
	 * @param userName
	 *            the name to match
	 * @return the <code>SblockUser</code> specified or <code>null</code> if
	 *         invalid.
	 */
	public static SblockUser getUser(String userName) {
		return UserManager.getUserManager().getUser(userName);
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
		private SblockUser user;

		/**
		 * Constructor for <code>ChannelJoinSynchronizer</code>.
		 * 
		 * @param user
		 *            the <code>SblockUser</code> joining a <code>Channel</code>
		 * @param channelName
		 *            the name of the <code>Channel</code> to join
		 */
		public ChannelJoinSynchronizer(SblockUser user, String channelName) {
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
		private SblockUser user;

		/**
		 * Constructor for <code>ChannelSetCurrentSynchronizer</code>.
		 * 
		 * @param user
		 *            the <code>SblockUser</code> joining a <code>Channel</code>
		 * @param channelName
		 *            the name of the <code>Channel</code> to join
		 */
		public ChannelSetCurrentSynchronizer(SblockUser user, String channelName) {
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
