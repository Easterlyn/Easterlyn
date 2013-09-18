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
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelManager;
import co.sblock.Sblock.Chat.Channel.RPChannel;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Chat.ColorDef;
import co.sblock.Sblock.Chat.ChatModule;

/**
 * <code>SblockUser</code> is the class for storing all <code>Player</code>
 * data.
 * 
 * @author Jikoo
 * @author Dublek
 * @author FireNG
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

	/** The <code>Player</code>'s current focused channel */
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

	/** Map of task ID's. Key = channel name, value = task ID. */
	private Map<String, Integer> tasks = new HashMap<String, Integer>();
	
	/** Keeps track of current region for RegionChannel purposes */
	private Region currentRegion;

	/**
	 * Creates a SblockUser object for a player.
	 * 
	 * @param p
	 *            the <code>Player</code> to load data for
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
	 * Gets the <code>Player</code>'s chosen class
	 * 
	 * @return the class type, <code>null</code> if unchosen
	 */
	public UserClass getClassType() {
		return this.classType;
	}

	/**
	 * Gets the <code>Player</code>'s chosen aspect
	 * 
	 * @return the aspect, <code>null</code> if unchosen
	 */
	public UserAspect getAspect() {
		return this.aspect;
	}

	/**
	 * Gets the <code>Player</code>'s chosen Medium planet.
	 * 
	 * @return the <code>Player</code>'s Medium planet
	 */
	public MediumPlanet getMPlanet() {
		return this.mPlanet;
	}

	/**
	 * Gets the <code>Player</code>'s chosen dream planet.
	 * 
	 * @return the <code>Player</code>'s dream planet
	 */
	public DreamPlanet getDPlanet() {
		return this.dPlanet;
	}

	/**
	 * Gets the tower number generated for the <code>Player</code>
	 * 
	 * @return the number of the tower the player will "dream" to
	 */
	public byte getTower() {
		return this.tower;
	}

	/**
	 * Gets the <code>Player</code>'s dreamstate
	 * 
	 * @return <code>true</code> if the <code>Player</code> is in dreamstate
	 */
	public boolean isSleeping() {
		return this.sleeping;
	}

	public Region getPlayerRegion() {
		return Region.getLocationRegion(this.getPlayer().getLocation());
	}

	/**
	 * Sets the UserClass type.
	 * 
	 * @param uclass
	 *            the new UserClass type
	 */
	public void setPlayerClass(String uclass) {
		this.classType = UserClass.getClass(uclass);
	}

	/**
	 * Sets the UserAspect.
	 * 
	 * @param aspect
	 *            the new UserAspect
	 */
	public void setAspect(String aspect) {
		this.aspect = UserAspect.getAspect(aspect);
	}

	/**
	 * Sets the MediumPlanet.
	 * 
	 * @param mPlanet
	 *            the new MediumPlanet
	 */
	public void setMediumPlanet(String mPlanet) {
		this.mPlanet = MediumPlanet.getPlanet(mPlanet);
	}

	/**
	 * Sets the DreamPlanet.
	 * 
	 * @param dPlanet
	 *            the new DreamPlanet
	 */
	public void setDreamPlanet(String dPlanet) {
		this.dPlanet = DreamPlanet.getPlanet(dPlanet);
	}

	/**
	 * Sets the tower number generated for the <code>Player</code>
	 * 
	 * @param tower
	 *            the number of the tower the player will "dream" to
	 */
	public void setTower(byte tower) {
		this.tower = tower;
	}

	/**
	 * Sets the <code>Player</code>'s dreamstate
	 * 
	 * @param sleeping
	 *            <code>true</code> if the <code>Player</code> is in dreamstate
	 */
	public void setIsSleeping(boolean sleeping) {
		this.sleeping = sleeping;
		this.getPlayer().setAllowFlight(sleeping);
	}

	/**
	 * Sets the player's location from the last world that they visited.
	 * 
	 * @param l
	 *            The player's previous location
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
	 * @return the previousLocation
	 */
	public Location getPreviousLocation() {
		return previousLocation;
	}

	/**
	 * @return
	 */
	public String getPreviousLocationString() {
		return previousLocation.getWorld().getName() + ","
				+ previousLocation.getBlockX() + ","
				+ previousLocation.getBlockY() + ","
				+ previousLocation.getBlockZ();
	}

	/**
	 * @return
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
	 * @return
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
	 * 
	 */
	public void updateTimePlayed() {
		this.timePlayed = this.timePlayed + new Date().getTime()
				- this.login.getTime();
	}

	/**
	 * TODO TODO
	 * @return isGodTier
	 */
	public boolean isGodTier() {
		return false;
	}

	/*
	 * CHAT & RELATED START
	 */

	/**
	 * @return the <code>Player</code>'s global nickname
	 */
	public String getNick() {
		return globalNick;
	}

	/**
	 * @param newNick
	 *            the new nickname for the <code>Player</code>
	 */
	public void setNick(String newNick) {
		this.globalNick = newNick;
	}

	/**
	 * @return the User's IP
	 */
	public String getUserIP() {
		return userIP;
	}

	/**
	 * Sets the User's IP if the player is online
	 */
	public void setUserIP() {
		if (this.getPlayer().isOnline())
			userIP = this.getPlayer().getAddress().getAddress()
					.getHostAddress();
	}

	public void setMute(boolean b) {
		this.globalMute = b;
		if (b) {
			this.sendMessage(ChatMsgs.onUserMute(this));
		} else {
			this.sendMessage(ChatMsgs.onUserUnmute(this));
		}
	}

	public boolean isMute() {
		return globalMute;
	}

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

	public Channel getCurrent() {
		return ChatModule.getChatModule().getChannelManager().getChannel(current);
	}

	public boolean addListening(Channel c) {
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"));
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

	public void removeListening(String cName) {
		Channel c = ChatModule.getChatModule().getChannelManager()
				.getChannel(cName);
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(cName));
			this.listening.remove(cName);
			return;
		}
		if (this.listening.remove(c)) {
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
	 * Tell channel player is leaving on quit
	 * 
	 * @param cName
	 *            the name of the channel
	 */
	public void removeListeningQuit(String cName) {
		Channel c = ChatModule.getChatModule().getChannelManager()
				.getChannel(cName);
		if (c != null) {
			c.sendToAll(this, ChatMsgs.onChannelLeave(this, c), "channel");
			c.removeListening(this.getPlayerName());
		}
	}

	public Set<String> getListening() {
		return listening;
	}

	public boolean isListening(Channel c) {
		return listening.contains(c.getName());
	}
	public Region getCurrentRegion() {
		return currentRegion;
	}
	public void setCurrentRegion(Region r) {
		currentRegion = r;
	}
	public void updateCurrentRegion(Region newR) {
		Channel oldC = ChannelManager.getChannelManager().getChannel("#" + this.getCurrentRegion().toString());
		Channel newC = ChannelManager.getChannelManager().getChannel("#" + newR.toString());
		if(current.equalsIgnoreCase(oldC.getName())) {
			current = newC.getName();
		}
		this.removeListening(oldC.getName());
		this.addListening(ChannelManager.getChannelManager().getChannel("#" + newR.toString()));
		currentRegion = newR;
	}

	// -----------------------------------------------------------------------------------------------------------------------

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
				} else { // should reach this point for publicchannel and
							// approved users
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
						+ ChatColor.WHITE + s.substring(s.indexOf(">" + 1));
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

	public String getOutputNameF(SblockUser sender, boolean isThirdPerson,
			Channel c) {
		// colors for <$name> applied here
		// SburbChat code. Handle with care
		String out = "";

		String outputName = c.getNick(this).getName();
		if (c instanceof RPChannel) {
			outputName = c.getNick(this).getColor() + outputName;
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

		colorW = Region.getRegionColor(getPlayerRegion());

		out = (isThirdPerson ? ">" : colorW + "<") + colorP + outputName
				+ ChatColor.WHITE
				+ (isThirdPerson ? "" : colorW + "> " + ChatColor.WHITE);
		// sender.getPlayer().sendMessage(out);
		return out;
	}

	public void sendMessage(String string) {
		this.getPlayer().sendMessage(string);
	}

	public String toString() { // For /whois usage mainly
		// TODONE Someone tell Dub to get off his lazy ass
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

	public static SblockUser getUser(String userName) {
		return UserManager.getUserManager().getUser(userName);
	}

	public static boolean isValidUser(String name) {
		return Bukkit.getOfflinePlayer(name).hasPlayedBefore();
	}

	public void stopPendingTasks() {
		for (int task : tasks.values()) {
			Bukkit.getScheduler().cancelTask(task);
		}
	}

	public void syncJoinChannel(String channelName) {
		tasks.put(channelName, Bukkit.getScheduler()
				.scheduleSyncDelayedTask(Sblock.getInstance(),
						new ChannelJoinSynchronizer(this, channelName)));
	}

	public class ChannelJoinSynchronizer implements Runnable {
		private String channelName;
		private SblockUser user;
		public ChannelJoinSynchronizer(SblockUser user, String channelName) {
			this.user = user;
			this.channelName = channelName;
		}
		@Override
		public void run() {
			Channel c = ChatModule.getChatModule()
					.getChannelManager().getChannel(channelName);
			if (c != null && !user.isListening(c)) {
				user.addListening(c);
			}
			tasks.remove(channelName);
		}
	}

	public void syncSetCurrentChannel(String channelName) {
		tasks.put(channelName, Bukkit.getScheduler()
				.scheduleSyncDelayedTask(Sblock.getInstance(),
						new ChannelSetCurrentSynchronizer(this, channelName)));
	}

	public class ChannelSetCurrentSynchronizer implements Runnable {
		private String channelName;
		private SblockUser user;
		public ChannelSetCurrentSynchronizer(SblockUser user, String channelName) {
			this.user = user;
			this.channelName = channelName;
		}
		@Override
		public void run() {
			Channel c = ChatModule.getChatModule()
					.getChannelManager().getChannel(channelName);
			if (c != null) {
				user.setCurrent(c);
			}
			tasks.remove(channelName);
		}
	}
}
