package co.sblock.users;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RegionChannel;
import co.sblock.discord.Discord;
import co.sblock.effects.Effects;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.BehaviorReactive;
import co.sblock.effects.effect.Effect;
import co.sblock.utilities.PlayerLoader;

import net.md_5.bungee.api.ChatColor;

/**
 * Storage and access of all data saved for a User.
 * 
 * @author Jikoo
 */
public class User {

	private final Sblock plugin;
	private final Users users;
	private final ChannelManager manager;
	private final YamlConfiguration yaml;

	/* General player data */
	private final UUID uuid;
	private Location previousLocation;

	/* Various data Sblock tracks for progression purposes */
	private final Set<String> programs;

	/* Chat data*/
	private String lastChat;
	private final AtomicInteger violationLevel;
	private final AtomicBoolean spamWarned;
	public String currentChannel;
	private final Set<String> listening;

	private User(Sblock plugin, UUID uuid, YamlConfiguration yaml) {
		this.plugin = plugin;
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		this.uuid = uuid;
		this.yaml = yaml;
		if (this.previousLocation == null) {
			World earth = Bukkit.getWorld("Earth");
			if (earth != null) {
				this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
			} else {
				this.previousLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
			}
		}
		this.programs = new HashSet<>();
		this.listening = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		this.lastChat = new String();
		this.violationLevel = new AtomicInteger();
		this.spamWarned = new AtomicBoolean();
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
		return yaml.getString("ip", "null");
	}

