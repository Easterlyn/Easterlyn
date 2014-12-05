package co.sblock.users;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.ColorDef;
import co.sblock.chat.channel.Channel;

/**
 * Storage and access of all data saved for a User.
 * 
 * @author Jikoo
 */
public class OfflineUser {

	public static final DecimalFormat DECIMAL_FORMATTER = new DecimalFormat("00");

	/* Player's UUID */
	private final UUID uuid;
	private final String userIP;
	private String displayName;
	private Region currentRegion;
	private String currentLocation; //TODO

	/* Classpect */
	private UserClass userClass;
	private UserAspect userAspect;
	private Region medium, dream;

	/* Various data Sblock tracks for progression purposes */
	private ProgressionState progression;
	private UUID server, client;
	private final Set<Integer> programs;
	boolean isServer;

	boolean allowFlight;

	/* Chat data*/
	protected String currentChannel;
	private final Set<String> listening;
	private AtomicBoolean globalMute, suppress;

	/* Locations to teleport Players to when conditions are met */
	private Location previousLocation;

	protected OfflineUser(UUID userID, String displayName, Region currentRegion, UserClass userClass,
			UserAspect aspect, Region mplanet, Region dplanet, ProgressionState progstate,
			boolean allowFlight, String IP, Location previousLocation,
			String currentChannel, Set<Integer> programs, Set<String> listening,
			AtomicBoolean globalMute, AtomicBoolean supress, UUID server, UUID client) {
		this.uuid = userID;
		this.displayName = displayName;
		this.userClass = userClass;
		this.userAspect = aspect;
		this.medium = mplanet;
		this.dream = dplanet;
		this.progression = progstate;
		this.isServer = false;
		this.allowFlight = allowFlight;
		this.previousLocation = previousLocation;
		if (previousLocation == null) {
			try {
				this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
			} catch (NullPointerException e) {
				
			}
		}
		this.currentChannel = currentChannel;
		this.programs = programs;
		this.listening = listening;
		this.globalMute = globalMute;
		this.suppress = supress;
		this.userIP = IP;
	}

	private OfflineUser(UUID uuid, String ip) {
		this.uuid = uuid;
		this.userIP = ip;
		this.userClass = UserClass.HEIR;
		this.userAspect = UserAspect.BREATH;
		this.medium = Region.LOWAS;
		this.dream = Region.PROSPIT;
		this.progression = ProgressionState.NONE;
		this.isServer = false;
		this.allowFlight = false;
		World earth = Bukkit.getWorld("Earth");
		if (earth != null) {
			this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
		}
		this.currentChannel = "#";
		this.programs = new HashSet<>();
		this.listening = new HashSet<>();
		this.listening.add("#");
		this.globalMute = new AtomicBoolean(false);
		this.suppress = new AtomicBoolean(false);
	}

	/**
	 * Gets the UUID of the User.
	 * 
	 * @return the UUID
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * Gets the Player represented by this User.
	 * 
	 * @return the Player
	 */
	public Player getPlayer() {
		// TODO offline load
		return null;
	}

