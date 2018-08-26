package com.easterlyn.users;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.channel.AccessLevel;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NickChannel;
import com.easterlyn.chat.channel.RegionChannel;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.discord.Discord;
import com.easterlyn.effects.Effects;
import com.easterlyn.effects.effect.BehaviorGodtier;
import com.easterlyn.effects.effect.BehaviorPassive;
import com.easterlyn.effects.effect.BehaviorReactive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.utilities.player.PlayerUtils;
import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
import java.util.stream.Collectors;

/**
 * Storage and access of all data saved for a User.
 *
 * @author Jikoo
 */
public class User {

	private final Easterlyn plugin;
	private final Language lang;
	private final Users users;
	private final ChannelManager manager;
	private final YamlConfiguration yaml;

	/* General player data */
	private final UUID uuid;
	private Location backLocation;
	private Location deathLocation;

	/* Chat data*/
	private String lastChat;
	private final AtomicInteger violationLevel;
	private final AtomicBoolean spamWarned;
	public String currentChannel;
	private final Set<String> listening;

	private User(Easterlyn plugin, UUID uuid, YamlConfiguration yaml) {
		this.plugin = plugin;
		this.lang = plugin.getModule(Language.class);
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		this.uuid = uuid;
		this.yaml = yaml;
		this.listening = Collections.newSetFromMap(new ConcurrentHashMap<>());
		this.lastChat = "";
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
		return PlayerUtils.getPlayer(plugin, getUUID());
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
		return UserClass.getClass(yaml.getString("classpect.class", "Plebian"));
	}

	/**
	 * Sets the User's UserClass.
	 *
	 * @param userClass the new UserClass
	 */
	public void setUserClass(UserClass userClass) {
		yaml.set("classpect.class", userClass.toString());
		yaml.set("progression.godtier.powers", null);
	}

	/**
	 * Gets the User's chosen UserAffinity.
	 *
	 * @return the UserAffinity
	 */
	public UserAffinity getUserAffinity() {
		return UserAffinity.getAffinity(yaml.getString("classpect.aspect", "Easterlyn"));
	}

