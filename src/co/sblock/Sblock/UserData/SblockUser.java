package co.sblock.Sblock.UserData;

import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.Sblock.DatabaseManager;
import co.sblock.Sblock.Chat.Channel.AccessLevel;
import co.sblock.Sblock.Chat.Channel.Channel;
import co.sblock.Sblock.Chat.Channel.ChannelType;
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
	private UserClass classType = UserClass.NONE;

	/** The <code>Player</code>'s chosen aspect */
	private UserAspect aspect = UserAspect.NONE;

	/** The <code>Player</code>'s chosen Medium planet. */
	private MediumPlanet mPlanet = MediumPlanet.NONE;

	/** The <code>Player</code>'s chosen dream planet. */
	private DreamPlanet dPlanet = DreamPlanet.NONE;

	/** The <code>Player</code>'s tower number */
	private short tower = -1;

	/** <code>true</code> if the <code>Player</code> is in dreamstate */
	private boolean sleeping = false;

	/** The <code>Player</code>'s location prior to sleeping to swap worlds */
	private Location previousLocation;

	/** The <code>Player</code>'s current focused channel */
	private String current;

	/** The channels the <code>Player</code> is listening to */
	private List<String> listening = new ArrayList<String>();

	/** <code>true</code> if the <code>Player</code> is muted */
	private boolean globalMute;

	/** The <code>Player</code>'s global nickname */
	private String globalNick;

	/** The last time the <code>Player</code> logged out */
	@SuppressWarnings("unused")
	private Timestamp lastLogin;
	// TODO do we need this? if(!getPlayer().isOnline())
	// getOfflinePlayer().getLastPlayed()
	//If that's a function of native OfflinePlayer, then cool. Use that, just make sure it gets saved in the db and is accessible easily here

	/** The total time the <code>Player</code> has spent logged in */
	@SuppressWarnings("unused")
	// TODO
	private Time timePlayed;

	/** The <code>Player</code>'s IP address */
	private String userIP;

	/**
	 * Creates a SblockUser object for a player.
	 * 
	 * @param p
	 *            the <code>Player</code> to load data for
	 */
	public SblockUser(String playerName) {
		this.playerName = playerName;
		DatabaseManager.getDatabaseManager().loadUserData(this);
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
	public short getTower() {
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
	public void setTower(short tower) {
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
	}

	/**
	 * Sets the player's location from the last world that they visited.
	 * 
	 * @param location
	 *            The player's previous location
	 */
	public void setPreviousLocation(Location location) {
		this.previousLocation = location;

	}

	/**
	 * @return the previousLocation
	 */
	public Location getPreviousLocation() {
		return previousLocation;
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
		if (c.isBanned(this)) {	//First make sure User may join channel
			this.getPlayer().sendMessage(ChatMsgs.isBanned(this, c));
			return;
		}
		else if(c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this))	{
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(this, c));
			return;
		}
		if(!this.isListening(c))	{
			this.addListening(c);
		}
		this.current = c.getName();
	}

	public Channel getCurrent() {
		return ChatModule.getInstance().getChannelManager().getChannel(current);
	}

	public boolean addListening(Channel c) {
		if (c == null) {
			//TODO add an error message here
			return false;
		}
		if (c.isBanned(this)) {
			this.getPlayer().sendMessage(ChatMsgs.isBanned(this, c));
			return false;
		}
		if (!this.listening.contains(c)) {
			if (c.userJoin(this)) {
				this.listening.add(c.getName());
				//this.sendMessage(ChatColor.GREEN + "Now listening to channel " + ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
				//Extra spam for the player to read. All in favor of removing it say aye
				return true;
			}
		} else {
			this.sendMessage(ChatColor.RED + "Already listening to channel "
					+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
		}
		return false;
	}

	public void removeListening(Channel c) {
		if (this.listening.contains(c)) {
			if (!this.current.equals(c)) {
				c.userLeave(this);
				this.listening.remove(c);
				this.sendMessage(ChatColor.GREEN + "No longer listening to channel "
				+ ChatColor.GOLD + c.getName() + ChatColor.GREEN + ".");
			} else {
				this.sendMessage(ChatColor.RED + "Cannot leave your current channel "
						+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
			}
		} else {
			this.sendMessage(ChatColor.RED + "Not listening to channel "
					+ ChatColor.GOLD + c.getName() + ChatColor.RED + "!");
		}
	}

	public List<String> getListening() {
		return listening;
	}
	public boolean isListening(Channel c)	{
		return listening.contains(c);
	}

	// -----------------------------------------------------------------------------------------------------------------------

	public void chat(AsyncPlayerChatEvent event) {
		// receives message from SblockChatListener
		// determine channel. if message doesn't begin with @$channelname, then
		// this.current confirm destination channel

		// confirm user has perm to send to channel (channel.cansend()) and also muteness
		// output of channel, string

		SblockUser sender = UserManager.getUserManager().getUser(
				event.getPlayer().getName());
		String fullmsg = event.getMessage();
		String outputmessage = fullmsg;
		Channel sendto = ChatModule.getInstance().getChannelManager()
				.getChannel(sender.current);

		if (fullmsg.indexOf("@") == 0) { // Check for alternate channel destination
			int space = fullmsg.indexOf(" ");
			String newChannel = fullmsg.substring(1, space);
			if (ChatModule.getInstance().getChannelManager().isValidChannel(newChannel)) {
				sendto = ChatModule.getInstance().getChannelManager().getChannel(newChannel);
				if(sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(sender))	{
					//User not approved in channel
					sender.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sender, sendto));
					return;
				}
				else	{	//should reach this point for publicchannel and approved users
					outputmessage = fullmsg.substring(space + 1);
				}
			}
			else	{
				//invalidChannel
				sender.sendMessage(ChatMsgs.errorInvalidChannel(sendto));
				return;
			}
		}
		if (sender.globalMute) {
			sender.sendMessage(ChatMsgs.isMute(sender));
			return;
		}
		//TODO see belowright
	/*		switch (sendto.getAccess()) {									//This part should no longer be necessary
			case PUBLIC:													//If they don't have access, they should be blocked 
			break;															//by this.setCurrent()
		case PRIVATE:
			if (sendto.getApprovedUsers().contains(sender.playerName)) {
				break;
			} else {
				return;
			}
		}
		*/
		// Logger.getLogger("Minecraft").info(sender.getName() + " " +
		// sendto.getName() + " " + outputmessage);
		this.formatMessage(sender, sendto, outputmessage);
	}

	public void formatMessage(SblockUser sender, Channel c, String s) {
		// remember, [$channel]<$player> $message

		// perhaps call getOutputChannelF and getOutputNameF?
		// though I should def include a ColorDefinitons class -- DONE

		// check for a global nick, prolly only occurs if admin is being tricksty

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
		sender.getPlayer().sendMessage(output);
		// This bypass will remain as long as the stupid
		// thing can't tell what it's listening to

		c.sendToAll(sender, output);

	}

	public void sendMessageFromChannel(String s, Channel c) {
		// final output, sends message to user
		// alert for if its player's name is applied here i.e. {!}
		// then just send it and be done!
		this.getPlayer().sendMessage(s);
	}

	// Here begins output formatting. Abandon all hope ye who enter

	public String getOutputChannelF(SblockUser sender, Channel channel) {
		// colors for [$channel] applied here
		// SburbChat code. Handle with care
		String out = "";

		ChatColor color = ColorDef.CHATRANK_MEMBER;
		if (channel.isOwner(sender))	{
			color = ColorDef.CHATRANK_OWNER;
		} else if (channel.isChannelMod(sender)) {
			color = ColorDef.CHATRANK_MOD;
		}
		out = ChatColor.WHITE + "[" + color + channel.getName()
				+ ChatColor.WHITE + "] ";
		// sender.getPlayer().sendMessage(out);
		return out;
	}

	public String getOutputNameF(SblockUser sender, boolean isThirdPerson, Channel c) {
		// colors for <$name> applied here
		// SburbChat code. Handle with care
		String out = "";

		String outputName = sender.playerName;
		if (!(sender.globalNick.equals(sender.playerName))) {
			outputName = sender.globalNick;
		}
		else if(c.getType().equals(ChannelType.NICK))	{
			//nick not required in NickChannel
		}
		else if(c.getType().equals(ChannelType.RP))	{
			//CanonNick required in RPChannel
			//if getNickFromUser.equals something in CanonNicks
			//use that name
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

		out = (isThirdPerson ? "> " : colorW + "<") + colorP + outputName
				+ ChatColor.WHITE
				+ (isThirdPerson ? ": " : colorW + "> " + ChatColor.WHITE);
		// sender.getPlayer().sendMessage(out);
		return out;
	}

	public void sendMessage(String string) {
		this.getPlayer().sendMessage(string);
	}

	public String toString() { // For /whois usage mainly
		//TODO Someone tell Dub to get off his lazy ass
		String s = "";
		return s;
	}

	public static SblockUser getUser(String userName) {
		return UserManager.getUserManager().getUser(userName);
	}
}