	/**
	 * Gets the OfflinePlayer.
	 * 
	 * @return the OfflinePlayer
	 */
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(uuid);
	}

	/**
	 * Gets the name of the Player.
	 * 
	 * @return a String containing the Player's name
	 */
	public String getPlayerName() {
		return this.getOfflinePlayer().getName();
	}

	/**
	 * Gets the display name of the Player.
	 * 
	 * @return a String of the Player's nickname
	 */
	public String getDisplayName() {
		return this.displayName == null ? this.getPlayerName() : this.displayName;
	}

	/**
	 * Gets the User's chosen UserClass.
	 * 
	 * @return the UserClass, default Heir
	 */
	public UserClass getUserClass() {
		return this.userClass;
	}

	/**
	 * Sets the User's UserClass.
	 * 
	 * @param userClassName the new UserClass
	 */
	public void setPlayerClass(String userClassName) {
		this.userClass = UserClass.getClass(userClassName);
	}

	/**
	 * Gets the User's chosen UserAspect.
	 * 
	 * @return the UserAspect
	 */
	public UserAspect getAspect() {
		return this.userAspect;
	}

	/**
	 * Sets the User's UserAspect.
	 * 
	 * @param userAspectName the new UserAspect
	 */
	public void setAspect(String userAspectName) {
		this.userAspect = UserAspect.getAspect(userAspectName);
	}

	/**
	 * Gets the User's chosen medium land.
	 * 
	 * @return the medium Region
	 */
	public Region getMediumPlanet() {
		return this.medium;
	}

	/**
	 * Sets the User's medium land.
	 * 
	 * @param mediumRegionName the new medium Region
	 */
	public void setMediumPlanet(String mediumRegionName) {
		Region planet = Region.getRegion(mediumRegionName);
		if (!planet.isMedium()) {
			throw new RuntimeException("Invalid medium planet: " + planet.name());
		}
		this.medium = planet;
	}

	/**
	 * Gets the User's chosen dream planet Region.
	 * 
	 * @return the dream Region
	 */
	public Region getDreamPlanet() {
		return this.dream;
	}

	/**
	 * Sets the User's dream planet Region.
	 * 
	 * @param dreamRegionName the new dream Region.
	 */
	public void setDreamPlanet(String dreamRegionName) {
		Region planet = Region.getRegion(dreamRegionName);
		if (!planet.isDream()) {
			throw new RuntimeException("Invalid dream planet: " + planet.name());
		}
		this.dream = planet;
	}

	/**
	 * Gets the User's game progression.
	 * 
	 * @return the ProgressionState
	 */
	public ProgressionState getProgression() {
		return this.progression;
	}

	/**
	 * Sets the User's game progression.
	 * 
	 * @param progression the new ProgressionState
	 */
	public void setProgression(ProgressionState progression) {
		this.progression = progression;
	}

	/**
	 * Check if the User can fly.
	 * 
	 * @return true if flight should be enabled
	 */
	public boolean canFly() {
		return this.allowFlight;
	}

	/**
	 * Updates the User's ability to fly.
	 */
	public void updateFlight() {}

	/**
	 * Sets the Player's previous location. Used for returning to and from dream planets.
	 * 
	 * @param l The Player's previous Location
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
	 * Gets Player's previous Location.
	 * 
	 * @return the previous Location
	 */
	public Location getPreviousLocation() {
		return this.previousLocation;
	}

	/**
	 * The String representation of the Player's total time ingame.
	 * 
	 * @return the Player's time ingame
	 */
	public String getTimePlayed() {
		// TODO
		return "null";
	}

	/**
	 * Gets a Set of all Computer programs accessible by the User.
	 * 
	 * @return the programs installed
	 */
	public Set<Integer> getPrograms() {
		return this.programs;
	}

	/**
	 * Add an Entry to the Set of programs accessible by the User at their Computer.
	 * 
	 * @param i the number of the program to add
	 */
	public void addProgram(int i) {
		this.programs.add(i);
	}

	/**
	 * Gets the User's IP.
	 * 
	 * @return the Player's IP
	 */
	public String getUserIP() {
		return this.userIP;
	}

	/**
	 * Gets the User's current Region.
	 * 
	 * @return the Region the Player is in.
	 */
	public Region getCurrentRegion() {
		return currentRegion != null ? currentRegion : Region.UNKNOWN;
	}

	/**
	 * Sets the User's current region. Does not update chat channels.
	 * 
	 * @param region
	 */
	protected void setCurrentRegion(Region region) {
		currentRegion = region;
	}

	/**
	 * Update current Region and change RegionChannel.
	 * 
	 * @param newR the Region being transitioned into
	 */
	public void updateCurrentRegion(Region newR) {}

	/**
	 * Check if the User is in server mode.
	 * 
	 * @return true if the User is in server mode
	 */
	public boolean isServer() {
		return this.isServer;
	}

	/**
	 * Initiate server mode.
	 */
	public void startServerMode() {}

	/**
	 * Disable server mode.
	 */
	public void stopServerMode() {}

	/**
	 * Set the User's server player.
	 * 
	 * @param userID the UUID of the server
	 */
	public void setServer(UUID userID) {
		this.server = userID;
	}

	/**
	 * Get the User's server's UUID.
	 * 
	 * @return the saved server UUID
	 */
	public UUID getServer() {
		return this.server;
	}

	/**
	 * Set the User's client player.
	 * 
	 * @param s the name of the Player to set as the client player.
	 */
	public boolean setClient(UUID userID) {
		if (this.client == null) {
			this.client = userID;
			return true;
		}
		return false;
	}

	/**
	 * Get the User's client player.
	 * 
	 * @return the saved client player
	 */
	public UUID getClient() {
		return this.client;
	}

	/**
	 * Sends the Player a message.
	 * 
	 * @param message the message to send
	 */
	public void sendMessage(String message) {}

	/**
	 * Sends a raw message that will attempt to highlight the user.
	 * 
	 * @param message
	 * @param additionalMatches
	 */
	public void rawHighlight(String message, String... additionalMatches) {}

	/**
	 * Sets the User's chat mute status.
	 * 
	 * @param b true if the User is muted
	 */
	public void setMute(boolean b) {
		this.globalMute.set(b);
	}

	/**
	 * Gets the User's mute status.
	 * 
	 * @return true if the User is muted
	 */
	public boolean isMute() {
		return this.globalMute.get();
	}

	/**
	 * Sets the User's current chat suppression status.
	 * 
	 * @param b true if the User is to suppress global channels
	 */
	public void setSuppressing(boolean b) {
		this.suppress.lazySet(b);
	}

	/**
	 * Gets the User's suppression status.
	 * 
	 * @return true if the Player is suppressing global channels.
	 */
	public boolean isSuppressing() {
		return suppress.get();
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param c the Channel to set as current
	 */
	public void setCurrentChannel(Channel c) {
		if (!c.isApproved(this) || c.isBanned(this)) {
			return;
		}
		this.currentChannel = c.getName();
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param c the Channel to set as current
	 */
	public void setCurrentChannel(String s) {
		Channel c = ChannelManager.getChannelManager().getChannel(s);
		this.setCurrentChannel(c);
	}

	/**
	 * Gets the Channel the Player is currently sending messages to.
	 * 
	 * @return Channel
	 */
	public Channel getCurrentChannel() {
		return ChannelManager.getChannelManager().getChannel(this.currentChannel);
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param channel the Channel to add
	 * 
	 * @return true if the Channel was added
	 */
	public boolean addListening(Channel channel) {
		if (!channel.isApproved(this) || channel.isBanned(this)) {
			return false;
		}
		this.listening.add(channel.getName());
		return true;
	}

	/**
	 * Begin listening to a Set of channels. Used on login.
	 * 
	 * @param channels
	 */
	public void loginAddListening(Set<String> channels) {
		this.listening.addAll(channels);
	}

	/**
	 * Remove a Channel from the Player's listening List.
	 * 
	 * @param channelName the name of the Channel to remove
	 */
	public void removeListening(String channelName) {
		if (this.currentChannel.equals(channelName)) {
			this.currentChannel = null;
		}
		this.listening.remove(channelName);
	}

	/**
	 * Silently removes a Channel from the Player's listening list.
	 * 
	 * @param channel the Channel to remove
	 */
	public void removeListeningSilent(Channel channel) {
		channel.removeNick(this, false);
		this.listening.remove(channel.getName());
		if (this.currentChannel != null && this.currentChannel.equals(channel.getName())) {
			this.currentChannel = null;
		}
		channel.removeListening(this.getUUID());
	}

	public boolean removeListeningQuit(String next) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Gets the Set of names of Channels that the Player is listening to.
	 * 
	 * @return a Set<String> of Channel names.
	 */
	public Set<String> getListening() {
		return this.listening;
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param c the Channel to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public boolean isListening(Channel c) {
		return this.listening.contains(c.getName());
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param s the Channel name to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public boolean isListening(String s) {
		return this.listening.contains(s);
	}

	/**
	 * Checks if the Player is close enough to a Computer to chat in #.
	 * 
	 * @return true if the Player can chat in #
	 */
	public boolean getComputerAccess() {
		return false;
	}

	/**
	 * Gets a user-friendly representation of this User's profile.
	 * 
	 * @return the profile information
	 */
	public String getProfile() {
		return new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("+---")
				.append(ChatColor.DARK_AQUA).append(' ').append(getPlayerName()).append(' ')
				.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("---+\n")
				.append(ChatColor.YELLOW).append(getUserClass().getDisplayName())
				.append(ChatColor.DARK_AQUA).append(" of ").append(getAspect().getColor())
				.append(getAspect().getDisplayName()).append('\n').append(ChatColor.DARK_AQUA)
				.append("Medium: ").append(getMediumPlanet().getColor()).append('\n')
				.append(ChatColor.DARK_AQUA).append("Dream: ").append(getDreamPlanet().getColor())
				.append(getDreamPlanet().getDisplayName()).toString();
	}

	/**
	 * Gets a user-friendly representation of all of this User's information.
	 * 
	 * @return all stored data for this User
	 */
	public String getWhois() {
		StringBuilder sb = new StringBuilder();
		//+-- Name aka Nickname from IP --+
		sb.append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH).append("+--")
				.append(ChatColor.DARK_AQUA).append(' ').append(getPlayerName())
				.append(ChatColor.YELLOW).append(" aka ").append(ChatColor.DARK_AQUA)
				.append(getDisplayName()).append(ChatColor.YELLOW)
				.append(" from ").append(ChatColor.DARK_AQUA).append(getUserIP())
				.append(ChatColor.YELLOW).append(' ').append(ChatColor.STRIKETHROUGH)
				.append("--+\n");

		// Class of Aspect, dream, planet
		sb.append(ChatColor.DARK_AQUA).append(getUserClass().getDisplayName())
				.append(ChatColor.YELLOW).append(" of ").append(ChatColor.DARK_AQUA)
				.append(getAspect().getDisplayName()).append(ChatColor.YELLOW).append(", ")
				.append(ChatColor.DARK_AQUA).append(getDreamPlanet().getDisplayName())
				.append(ChatColor.YELLOW).append(", ").append(ChatColor.DARK_AQUA)
				.append(getMediumPlanet().getDisplayName()).append('\n');

		// Loc: current location TODO, Region: region
		sb.append(ChatColor.YELLOW).append("Loc: ").append(ChatColor.DARK_AQUA)
//				.append(yaml.getString("location", "unknown")).append(ChatColor.YELLOW) // TODO
				.append('\n');

		// Prev loc: loc prior to change to/from dreamplanet, Prev region: region of said location
		sb.append("Prev loc: ").append(ChatColor.DARK_AQUA)
				.append(BukkitSerializer.locationToBlockCenterString(previousLocation))
				.append(ChatColor.YELLOW).append(", Prev region: ").append(ChatColor.DARK_AQUA)
				.append(Region.valueOf(previousLocation != null ? previousLocation.getWorld().getName()
						: "UNKNOWN")).append('\n');

		// Progression: PROGRESSION, Programs: [list]
		sb.append(ChatColor.YELLOW).append("Progression: ").append(ChatColor.DARK_AQUA)
				.append(getProgression().name()).append(ChatColor.YELLOW)
				.append(", Programs: ").append(ChatColor.DARK_AQUA)
				.append(getPrograms()).append('\n');

		// Server: UUID, Client: UUID
		sb.append(ChatColor.YELLOW).append("Server: ").append(ChatColor.DARK_AQUA)
				.append(getServer() != null ? getServer() : "null").append(ChatColor.YELLOW)
				.append(", Client: ").append(ChatColor.DARK_AQUA)
				.append(getClient() != null ? getClient() : "null").append('\n');

		// Pestering: current, Listening: [list]
		sb.append(ChatColor.YELLOW).append("Pestering: ").append(ChatColor.DARK_AQUA)
				.append(getCurrentChannel() != null ? getCurrentChannel().getName() : "null")
				.append(ChatColor.YELLOW).append(", Listening: ").append(ChatColor.DARK_AQUA)
				.append(getListening()).append('\n');

		// Muted: boolean, Suppressing: boolean
		sb.append(ChatColor.YELLOW).append("Muted: ").append(ChatColor.DARK_AQUA).append(isMute())
				.append(ChatColor.YELLOW).append(", Suppressing: ").append(ChatColor.DARK_AQUA)
				.append(isSuppressing()).append('\n');

		// Last seen: date, Playtime: X days, XX:XX
		sb.append(ChatColor.YELLOW).append("Last login: ").append(ChatColor.DARK_AQUA)
				.append(new SimpleDateFormat("HH:mm 'on' dd/MM/YY").format(new Date(
						getOfflinePlayer().getLastPlayed()))).append(ChatColor.YELLOW)
				.append(", Time ingame: ").append(ChatColor.DARK_AQUA).append(getTimePlayed());

		return sb.toString();
	}

	public boolean equals(Object object) {
		if (object == null) {
			return false;
		}
		if (object == this) {
			return true;
		}
		if (object instanceof OfflineUser) {
			OfflineUser u = (OfflineUser) object;
			return u.getUUID().equals(getUUID());
		}
		return false;
	}

	public boolean isOnline() {
		return Bukkit.getOfflinePlayer(getUUID()).isOnline();
	}

	public OnlineUser getOnlineUser() {
		OnlineUser user = UserManager.getOnlineUser(getUUID());
		if (user == null) {
			return new OnlineUser(uuid, displayName, currentRegion, userClass, userAspect, medium, dream,
					progression, allowFlight, Bukkit.getPlayer(uuid).getAddress().getHostString(),
					previousLocation, currentChannel, programs, listening, globalMute, suppress,
					server, client);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected static OfflineUser load(final UUID uuid) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					UserManager.getUserManager().getLogger().warning("File " + uuid.toString() + ".yml does not exist!");
					return new OfflineUser(uuid, "null");
				}
				player.teleport(UserManager.getSpawnLocation());

				OnlineUser user = new OfflineUser(uuid, player.getAddress().getHostString()).getOnlineUser();
				user.getListening().add(Region.EARTH.getChannelName());
				user.loginAddListening(user.getListening());
				user.updateCurrentRegion(Region.EARTH);
				UserManager.addUser(user);

				Bukkit.broadcastMessage(ColorDef.HAL + "It would seem that " + player.getName()
						+ " is joining us for the first time! Please welcome them.");
				return user;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data for " + uuid, e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		OfflineUser user = new OfflineUser(uuid, yaml.getString("ip", "null"));
		user.setPreviousLocation(BukkitSerializer.locationFromString(yaml.getString("previousLocation")));
		//yaml.getString("previousRegion");
		user.setPlayerClass(yaml.getString("classpect.class", "HEIR"));
		user.setAspect(yaml.getString("classpect.aspect", "BREATH"));
		user.setMediumPlanet(yaml.getString("classpect.medium", "LOWAS"));
		Region dream = Region.getRegion(yaml.getString("classpect.dream", "PROSPIT"));
		user.setDreamPlanet(dream.name());
		Location currentLoc = BukkitSerializer.locationFromString(yaml.getString("previousLocation"));
		if (currentLoc == null) {
			currentLoc = UserManager.getSpawnLocation();
		}
		Region current = Region.getRegion(currentLoc.getWorld().getName());
		if (current.isDream()) {
			current = dream;
		}
		user.setCurrentRegion(current);
		user.setProgression(ProgressionState.valueOf(yaml.getString("progression.progression", "NONE")));
		user.getPrograms().addAll((HashSet<Integer>) yaml.get("progression.programs"));
		if (yaml.getString("progression.server") != null) {
			user.setServer(UUID.fromString(yaml.getString("progression.server")));
		}
		if (yaml.getString("progression.client") != null) {
			user.setClient(UUID.fromString(yaml.getString("progression.client")));
		}
		user.setCurrentChannel(yaml.getString("chat.current", "#"));
		user.getListening().addAll((HashSet<String>) yaml.get("chat.listening"));
		user.setMute(yaml.getBoolean("chat.muted"));
		user.setSuppressing(yaml.getBoolean("chat.suppressing"));
		//(Set<String>) yaml.get("chat.ignoring");
		return user;
	}
}