	/**
	 * Sets the User's UserAffinity.
	 *
	 * @param userAffinity the new UserAffinity
	 */
	public void setUserAffinity(UserAffinity userAffinity) {
		yaml.set("classpect.aspect", userAffinity.toString());
		yaml.set("progression.godtier.powers", null);
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
					yaml.set("flying", false);
					return;
				}
				boolean allowFlight = player.getWorld().getName().equals("Derspit")
								|| player.getGameMode() == GameMode.CREATIVE
								|| player.getGameMode() == GameMode.SPECTATOR;
				if (!allowFlight && player.hasPermission("easterlyn.command.fly.safe")) {
					allowFlight = player.isOnGround();
				}
				player.setAllowFlight(allowFlight);
				player.setFlying(allowFlight);
				yaml.set("flying", allowFlight);
			}
		}.runTaskLater(getPlugin(), 10L);
	}

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

	/**
	 * Gets the Location used by the back command. This is temporary data, and is not preserved.
	 *
	 * @return the back location, or null if not set
	 */
	public Location getBackLocation() {
		return this.backLocation;
	}

	/**
	 * Sets the Location used by the back command. This is temporary data, and is not preserved.
	 */
	public void setBackLocation(Location location) {
		this.backLocation = location.add(0, 0.1, 0);
	}

	/**
	 * Gets the Location used by the deathpoint command. This is temporary data, and is not preserved.
	 *
	 * @return the death location, or null if not set
	 */
	public Location getDeathLocation() {
		return this.deathLocation;
	}

	/**
	 * Sets the Location used by the deathpoint command. This is temporary data, and is not preserved.
	 */
	public void setDeathLocation(Location location) {
		this.deathLocation = location;
	}

	/**
	 * Gets a Location to teleport the Player to on login.
	 *
	 * @return the Location, or null if none is set.
	 */
	public Location getLoginLocation() {
		Object location = yaml.get("loginLocation");
		if (location instanceof Location) {
			return (Location) location;
		}
		return null;
	}

	/**
	 * Sets a Location to teleport the Player to when logging in.
	 *
	 * @param location the Location to teleport to on join
	 */
	public void setLoginLocation(Location location) {
		yaml.set("loginLocation", location);
	}

	/**
	 * The String representation of the Player's total time in game.
	 *
	 * @return the Player's time in game
	 */
	private String getTimePlayed() {
		int time = getPlayer().getStatistic(Statistic.PLAY_ONE_MINUTE) / 20 / 60;
		int days = time / (24 * 60);
		time -= days * 24 * 60;
		int hours = time / (60);
		time -= hours * 60;
		DecimalFormat decimalFormat = new DecimalFormat("00");
		return days + " days, " + decimalFormat.format(hours) + ':' + decimalFormat.format(time);
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
			highlights.add(this.getPlayerName());
			highlights.add(ChatColor.stripColor(this.getDisplayName()));
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
			this.sendMessage(lang.getValue("chat.error.invalidChannel").replace("{CHANNEL}", "null"));
			return;
		}
		if (channel.isBanned(this)) {
			this.sendMessage(lang.getValue("chat.error.banned").replace("{CHANNEL}", channel.getName()));
			return;
		}
		if (!channel.isApproved(this)) {
			this.sendMessage(lang.getValue("chat.error.private").replace("{CHANNEL}", channel.getName()));
			return;
		}
		currentChannel = channel.getName();
		if (!this.getListening().contains(channel.getName())) {
			this.addListening(channel);
		} else {
			this.sendMessage(lang.getValue("chat.channel.setcurrent").replace("{CHANNEL}", channel.getName()));
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
			this.sendMessage(lang.getValue("chat.error.banned").replace("{CHANNEL}", channel.getName()));
			return false;
		}
		if (!channel.isApproved(this)) {
			this.sendMessage(lang.getValue("chat.error.private").replace("{CHANNEL}", channel.getName()));
			return false;
		}
		this.getListening().add(channel.getName());
		if (!channel.getListening().contains(this.getUUID())) {
			channel.getListening().add(this.getUUID());
			this.getListening().add(channel.getName());
			channel.sendMessage(lang.getValue("chat.channel.join", true)
					.replace("{PLAYER}", this.getDisplayName())
					.replace("{CHANNEL}", channel.getName()));
			return true;
		} else {
			this.sendMessage(lang.getValue("chat.error.alreadyListening").replace("{CHANNEL}", channel.getName()));
			return false;
		}
	}

	/**
	 * Begin listening to a Set of channels. Used on login.
	 *
	 * @param announce true if joins are to be announced.
	 */
	public void handleLoginChannelJoins(boolean announce) {
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
		if (this.getPlayer().hasPermission(UserRank.MOD.getPermission()) && !this.getListening().contains("@")) {
			this.getListening().add("@");
			getChannelManager().getChannel("@").getListening().add(this.getUUID());
		}
		String base = lang.getValue("chat.channel.join", true).replace("{PLAYER}", this.getDisplayName());

		String all = base.replace("{CHANNEL}", StringUtils.join(getListening(), ", "));
		int lastComma = all.lastIndexOf(',');
		if (lastComma > -1) {
			all = all.substring(0, lastComma) + " and" + all.substring(lastComma + 1);
		}
		Logger.getLogger("Minecraft").info(all);
		this.sendMessage(all);

		if (!announce) {
			return;
		}

		// Heavy loopage ensues
		for (User user : users.getOnlineUsers()) {
			if (!user.isOnline() || user == this) {
				continue;
			}
			// TODO support ignore, softmute
			StringBuilder matches = new StringBuilder();
			for (String channelName : this.getListening()) {
				if (user.getListening().contains(channelName)) {
					matches.append(Language.getColor("link_channel")).append(channelName).append(Language.getColor("neutral")).append(", ");
				}
			}
			String message;
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.replace("{CHANNEL}", matches.toString()));
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
				message = base.replace(" {CHANNEL}", "");
			}
			user.sendMessage(message);
		}
	}

	/**
	 * Remove a Channel from the Player's listening List.
	 *
	 * @param channelName the name of the Channel to remove
	 */
	public synchronized void removeListening(String channelName) {
		Channel channel = getChannelManager().getChannel(channelName);
		if (channel == null) {
			this.sendMessage(lang.getValue("chat.error.invalidChannel").replace("{CHANNEL}", channelName));
			this.getListening().remove(channelName);
			return;
		}
		if (this.getListening().remove(channelName)) {
			if (channel instanceof NickChannel) {
				((NickChannel) channel).removeNick(this);
			}
			channel.sendMessage(lang.getValue("chat.channel.quit", true)
					.replace("{PLAYER}", this.getDisplayName()).replace("{CHANNEL}", channelName));
			channel.getListening().remove(this.getUUID());
			if (this.currentChannel != null && channelName.equals(this.getCurrentChannel().getName())) {
				this.currentChannel = null;
			}
		} else {
			this.sendMessage(lang.getValue("chat.error.notListening").replace("{CHANNEL}", channelName));
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
	 * @return true if the Player is listening to the Channel
	 */
	public boolean isListening(Channel channel) {
		return this.listening.contains(channel.getName());
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
	 * random assortment from available Effects for the OfflineUser's UserAffinity.
	 *
	 * @return the List of enabled godtier Effects
	 */
	public List<String> getGodtierEffects() {
		if (yaml.isSet("progression.godtier.powers")) {
			return yaml.getStringList("progression.godtier.powers");
		}
		List<Effect> active = getPlugin().getModule(Effects.class).getGodtierEffects(getUserAffinity());
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
				|| !((BehaviorGodtier) effect).getAffinity().contains(getUserAffinity())) {
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
	 * @return the List of commands to run on login
	 */
	public List<String> getLoginCommands() {
		return yaml.getStringList("misc.logincommands");
	}

	/**
	 * Sets a List of commands to be sent on login.
	 *
	 * @param commands the List of commands to run on login
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
		return new StringBuilder().append(Language.getColor("neutral")).append(ChatColor.STRIKETHROUGH)
				.append("+---").append(Language.getColor("emphasis.neutral")).append(' ').append(getPlayerName())
				.append(' ').append(Language.getColor("neutral")).append(ChatColor.STRIKETHROUGH)
				.append("---+\n").append(Language.getColor("neutral")).append(getUserClass().getDisplayName())
				.append(Language.getColor("emphasis.neutral")).append(" of ").append(getUserAffinity().getColor())
				.append(getUserAffinity().getDisplayName()).toString();
	}

	/**
	 * Gets a user-friendly representation of all of this User's information.
	 *
	 * @return all stored data for this User
	 */
	public String getWhois() {
		StringBuilder sb = new StringBuilder();
		//+-- Name from IP --+
		sb.append(Language.getColor("neutral")).append(ChatColor.STRIKETHROUGH).append("+--")
				.append(Language.getColor("emphasis.neutral")).append(' ').append(getPlayerName())
				.append(Language.getColor("neutral")).append(" from ").append(Language.getColor("emphasis.neutral"))
				.append(getUserIP()).append(Language.getColor("neutral")).append(' ')
				.append(ChatColor.STRIKETHROUGH).append("--+\n");

		// UUID: uuid
		sb.append(Language.getColor("neutral")).append("UUID: ").append(getUUID()).append('\n');

		// If stored, Previously known as: Name
		if (yaml.getString("previousname") != null) {
			sb.append(Language.getColor("neutral")).append("Previously known as: ").append(Language.getColor("emphasis.neutral"))
					.append(yaml.getString("previousname")).append('\n');
		}

		// Class of Aspect
		sb.append(Language.getColor("emphasis.neutral")).append(getUserClass().getDisplayName())
				.append(Language.getColor("neutral")).append(" of ").append(Language.getColor("emphasis.neutral"))
				.append(getUserAffinity().getDisplayName()).append('\n');

		// Loc: current location, RegionUtils: region
		sb.append(Language.getColor("neutral")).append("Loc: ").append(Language.getColor("emphasis.neutral"))
				.append(BukkitSerializer.locationToBlockCenterString(getPlayer().getLocation())).append('\n');

		// Pestering: current, Listening: [list]
		sb.append(Language.getColor("neutral")).append("Pestering: ").append(Language.getColor("emphasis.neutral"))
				.append(getCurrentChannel() != null ? getCurrentChannel().getName() : "null")
				.append(Language.getColor("neutral")).append(", Listening: ").append(Language.getColor("emphasis.neutral"))
				.append(getListening()).append('\n');

		// Muted: boolean, Suppressing: boolean
		sb.append(Language.getColor("neutral")).append("Suppressing: ").append(Language.getColor("emphasis.neutral"))
				.append(getSuppression()).append('\n');

		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm 'on' dd/MM/YY");
		// Seen: date, Playtime: X days, XX:XX since XX:XX on XX/XX/XX
		sb.append(Language.getColor("neutral")).append("Seen: ").append(Language.getColor("emphasis.neutral"))
				.append(dateFormat.format(new Date(getOfflinePlayer().getLastPlayed())))
				.append(Language.getColor("neutral")).append(", Ingame: ").append(Language.getColor("emphasis.neutral"))
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

	public Easterlyn getPlugin() {
		return this.plugin;
	}

	@SuppressWarnings("unchecked")
	protected static User load(Easterlyn plugin, final UUID uuid) {
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

			User user = new User(plugin, uuid, new YamlConfiguration());
			user.yaml.set("ip", player.getAddress().getHostString());

			Chat chat = plugin.getModule(Chat.class);
			Channel hash = chat.getChannelManager().getChannel("#");
			MessageBuilder base = chat.getHalBase().setChannel(hash);

			if (!player.hasPlayedBefore()) {
				Discord discord = plugin.getModule(Discord.class);
				discord.postMessage(null, player.getName()
						+ " is new! Please welcome them.", true);
				base.setMessage(Language.getColor("bot_text") + "It would seem that " + player.getName()
						+ " is joining us for the first time! Please welcome them.").toMessage()
						.send(Bukkit.getOnlinePlayers().stream()
								.filter(online -> !online.getUniqueId().equals(player.getUniqueId()))
								.collect(Collectors.toCollection(ArrayList<Object>::new)), false);
			}

			// Manually set channel - using setCurrentChannel results in recursive User load
			// It's not guaranteed that this is happening during the PlayerJoinEvent either,
			// so we can't count on User#handleLoginChannelJoins being called.
			user.currentChannel = "#";
			user.listening.add("#");
			hash.getListening().add(uuid);

			return user;
		}
		YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
		User user = new User(plugin, uuid, yaml);
		if (user.isOnline()) {
			yaml.set("ip", user.getPlayer().getAddress().getHostString());
		}
		Channel currentChannel = user.manager.getChannel(yaml.getString("chat.current", "#"));
		if (currentChannel != null && !currentChannel.isBanned(user) && currentChannel.isApproved(user)) {
			user.currentChannel = currentChannel.getName();
			user.getListening().add(currentChannel.getName());
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
			Chat chat = plugin.getModule(Chat.class);
			chat.getHalBase().setChannel(chat.getChannelManager().getChannel("#"))
					.setMessage(Language.getColor("bot_text") + previous).toMessage()
					.send(Bukkit.getOnlinePlayers(), false);
			Discord discord = getPlugin().getModule(Discord.class);
			discord.postMessage(null, previous, true);
			discord.updateLinkedUser(null, getUUID());
		}
	}

}