	public void setUserIP(String userIP) {
		yaml.set("ip", userIP);
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
		return getPlayer().getDisplayName();
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
		yaml.set("progression.godtier.powers", null);
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
		yaml.set("progression.godtier.powers", null);
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
	public void updateFlight() {
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = getPlayer();
				if (player == null) {
					getYamlConfiguration().set("flying", false);
					return;
				}
				boolean allowFlight = player.getWorld().getName().equals("Derspit")
								|| player.getGameMode() == GameMode.CREATIVE
								|| player.getGameMode() == GameMode.SPECTATOR;
				if (!allowFlight && player.hasPermission("sblock.command.fly.safe")) {
					Block block = player.getLocation().getBlock();
					allowFlight = block.isEmpty() && block.getRelative(BlockFace.DOWN).isEmpty();
				}
				player.setAllowFlight(allowFlight);
				player.setFlying(allowFlight);
				getYamlConfiguration().set("flying", allowFlight);
			}
		}.runTask(getPlugin());}

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
		return getPlayer().getLocation();
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
	public void updateCurrentRegion(Region newRegion, boolean force) {
		if (newRegion.isDream()) {
			getPlayer().setPlayerTime(newRegion == Region.DERSE ? 18000L : 6000L, false);
		} else {
			getPlayer().resetPlayerTime();
		}
		if (isOnline() && (force
				|| !getCurrentRegion().getResourcePackURL().equals(newRegion.getResourcePackURL()))) {
			getPlayer().setResourcePack(newRegion.getResourcePackURL());
		}
		setCurrentRegion(newRegion);
	}

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
		long time = getPlayer().getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK);
		long days = time / (24 * 60 * 60 * 20);
		time -= days * 24 * 60 * 60 * 20;
		long hours = time / (60 * 60 * 20);
		time -= hours * 60 * 60 * 20;
		time = time / (60 * 20);
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return days + " days, " + decimalFormat.format(hours) + ':' + decimalFormat.format(time);
	}

	/**
	 * Gets a Set of all Computer programs accessible by the User.
	 * 
	 * @return the programs installed
	 */
	public Set<String> getPrograms() {
		return this.programs;
	}

	/**
	 * Add an Entry to the Set of programs accessible by the User at their Computer.
	 * 
	 * @param id the id of the program to add
	 */
	public void addProgram(String id) {
		this.programs.add(id);
	}

	/**
	 * Sends the Player a message.
	 * 
	 * @param message the message to send
	 */
	public void sendMessage(String message) {
		Player player = this.getPlayer();
		if (player == null) {
			return;
		}
		player.sendMessage(message);
	}

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
				highlights.add(ChatColor.stripColor(((NickChannel) channel).getNick(this)));
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
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"));
			return;
		}
		if (channel.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), channel.getName()));
			return;
		}
		if (!channel.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()));
			return;
		}
		currentChannel = channel.getName();
		if (!this.getListening().contains(channel.getName())) {
			this.addListening(channel);
		} else {
			this.sendMessage(ChatMsgs.onChannelSetCurrent(channel.getName()));
		}
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param channelName the name of the Channel to set as current
	 */
	public synchronized void setCurrentChannel(String channelName) {
		Channel channel = getPlugin().getModule(Chat.class).getChannelManager().getChannel(channelName);
		this.setCurrentChannel(channel);
	}

	/**
	 * Gets the Channel the Player is currently sending messages to.
	 * 
	 * @return Channel
	 */
	public synchronized Channel getCurrentChannel() {
		return getChannelManager().getChannel(this.currentChannel);
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param channel the Channel to add
	 * 
	 * @return true if the Channel was added
	 */
	public synchronized boolean addListening(Channel channel) {
		if (channel == null) {
			return false;
		}
		if (channel.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), channel.getName()));
			return false;
		}
		if (!channel.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()));
			return false;
		}
		if (!this.getListening().contains(channel)) {
			this.getListening().add(channel.getName());
		}
		if (!channel.getListening().contains(this.getUUID())) {
			channel.getListening().add(this.getUUID());
			this.getListening().add(channel.getName());
			channel.sendMessage(ChatMsgs.onChannelJoin(this, channel));
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()));
			return false;
		}
	}

	/**
	 * Begin listening to a Set of channels. Used on login.
	 * 
	 * @param channels
	 */
	public void handleLoginChannelJoins() {
		for (Iterator<String> iterator = listening.iterator(); iterator.hasNext();) {
			Channel channel = getChannelManager().getChannel(iterator.next());
			if (channel != null && !(channel instanceof RegionChannel) && !channel.isBanned(this)
					&& (channel.getAccess() != AccessLevel.PRIVATE || channel.isApproved(this))) {
				channel.getListening().add(this.getUUID());
			} else {
				iterator.remove();
			}
		}
		listening.add("#");
		getChannelManager().getChannel("#").getListening().add(getUUID());
		if (this.getPlayer().hasPermission("sblock.felt") && !this.getListening().contains("@")) {
			this.getListening().add("@");
			getChannelManager().getChannel("@").getListening().add(this.getUUID());
		}
		String base = new StringBuilder(Color.GOOD_PLAYER.toString()).append(this.getDisplayName())
				.append(Color.GOOD).append(" began pestering <>").append(Color.GOOD)
				.append(" at ").append(new SimpleDateFormat("HH:mm").format(new Date())).toString();
		// Heavy loopage ensues
		for (User user : users.getOnlineUsers()) {
			if (!user.isOnline()) {
				continue;
			}
			StringBuilder matches = new StringBuilder();
			for (String channelName : this.getListening()) {
				if (user.getListening().contains(channelName)) {
					matches.append(Color.GOOD_EMPHASIS).append(channelName).append(Color.GOOD).append(", ");
				}
			}
			String message;
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.replace("<>", matches.toString()));
				int comma = msg.lastIndexOf(",");
				if (comma != -1) {
					if (comma == msg.indexOf(",")) {
						msg.replace(comma, comma + 1, " and");
					} else {
						msg.insert(comma + 1, " and");
					}
				}
				message = msg.toString();
			} else {
				message = base.replace(" <>", "");
			}
			user.sendMessage(message);
		}

		Logger.getLogger("Minecraft").info(base.toString().replace("<>", StringUtils.join(getListening(), ", ")));
	}

	/**
	 * Remove a Channel from the Player's listening List.
	 * 
	 * @param channelName the name of the Channel to remove
	 */
	public synchronized void removeListening(String channelName) {
		Channel channel = getChannelManager().getChannel(channelName);
		if (channel == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(channelName));
			this.getListening().remove(channelName);
			return;
		}
		if (this.getListening().remove(channelName)) {
			if (channel instanceof NickChannel) {
				((NickChannel) channel).removeNick(this);
			}
			channel.sendMessage(ChatMsgs.onChannelLeave(this, channel));
			channel.getListening().remove(this.getUUID());
			if (this.currentChannel != null && channelName.equals(this.getCurrentChannel().getName())) {
				this.currentChannel = null;
			}
		} else {
			this.sendMessage(Color.BAD + "You are not listening to " + Color.BAD_EMPHASIS + channelName);
		}
	}

	/**
	 * Silently removes a Channel from the Player's listening list.
	 * 
	 * @param channel the Channel to remove
	 */
	public synchronized void removeListeningSilent(Channel channel) {
		if (channel instanceof NickChannel) {
			((NickChannel) channel).removeNick(this);
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
		Channel channel = getChannelManager().getChannel(channelName);
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
	 * @param channel the Channel to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public boolean isListening(Channel channel) {
		return this.listening.contains(channel.getName());
	}

	/**
	 * Check if the Player is listening to a specific Channel.
	 * 
	 * @param channelName the Channel name to check for
	 * 
	 * @return true if the Player is listening to c
	 */
	public boolean isListening(String channelName) {
		return this.listening.contains(channelName);
	}

	/**
	 * Gets the last chat message sent by the Player.
	 * 
	 * @return the last chat message sent
	 */
	public synchronized String getLastChat() {
		return this.lastChat;
	}

	/**
	 * Sets the last chat message sent by the Player.
	 * 
	 * @param message the last chat message sent
	 */
	public synchronized void setLastChat(String message) {
		this.lastChat = message;
	}

	/**
	 * Gets the Player's chat violation level.
	 * 
	 * @return the violation level
	 */
	public int getChatViolationLevel() {
		return violationLevel.get();
	}

	/**
	 * Sets the Player's chat violation level.
	 * 
	 * @param violationLevel the violation level
	 */
	public void setChatViolationLevel(int violationLevel) {
		this.violationLevel.set(violationLevel);
	}

	/**
	 * Gets whether or not the Player has been warned to not spam.
	 * 
	 * @return true if the Player has been warned
	 */
	public boolean getChatWarnStatus() {
		return spamWarned.get();
	}

	/**
	 * Sets whether or not the Player has been warned not to spam.
	 * 
	 * @param warned whether or not the Player has been warned
	 */
	public void setChatWarnStatus(boolean warned) {
		spamWarned.set(warned);
	}

	/**
	 * Gets a List of Effect names set as active powers. If the List has not been set, creates a
	 * random assortment from available Effects for the OfflineUser's UserAspect.
	 * 
	 * @return the List of enabled godtier Effects
	 */
	public List<String> getGodtierEffects() {
		if (yaml.isSet("progression.godtier.powers")) {
			return yaml.getStringList("progression.godtier.powers");
		}
		List<Effect> active = getPlugin().getModule(Effects.class).getGodtierEffects(getUserAspect());
		ArrayList<Effect> passive = new ArrayList<>();
		active.removeIf(effect -> {
			if (effect instanceof BehaviorPassive || effect instanceof BehaviorReactive) {
				passive.add(effect);
				return true;
			}
			return false;
		});
		List<String> list = new ArrayList<>();
		for (int i = 0; i < getUserClass().getActiveSkills() && !active.isEmpty(); i++) {
			Effect effect = active.get((int) (Math.random() * active.size()));
			active.remove(effect);
			list.add(effect.getName());
		}
		for (int i = 0; i < getUserClass().getPassiveSkills() && !passive.isEmpty(); i++) {
			Effect effect = passive.get((int) (Math.random() * passive.size()));
			passive.remove(effect);
			list.add(effect.getName());
		}
		yaml.set("progression.godtier.powers", list);
		return list;
	}

	/**
	 * Attempts to add an Effect to the OfflineUser's selected godtier Effects.
	 * 
	 * @param effect the Effect
	 * @return true if the Effect can be added
	 */
	public boolean addGodtierEffect(Effect effect) {
		if (!(effect instanceof BehaviorGodtier)
				|| !((BehaviorGodtier) effect).getAspects().contains(getUserAspect())) {
			return false;
		}
		ArrayList<String> list = new ArrayList<>(yaml.getStringList("progression.godtier.powers"));
		int type = 0;
		boolean active = !(effect instanceof BehaviorPassive || effect instanceof BehaviorReactive);
		for (String effectName : list) {
			Effect enabledEffect = getPlugin().getModule(Effects.class).getEffect(effectName);
			if (enabledEffect instanceof BehaviorPassive || enabledEffect instanceof BehaviorReactive) {
				if (!active) {
					type++;
				}
			} else if (active) {
				type++;
			}
		}
		if (type >= (active ? getUserClass().getActiveSkills() : getUserClass().getPassiveSkills())) {
			return false;
		}
		if (!list.contains(effect.getName())) {
			list.add(effect.getName());
		}
		yaml.set("progression.godtier.powers", list);
		return true;
	}

	/**
	 * Removes an Effect from an OfflineUser's selected godtier Effects.
	 * 
	 * @param effect the Effect
	 */
	public void removeGodtierEffect(Effect effect) {
		if (!yaml.isSet("progression.godtier.powers")) {
			return;
		}
		ArrayList<String> list = new ArrayList<>(yaml.getStringList("progression.godtier.powers"));
		list.remove(effect.getName());
		yaml.set("progression.godtier.powers", list);
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

		// Programs: [list]
		sb.append(Color.GOOD).append("Programs: ").append(Color.GOOD_EMPHASIS)
				.append(getPrograms()).append('\n');

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
		if (object instanceof User) {
			User u = (User) object;
			return u.getUUID().equals(getUUID());
		}
		return false;
	}

	public boolean isOnline() {
		return Bukkit.getOfflinePlayer(getUUID()).isOnline();
	}

	protected YamlConfiguration getYamlConfiguration() {
		return yaml;
	}

	public void save() {
		File folder = new File(getPlugin().getDataFolder(), "users");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(folder, getUUID().toString() + ".yml");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new RuntimeException("Unable to save data for " + getUUID().toString(), e);
			}
		}
		yaml.set("name", getPlayerName());
		yaml.set("ip", getUserIP());
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

	protected ChannelManager getChannelManager() {
		return this.manager;
	}

	public Sblock getPlugin() {
		return this.plugin;
	}

	@SuppressWarnings("unchecked")
	protected static User load(Sblock plugin, final UUID uuid) {
		File folder = new File(plugin.getDataFolder(), "users");
		if (!folder.exists()) {
			folder.mkdirs();
		}
		File file = new File(folder, uuid.toString() + ".yml");
		if (!file.exists()) {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return new User(plugin, uuid, new YamlConfiguration());
			}
			player.teleport(Users.getSpawnLocation());

			User user = new User(plugin, uuid, new YamlConfiguration());
			user.setUserIP(player.getAddress().getHostString());

			// Manually set channel - using setCurrentChannel results in recursive User load
			// It's not guaranteed that this is happening during the PlayerJoinEvent either,
			// so we can't count on User#handleLoginChannelJoins being called.
			user.currentChannel = "#";
			user.listening.add("#");
			Channel hash = user.manager.getChannel("#");
			hash.getListening().add(uuid);

			user.updateCurrentRegion(Region.EARTH, true);

			if (!player.hasPlayedBefore()) {
				// Our data file may have just been deleted - reset planned for Entry, etc.
				Bukkit.broadcastMessage(Color.HAL + "It would seem that " + player.getName()
						+ " is joining us for the first time! Please welcome them.");
				plugin.getModule(Discord.class).postMessage(Discord.BOT_NAME, player.getName()
						+ " is new! Please welcome them.", true);
			} else {
				player.sendMessage(Color.HAL + "We've reset classpect since you last played. Please re-select now!");
			}
			return user;
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		User user = new User(plugin, uuid, yaml);
		user.setPreviousLocation(BukkitSerializer.locationFromString(yaml.getString("previousLocation")));
		if (user.isOnline()) {
			Location currentLoc = BukkitSerializer.locationFromString(yaml.getString("location"));
			if (currentLoc == null) {
				currentLoc = Users.getSpawnLocation();
			}
			Region currentRegion = Region.getRegion(currentLoc.getWorld().getName());
			if (currentRegion.isDream()) {
				currentRegion = user.getDreamPlanet();
			}
			user.setUserIP(user.getPlayer().getAddress().getHostString());
			user.updateCurrentRegion(currentRegion, true);
		}
		user.getPrograms().addAll((HashSet<String>) yaml.get("progression.programs"));
		Channel currentChannel = user.manager.getChannel(yaml.getString("chat.current", "#"));
		if (currentChannel != null && !currentChannel.isBanned(user) && currentChannel.isApproved(user)) {
			user.currentChannel = currentChannel.getName();
			if (!user.getListening().contains(currentChannel.getName())) {
				user.getListening().add(currentChannel.getName());
			}
		}
		user.getListening().addAll((HashSet<String>) yaml.get("chat.listening"));
		return user;
	}

	public void handleNameChange() {
		String name = yaml.getString("name");
		OfflinePlayer offline = getOfflinePlayer();
		if (name != null && !name.equalsIgnoreCase(offline.getName())) {
			yaml.set("previousname", name);
			String previous = offline.getName() + " was previously known as " + name;
			Bukkit.broadcastMessage(Color.HAL + previous);
			getPlugin().getModule(Discord.class).postMessage(Discord.BOT_NAME, previous, true);
		}
	}
}
