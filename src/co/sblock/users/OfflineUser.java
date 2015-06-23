package co.sblock.users;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.utilities.player.PlayerLoader;

import net.md_5.bungee.api.ChatColor;

/**
 * Storage and access of all data saved for a User.
 * 
 * @author Jikoo
 */
public class OfflineUser {

	private final YamlConfiguration yaml;

	/* General player data */
	private final UUID uuid;
	private final String userIP;
	private Location previousLocation;

	/* Various data Sblock tracks for progression purposes */
	private Set<Integer> programs;

	/* Chat data*/
	protected String currentChannel;
	private Set<String> listening;

	protected OfflineUser(UUID userID, String ip, YamlConfiguration yaml,
			Location previousLocation, Set<Integer> programs, String currentChannel,
			Set<String> listening) {
		this.uuid = userID;
		this.userIP = ip;
		this.yaml = yaml;
		this.previousLocation = previousLocation;
		if (previousLocation == null) {
			World earth = Bukkit.getWorld("Earth");
			if (earth != null) {
				this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
			}
		}
		this.programs = programs;
		this.currentChannel = currentChannel;
		this.listening = listening;
	}

	private OfflineUser(UUID uuid, String ip, YamlConfiguration yaml) {
		this.uuid = uuid;
		this.userIP = ip;
		this.yaml = yaml;
		World earth = Bukkit.getWorld("Earth");
		if (earth != null) {
			this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
		}
		this.currentChannel = "#";
		this.programs = new HashSet<>();
		this.listening = new HashSet<>();
		this.listening.add("#");
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
		return PlayerLoader.getPlayer(getUUID());
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
	 * Gets the User's IP.
	 * 
	 * @return the Player's IP
	 */
	public String getUserIP() {
		return this.userIP;
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
		return yaml.getString("nickname", getPlayerName());
	}

	/**
	 * Gets the User's chosen UserClass.
	 * 
	 * @return the UserClass, default Heir
	 */
	public UserClass getUserClass() {
		return UserClass.getClass(yaml.getString("classpect.class", "HEIR"));
	}

	/**
	 * Sets the User's UserClass.
	 * 
	 * @param userClassName the new UserClass
	 */
	public void setUserClass(String userClassName) {
		yaml.set("classpect.class", UserClass.getClass(userClassName).getDisplayName());
	}

	/**
	 * Gets the User's chosen UserAspect.
	 * 
	 * @return the UserAspect
	 */
	public UserAspect getUserAspect() {
		return UserAspect.getAspect(yaml.getString("classpect.aspect", "BREATH"));
	}

	/**
	 * Sets the User's UserAspect.
	 * 
	 * @param userAspectName the new UserAspect
	 */
	public void setUserAspect(String userAspectName) {
		yaml.set("classpect.aspect", UserAspect.getAspect(userAspectName).getDisplayName());
	}

	/**
	 * Gets the User's chosen medium land.
	 * 
	 * @return the medium Region
	 */
	public Region getMediumPlanet() {
		return Region.getRegion(yaml.getString("classpect.medium", "LOWAS"));
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
		yaml.set("classpect.medium", planet.getDisplayName());
	}

	/**
	 * Gets the User's chosen dream planet Region.
	 * 
	 * @return the dream Region
	 */
	public Region getDreamPlanet() {
		return Region.getRegion(yaml.getString("classpect.dream", "PROSPIT"));
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
		yaml.set("classpect.dream", planet.getDisplayName());
	}

	/**
	 * Gets the User's game progression.
	 * 
	 * @return the ProgressionState
	 */
	public ProgressionState getProgression() {
		return ProgressionState.valueOf(yaml.getString("progression.progression", "NONE"));
	}

	/**
	 * Sets the User's game progression.
	 * 
	 * @param progression the new ProgressionState
	 */
	public void setProgression(ProgressionState progression) {
		yaml.set("progression.progression", progression.name());
	}

	/**
	 * Check if the User can fly.
	 * 
	 * @return true if flight should be enabled
	 */
	public boolean getFlight() {
		return yaml.getBoolean("flying", false);
	}

	/**
	 * Updates the User's ability to fly.
	 */
	public void updateFlight() {}

	/**
	 * Set whether or not a user can be spectated to.
	 * 
	 * @param spectatable true if the user can be spectated
	 */
	public void setSpectatable(boolean spectatable) {
		yaml.set("spectatable", spectatable);
	}

	/**
	 * Get whether or not a user can be spectated to.
	 * 
	 * @return true if the user can be spectated to
	 */
	public boolean getSpectatable() {
		return yaml.getBoolean("spectatable", true);
	}

	public Location getCurrentLocation() {
		String location = yaml.getString("location");
		if (location == null) {
			return Users.getSpawnLocation();
		}
		return BukkitSerializer.locationFromString(location);
	}

	/**
	 * Gets the User's current Region.
	 * 
	 * @return the Region the Player is in.
	 */
	public Region getCurrentRegion() {
		return Region.getRegion(yaml.getString("region", "UNKNOWN"));
	}

	/**
	 * Sets the User's current region. Does not update chat channels.
	 * 
	 * @param region
	 */
	protected void setCurrentRegion(Region region) {
		yaml.set("region", region.getDisplayName());
	}

	/**
	 * Update current Region and change RegionChannel.
	 * 
	 * @param newR the Region being transitioned into
	 */
	public void updateCurrentRegion(Region newR) {} // CHAT: allow and do channel management

	/**
	 * Sets the Player's previous location. Used for returning to and from dream planets.
	 * 
	 * @param l The Player's previous Location
	 */
	public void setPreviousLocation(Location l) {
		l = l.clone();
		l.setX(l.getBlockX() + .5);
		l.setY(l.getBlockY());
		l.setZ(l.getBlockZ() + .5);
		this.previousLocation = l;
	}

	/**
	 * Gets Player's previous Location.
	 * 
	 * @return the previous Location
	 */
	public Location getPreviousLocation() {
		return previousLocation != null ? previousLocation : Users.getSpawnLocation();
	}

	/**
	 * The String representation of the Player's total time ingame.
	 * 
	 * @return the Player's time ingame
	 */
	public String getTimePlayed() {
		return yaml.getString("playtime", "Unknown");
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
	 * Set the User's server player.
	 * 
	 * @param userID the UUID of the server
	 */
	public void setServer(UUID userID) {
		yaml.set("progression.server", userID != null ? userID.toString() : null);
	}

	/**
	 * Get the User's server's UUID.
	 * 
	 * @return the saved server UUID
	 */
	public UUID getServer() {
		if (yaml.getString("progression.server") != null) {
			return UUID.fromString(yaml.getString("progression.server"));
		}
		return null;
	}

	/**
	 * Set the User's client player.
	 * 
	 * @param s the name of the Player to set as the client player.
	 */
	public void setClient(UUID userID) {
		yaml.set("progression.client", userID != null ? userID.toString() : null);
	}

	/**
	 * Get the User's client player.
	 * 
	 * @return the saved client player
	 */
	public UUID getClient() {
		if (yaml.getString("progression.client") != null) {
			return UUID.fromString(yaml.getString("progression.client"));
		}
		return null;
	}

	/**
	 * Sends the Player a message.
	 * 
	 * @param message the message to send
	 */
	public void sendMessage(String message) {}

	/**
	 * Sets the User's current chat suppression status.
	 * 
	 * @param suppress true if the User is to suppress global channels
	 */
	public synchronized void setSuppression(boolean suppress) {
		yaml.set("chat.suppressing", suppress);
	}

	/**
	 * Gets the User's suppression status.
	 * 
	 * @return true if the Player is suppressing global channels.
	 */
	public synchronized boolean getSuppression() {
		return yaml.getBoolean("chat.suppressing");
	}

	/**
	 * Sets the User's current chat highlight status.
	 * 
	 * @param highlight true if the User is to be highlighted
	 */
	public synchronized void setHighlight(boolean highlight) {
		yaml.set("chat.highlight", highlight);
	}

	/**
	 * Gets the User's highlight status.
	 * 
	 * @return true if the Player is to be highlighted.
	 */
	public synchronized boolean getHighlight() {
		return yaml.getBoolean("chat.highlight", true);
	}

	public synchronized Collection<String> getHighlights(Channel channel) {
		HashSet<String> highlights = new HashSet<>();
		if (this.getHighlight()) {
			if (this.isOnline() && channel instanceof NickChannel) {
				highlights.add(((NickChannel) channel).getNick(this));
			}
			highlights.add(getPlayerName());
			highlights.add(getDisplayName());
		}
		return highlights;
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param channel the Channel to set as current
	 */
	public synchronized void setCurrentChannel(Channel channel) {
		if (channel == null) {
			this.currentChannel = null;
			return;
		}
		if (!channel.isApproved(this) || channel.isBanned(this)) {
			return;
		}
		this.currentChannel = channel.getName();
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param channelName the name of the Channel to set as current
	 */
	public synchronized void setCurrentChannel(String channelName) {
		Channel channel = ChannelManager.getChannelManager().getChannel(channelName);
		this.setCurrentChannel(channel);
	}

	/**
	 * Gets the Channel the Player is currently sending messages to.
	 * 
	 * @return Channel
	 */
	public synchronized Channel getCurrentChannel() {
		return ChannelManager.getChannelManager().getChannel(this.currentChannel);
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param channel the Channel to add
	 * 
	 * @return true if the Channel was added
	 */
	public synchronized boolean addListening(Channel channel) {
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
	public void handleLoginChannelJoins() {}

	/**
	 * Remove a Channel from the Player's listening List.
	 * 
	 * @param channelName the name of the Channel to remove
	 */
	public synchronized void removeListening(String channelName) {
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
	public synchronized void removeListeningSilent(Channel channel) {
		if (channel instanceof NickChannel) {
			((NickChannel) channel).removeNick(this, false);
		}
		this.listening.remove(channel.getName());
		if (this.currentChannel != null && this.currentChannel.equals(channel.getName())) {
			this.currentChannel = null;
		}
		channel.getListening().remove(this.getUUID());
	}

	/**
	 * Removes the User from the specified Channel's list of online Users without an announcement.
	 * Does not modify the User's listening list.
	 * 
	 * @param channelName the name of the Channel
	 * 
	 * @return true if the Channel is valid and had the User registered
	 */
	public boolean removeListeningQuit(String channelName) {
		Channel channel = ChannelManager.getChannelManager().getChannel(channelName);
		if (channel == null || !channel.getListening().contains(this.getUUID())) {
			return false;
		}
		channel.getListening().remove(this.getUUID());
		return true;
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
	public synchronized boolean isListening(Channel c) {
		return this.listening.contains(c.getName());
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param s the Channel name to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public synchronized boolean isListening(String s) {
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
	 * Gets a List of commands to be sent on login.
	 * 
	 * @return
	 */
	public List<String> getLoginCommands() {
		return yaml.getStringList("misc.logincommands");
	}

	/**
	 * Sets a List of commands to be sent on login.
	 * 
	 * @param commands
	 */
	public void setLoginCommands(List<String> commands) {
		yaml.set("misc.logincommands", commands);
	}

	/**
	 * Gets a user-friendly representation of this User's profile.
	 * 
	 * @return the profile information
	 */
	public String getProfile() {
		return new StringBuilder().append(Color.GOOD).append(ChatColor.STRIKETHROUGH)
				.append("+---").append(Color.GOOD_EMPHASIS).append(' ').append(getPlayerName())
				.append(' ').append(Color.GOOD).append(ChatColor.STRIKETHROUGH)
				.append("---+\n").append(Color.GOOD).append(getUserClass().getDisplayName())
				.append(Color.GOOD_EMPHASIS).append(" of ").append(getUserAspect().getColor())
				.append(getUserAspect().getDisplayName()).append('\n').append(Color.GOOD_EMPHASIS)
				.append("Medium: ").append(getMediumPlanet().getColor())
				.append(getMediumPlanet().getDisplayName()).append('\n')
				.append(Color.GOOD_EMPHASIS).append("Dream: ").append(getDreamPlanet().getColor())
				.append(getDreamPlanet().getDisplayName()).toString();
	}

	/**
	 * Gets a user-friendly representation of all of this User's information.
	 * 
	 * @return all stored data for this User
	 */
	public String getWhois() {
		StringBuilder sb = new StringBuilder();
		//+-- Name from IP --+
		sb.append(Color.GOOD).append(ChatColor.STRIKETHROUGH).append("+--")
				.append(Color.GOOD_EMPHASIS).append(' ').append(getPlayerName())
				.append(Color.GOOD).append(" from ").append(Color.GOOD_EMPHASIS)
				.append(getUserIP()).append(Color.GOOD).append(' ')
				.append(ChatColor.STRIKETHROUGH).append("--+\n");

		// UUID: uuid
		sb.append(Color.GOOD).append("UUID: ").append(getUUID()).append('\n');

		// If stored, Previously known as: Name
		if (yaml.getString("previousname") != null) {
			sb.append(Color.GOOD).append("Previously known as: ").append(Color.GOOD_EMPHASIS)
					.append(yaml.getString("previousname")).append('\n');
		}

		// Class of Aspect, dream, planet
		sb.append(Color.GOOD_EMPHASIS).append(getUserClass().getDisplayName())
				.append(Color.GOOD).append(" of ").append(Color.GOOD_EMPHASIS)
				.append(getUserAspect().getDisplayName()).append(Color.GOOD).append(", ")
				.append(Color.GOOD_EMPHASIS).append(getDreamPlanet().getDisplayName())
				.append(Color.GOOD).append(", ").append(Color.GOOD_EMPHASIS)
				.append(getMediumPlanet().getDisplayName()).append('\n');

		// Loc: current location, Region: region
		sb.append(Color.GOOD).append("Loc: ").append(Color.GOOD_EMPHASIS)
				.append(BukkitSerializer.locationToBlockCenterString(getCurrentLocation()))
				.append(Color.GOOD).append(", Region: ").append(Color.GOOD_EMPHASIS)
				.append(getCurrentRegion().getDisplayName()).append('\n');

		// Prev loc: loc prior to change to/from dreamplanet, Prev region: region of said location
		sb.append(Color.GOOD).append("Prev loc: ").append(Color.GOOD_EMPHASIS)
				.append(BukkitSerializer.locationToBlockCenterString(previousLocation))
				.append(Color.GOOD).append(", Prev region: ").append(Color.GOOD_EMPHASIS)
				.append(Region.getRegion(getPreviousLocation().getWorld().getName())).append('\n');

		// Progression: PROGRESSION, Programs: [list]
		sb.append(Color.GOOD).append("Progression: ").append(Color.GOOD_EMPHASIS)
				.append(getProgression().name()).append(Color.GOOD)
				.append(", Programs: ").append(Color.GOOD_EMPHASIS)
				.append(getPrograms()).append('\n');

		// Server: UUID, Client: UUID
		sb.append(Color.GOOD).append("Server: ").append(Color.GOOD_EMPHASIS)
				.append(getServer() != null ? getServer() : "null").append(Color.GOOD)
				.append(", Client: ").append(Color.GOOD_EMPHASIS)
				.append(getClient() != null ? getClient() : "null").append('\n');

		// Pestering: current, Listening: [list]
		sb.append(Color.GOOD).append("Pestering: ").append(Color.GOOD_EMPHASIS)
				.append(getCurrentChannel() != null ? getCurrentChannel().getName() : "null")
				.append(Color.GOOD).append(", Listening: ").append(Color.GOOD_EMPHASIS)
				.append(getListening()).append('\n');

		// Muted: boolean, Suppressing: boolean
		sb.append(Color.GOOD).append("Suppressing: ").append(Color.GOOD_EMPHASIS)
				.append(getSuppression()).append('\n');

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'on' dd/MM/YY");
		// Seen: date, Playtime: X days, XX:XX since XX:XX on XX/XX/XX
		sb.append(Color.GOOD).append("Seen: ").append(Color.GOOD_EMPHASIS)
				.append(dateFormat.format(new Date(getOfflinePlayer().getLastPlayed())))
				.append(Color.GOOD).append(", Ingame: ").append(Color.GOOD_EMPHASIS)
				.append(getTimePlayed()).append(" since ")
				.append(dateFormat.format(getOfflinePlayer().getFirstPlayed()));

		return sb.toString();
	}

	@Override
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
		OnlineUser user = Users.getOnlineUser(getUUID());
		if (user == null) {
			return new OnlineUser(getUUID(), Bukkit.getPlayer(uuid).getAddress().getHostString(),
					yaml, getPreviousLocation(), getPrograms(), currentChannel, getListening());
		}
		return null;
	}

	protected YamlConfiguration getYamlConfiguration() {
		return yaml;
	}

	public void save() {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), getUUID().toString() + ".yml");
			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + getUUID().toString(), e);
		}
		Player player = this instanceof OnlineUser ? this.getPlayer() : Bukkit.getPlayer(getUUID());
		yaml.set("name", player != null ? getPlayerName() : Bukkit.getOfflinePlayer(getUUID()).getName());
		yaml.set("ip", getUserIP());
		if (player != null) {
			yaml.set("playtime", getTimePlayed());
		}
		yaml.set("previousLocation", BukkitSerializer.locationToBlockCenterString(getPreviousLocation()));
		yaml.set("previousRegion", null);
		yaml.set("progression.programs", getPrograms());
		yaml.set("chat.current", getCurrentChannel() != null ? getCurrentChannel().getName() : "#");
		yaml.set("chat.listening", getListening());
		yaml.set("chat.ignoring", null);
		try {
			yaml.save(file);
		} catch (IOException e) {
			throw new RuntimeException("Unable to save data for " + getPlayerName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	protected static OfflineUser load(final UUID uuid) {
		File file;
		try {
			file = new File(Sblock.getInstance().getUserDataFolder(), uuid.toString() + ".yml");
			if (!file.exists()) {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					Users.getInstance().getLogger().warning("File " + uuid.toString() + ".yml does not exist!");
					return new OfflineUser(uuid, "null", new YamlConfiguration());
				}
				player.teleport(Users.getSpawnLocation());

				OfflineUser offline = new OfflineUser(uuid, player.getAddress().getHostString(), new YamlConfiguration());
				offline.setCurrentChannel("#"); // Reverse come Entry
				offline.getListening().add(Region.EARTH.getChannelName());
				OnlineUser user = offline.getOnlineUser();
				user.updateCurrentRegion(Region.EARTH);
				player.setResourcePack(Region.EARTH.getResourcePackURL());
				Users.addUser(user);

				if (!player.hasPlayedBefore()) {
					// Our data file may have just been deleted - reset planned for Entry, etc.
					Bukkit.broadcastMessage(Color.HAL + "It would seem that " + player.getName()
							+ " is joining us for the first time! Please welcome them.");
				} else {
					player.sendMessage(Color.HAL + "We've reset classpect since you last played. Please re-select now!");
				}
				return user;
			}
		} catch (IOException e) {
			throw new RuntimeException("Unable to load data for " + uuid, e);
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		OfflineUser user = new OfflineUser(uuid, yaml.getString("ip", "null"), yaml);
		user.setPreviousLocation(BukkitSerializer.locationFromString(yaml.getString("previousLocation")));
		//yaml.getString("previousRegion");
		Location currentLoc = BukkitSerializer.locationFromString(yaml.getString("location"));
		if (currentLoc == null) {
			currentLoc = Users.getSpawnLocation();
		}
		Region current = Region.getRegion(currentLoc.getWorld().getName());
		if (current.isDream()) {
			current = user.getDreamPlanet();
		}
		user.setCurrentRegion(current);
		user.getPrograms().addAll((HashSet<Integer>) yaml.get("progression.programs"));
		if (yaml.getString("progression.server") != null) {
			user.setServer(UUID.fromString(yaml.getString("progression.server")));
		}
		if (yaml.getString("progression.client") != null) {
			user.setClient(UUID.fromString(yaml.getString("progression.client")));
		}
		user.setCurrentChannel(yaml.getString("chat.current", "#"));
		user.getListening().addAll((HashSet<String>) yaml.get("chat.listening"));
		return user;
	}

	public void handleNameChange() {
		String name = yaml.getString("name");
		if (name != null && !name.equalsIgnoreCase(Bukkit.getOfflinePlayer(uuid).getName())) {
			yaml.set("previousname", name);
			Bukkit.broadcastMessage(Color.HAL + Bukkit.getOfflinePlayer(uuid).getName() + " was previously known as " + name);
		}
	}

	public static OfflineUser fromOnline(OnlineUser online) {
		OfflineUser user = OfflineUser.load(online.getUUID());

		user.setUserClass(online.getUserClass().name());
		user.setUserAspect(online.getUserAspect().name());
		user.setMediumPlanet(online.getMediumPlanet().name());
		user.setDreamPlanet(online.getDreamPlanet().name());
		user.setPreviousLocation(online.getPreviousLocation());
		// User may have logged out and not been unloaded properly
		if (user.isOnline()) {
			user.getYamlConfiguration().set("location", BukkitSerializer.locationToBlockCenterString(online.getCurrentLocation()));
			user.getYamlConfiguration().set("nickname", online.getDisplayName());
			user.setCurrentRegion(online.getCurrentRegion());
		}
		user.setProgression(online.getProgression());
		user.setServer(online.getServer());
		user.setClient(online.getClient());
		user.programs = online.getPrograms();
		user.setCurrentChannel(online.getCurrentChannel());
		user.listening = online.getListening();
		user.setSuppression(user.getSuppression());

		return user;
	}
}
