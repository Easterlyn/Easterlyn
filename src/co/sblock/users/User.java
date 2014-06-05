package co.sblock.users;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.channel.ChannelType;
import co.sblock.data.SblockData;
import co.sblock.effects.PassiveEffect;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.inventory.InventoryManager;
import co.sblock.utilities.regex.RegexUtils;
import co.sblock.utilities.spectator.Spectators;

/**
 * The class for storing all Player data.
 * 
 * @author Jikoo
 */
public class User {

	/** Player's UUID */
	private UUID playerID;

	/** Play time tracking information */
	private long login;

	/** The Player's IP address */
	private String userIP;

	/** Ensures that User data is not overwritten */
	private boolean loaded;

	/** Keeps track of current Region for various purposes */
	private Region currentRegion;

	/** Used to calculate elapsed times. */
	private SimpleDateFormat dateFormat;

	/* SBLOCK USER DATA BELOW */
	/** Classpect */
	private UserClass classType;
	private UserAspect aspect;
	private MediumPlanet mPlanet;
	private DreamPlanet dPlanet;
	private ProgressionState progression;

	/** The Player's tower number */
	private byte tower;

	/** Locations to teleport Players to when conditions are met */
	private Location previousLocation, serverDisableTeleport;

	/** Programs installed to the player's computer */
	private HashSet<Integer> programs;

	/** Checks made while the Player is logged in, not saved. */
	private boolean isServer, allowFlight;

	/** The UUIDs of the Player's server and client players. */
	private UUID server, client;

	/** A map of the Effects applied to the Player and their strength. */
	private HashMap<PassiveEffect, Integer> passiveEffects;


	/* CHAT USER DATA BELOW */
	/** The name of the Player's current focused Channel */
	private String current;

	/** The channels the Player is listening to */
	private HashSet<String> listening;

	/** Booleans affecting channel message reception. */
	private AtomicBoolean globalMute, suppress;
	
	/**
	 * Creates a SblockUser object for a Player.
	 * 
	 * @param playerName the name of the Player to create a SblockUser for
	 */
	public User(UUID playerID) {
		// Generic user data
		this.playerID = playerID;
		login = System.nanoTime();
		this.setUserIP();
		loaded = false;

		dateFormat = new SimpleDateFormat("DDD 'days' HH:mm:ss");
		// Time will not be properly displayed if not in UTC
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

		// SblockUser-set data
		classType = UserClass.HEIR;
		aspect = UserAspect.BREATH;
		mPlanet = MediumPlanet.LOWAS;
		dPlanet = DreamPlanet.PROSPIT;
		progression = ProgressionState.NONE;
		tower = (byte)(8 * Math.random());
		isServer = false;
		allowFlight = false;
		this.updateFlight();
		previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
		programs = new HashSet<>();
		this.passiveEffects = new HashMap<>();

		// ChatUser-set data
		current = null;
		listening = new HashSet<String>();
		globalMute = new AtomicBoolean();
		suppress = new AtomicBoolean();
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
	 * Gets the Player represented by this User. If offline, returns null.
	 * 
	 * @return the Player
	 */
	public Player getPlayer() {
		return Bukkit.getPlayer(playerID);
	}

	/**
	 * Gets the OfflinePlayer. Please note: getOfflinePlayer cannot be called on the main thread as
	 * it is blocking.
	 * 
	 * @return the OfflinePlayer
	 */
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(playerID);
	}

	/**
	 * Gets the name of the Player.
	 * 
	 * @return a String containing the Player's name
	 */
	public String getPlayerName() {
		return this.getPlayer().getName();
	}

	/**
	 * Gets the Player's chosen UserClass.
	 * 
	 * @return the UserClass, default Heir
	 */
	public UserClass getPlayerClass() {
		return this.classType;
	}

	/**
	 * Sets the Player's UserClass.
	 * 
	 * @param uclass the new UserClass
	 */
	public void setPlayerClass(String uclass) {
		this.classType = UserClass.getClass(uclass);
	}

	/**
	 * Gets the Player's chosen UserAspect.
	 * 
	 * @return the UserAspect, default Breath
	 */
	public UserAspect getAspect() {
		return this.aspect;
	}

	/**
	 * Sets the Player's UserAspect.
	 * 
	 * @param aspect the new UserAspect
	 */
	public void setAspect(String aspect) {
		this.aspect = UserAspect.getAspect(aspect);
	}

	/**
	 * Gets the Player's chosen MediumPlanet.
	 * 
	 * @return the Player's MediumPlanet
	 */
	public MediumPlanet getMediumPlanet() {
		return this.mPlanet;
	}

	/**
	 * Sets the Player's MediumPlanet.
	 * 
	 * @param mPlanet the new MediumPlanet
	 */
	public void setMediumPlanet(String mPlanet) {
		this.mPlanet = MediumPlanet.getPlanet(mPlanet);
	}

	/**
	 * Gets the Player's chosen DreamPlanet.
	 * 
	 * @return the Player's DreamPlanet
	 */
	public DreamPlanet getDreamPlanet() {
		return this.dPlanet;
	}

	/**
	 * Sets the Player's DreamPlanet.
	 * 
	 * @param dPlanet the new DreamPlanet
	 */
	public void setDreamPlanet(String dPlanet) {
		this.dPlanet = DreamPlanet.getPlanet(dPlanet);
	}

	/**
	 * Gets the Player's game progression.
	 * 
	 * @return the Player's ProgressionState
	 */
	public ProgressionState getProgression() {
		return this.progression;
	}

	/**
	 * Sets the Player's game progression.
	 * 
	 * @param progression the new ProgressionState
	 */
	public void setProgression(ProgressionState progression) {
		this.progression = progression;
	}

	/**
	 * Gets the tower number generated for the Player.
	 * 
	 * @return the number of the tower the player will "dream" to
	 */
	public byte getTower() {
		return this.tower;
	}

	/**
	 * Sets the tower number generated for the Player.
	 * 
	 * @param tower the number of the tower the Player will "dream" to
	 */
	public void setTower(byte tower) {
		this.tower = tower;
	}

	/**
	 * Gets the Player's dreamstate.
	 * 
	 * @return true if the Player is in dreamstate
	 */
	public boolean canFly() {
		return this.allowFlight;
	}

	/**
	 * Sets the Player's dreamstate.
	 * 
	 * @param allowFlight true if the Player is in dreamstate
	 */
	public void updateFlight() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				allowFlight = getPlayer() != null && (getPlayer().getWorld().getName().contains("Circle")
						|| getPlayer().getGameMode().equals(GameMode.CREATIVE)
						|| isServer || Spectators.getSpectators().isSpectator(playerID));
				if (getOfflinePlayer().isOnline()) {
					getPlayer().setAllowFlight(allowFlight);
					getPlayer().setFlying(allowFlight);
				}
			}
		});
	}

	/**
	 * Sets the Player's Location from the last World that they visited.
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
	 * Sets the Player's previous Location from a String. Only for use in DatabaseManager.
	 * 
	 * @param s
	 */
	public void setPreviousLocationFromString(String s) {
		String[] loc = s.split(",");
		World w = Bukkit.getWorld(loc[0]);
		if (w != null) {
			this.previousLocation = new Location(w, Double.parseDouble(loc[1]) + .5,
					Double.parseDouble(loc[2]) + .5, Double.parseDouble(loc[3]) + .5);
		} else {
			this.previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();
		}
	}

	/**
	 * Gets the Location of the Player prior to sleep teleportation.
	 * 
	 * @return the previousLocation
	 */
	public Location getPreviousLocation() {
		return this.previousLocation;
	}

	/**
	 * The String representation of the Player's Location prior to last sleep teleport.
	 * 
	 * @return String
	 */
	public String getPreviousLocationString() {
		return this.previousLocation.getWorld().getName() + "," + this.previousLocation.getBlockX()
				+ "," + this.previousLocation.getBlockY() + "," + this.previousLocation.getBlockZ();
	}

	/**
	 * The String representation of the Player's total time ingame.
	 * 
	 * @return the Player's time ingame
	 */
	public String getTimePlayed() {
		return dateFormat.format(new Date(getPlayer().getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK) * 50L));
	}

	/**
	 * Gets a Set of all Computer programs accessible by the Player.
	 * 
	 * @return the programs installed
	 */
	public HashSet<Integer> getPrograms() {
		return this.programs;
	}

	/**
	 * Add an Entry to the Set of programs accessible by the Player at their Computer.
	 * 
	 * @param i the number of the program to add
	 */
	public void addProgram(int i) {
		this.programs.add(i);
	}

	/**
	 * Restore the Player's installed programs from a String. For use in DatabaseManager only.
	 * 
	 * @param s the string containing programs previously installed
	 */
	public void setPrograms(String s) {
		if (s == null || s.isEmpty()) {
			return;
		}
		for (String s1 : s.split(",")) {
			this.programs.add(Integer.valueOf(s1));
		}
	}

	/**
	 * Gets a String representation of the Player's installed programs.
	 * 
	 * @return representation of the contents of programs
	 */
	public String getProgramString() {
		StringBuilder sb = new StringBuilder();
		for (int i : this.programs) {
			sb.append(i).append(',');
		}
		if (sb.length() == 0) {
			return null;
		}
		return sb.substring(0, sb.length() - 1);
	}

	/**
	 * Gets a String representation of the Player's IP.
	 * 
	 * @return the Player's IP
	 */
	public String getUserIP() {
		return this.userIP;
	}

	/**
	 * Gets the Player's current Region
	 * 
	 * @return the Region that the Player is in
	 */
	public Region getPlayerRegion() {
		return Region.getLocationRegion(this.getPlayer().getLocation());
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
		if (currentRegion != null && newR == currentRegion) {
			if (!listening.contains("#" + currentRegion.toString())) {
				Channel c = ChannelManager.getChannelManager().getChannel("#" + currentRegion.toString());
				this.addListening(c);
			}
			return;
		}
		Channel newC = ChannelManager.getChannelManager().getChannel("#" + newR.toString());
		if (current == null || currentRegion != null && current.equals("#" + currentRegion.toString())) {
			current = newC.getName();
		}
		if (currentRegion != null) {
			this.removeListening("#" + currentRegion.toString());
		}
		if (!this.listening.contains(newC.getName())) {
			this.addListening(newC);
		}
		currentRegion = newR;
		getPlayer().setResourcePack(newR.getResourcePackURL());
	}

	/**
	 * Sets the SblockUser's IP if the Player is online.
	 */
	public void setUserIP() {
		if (this.getPlayer() != null) {
			this.userIP = this.getPlayer().getAddress().getAddress().getHostAddress();
		}
	}

	/**
	 * Ensure that data is not overwritten if load is completed after quit.
	 */
	public void setLoaded() {
		this.loaded = true;
	}

	/**
	 * Check to ensure that load has been completed before saving.
	 * 
	 * @return true if load has been completed.
	 */
	public boolean isLoaded() {
		return this.loaded;
	}

	/**
	 * Check if the user is in server mode.
	 * 
	 * @return true if the user is in server mode
	 */
	public boolean isServer() {
		return this.isServer;
	}

	/**
	 * Initiate server mode! Funtimes commence.
	 */
	public void startServerMode() {
		Player p = this.getPlayer();
		if (Spectators.getSpectators().isSpectator(playerID)) {
			Spectators.getSpectators().removeSpectator(p);
		}
		if (this.client == null) {
			p.sendMessage(ChatColor.RED + "You must have a client to enter server mode!"
					+ "+\nAsk someone with " + ChatColor.AQUA + "/requestclient <player>");
			return;
		}
		User u = getUser(client);
		if (u == null) {
			p.sendMessage(ChatColor.RED + "You should wait for your client before progressing!");
			return;
		}
		if (!u.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())) {
			p.sendMessage(ChatColor.RED + u.getPlayerName() + " does not have the Sburb Client installed!");
			return;
		}
		Machine m = SblockMachines.getMachines().getManager().getComputer(client);
		if (m == null) {
			p.sendMessage(ChatColor.RED + u.getPlayerName() + " has not placed their computer in their house!");
			return;
		}
		this.serverDisableTeleport = p.getLocation();
		if (!SblockMachines.getMachines().getManager().isByComputer(u.getPlayer(), 25)) {
			p.teleport(m.getKey());
		} else {
			p.teleport(u.getPlayer());
		}
		this.isServer = true;
		this.updateFlight();
		p.setNoDamageTicks(Integer.MAX_VALUE);
		InventoryManager.storeAndClearInventory(p);
		p.getInventory().addItem(MachineType.COMPUTER.getUniqueDrop());
		p.sendMessage(ChatColor.GREEN + "Server mode enabled!");
	}

	/**
	 * Disable server mode.
	 */
	public void stopServerMode() {
		this.isServer = false;
		this.updateFlight();
		Player p = this.getPlayer();
		p.teleport(serverDisableTeleport);
		p.setFallDistance(0);
		p.setNoDamageTicks(0);
		InventoryManager.restoreInventory(p);
		p.sendMessage(ChatColor.GREEN + "Server program closed!");
	}

	/**
	 * Set the user's server player.
	 * 
	 * @param s the name of the Player to set as the server player.
	 */
	public boolean setServer(UUID userID) {
		if (this.server == null) {
			this.server = userID;
			return true;
		}
		return false;
	}

	/**
	 * Get the user's server player.
	 * 
	 * @return the saved server player
	 */
	public UUID getServer() {
		return this.server;
	}

	/**
	 * Set the user's client player.
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
	 * Get the user's client player.
	 * 
	 * @return the saved client player
	 */
	public UUID getClient() {
		return this.client;
	}
	
	/**
	 * Gets the user's current Passive Effects
	 * 
	 * @return the map of passive effects and their strengths
	 */
	public HashMap<PassiveEffect, Integer> getPassiveEffects() {
		return this.passiveEffects;
	}
	
	/**
	 * Set the user's current Passive Effects. Will overlay existing map.
	 * 
	 * @param effects the map of all PassiveEffects to add
	 */
	public void setAllPassiveEffects(HashMap<PassiveEffect, Integer> effects) {
		removeAllPassiveEffects();
		this.passiveEffects = effects;
	}
	
	/**
	 * Removes all PassiveEffects from the user and cancels the Effect
	 */
	public void removeAllPassiveEffects() {
		for (PassiveEffect effect : passiveEffects.keySet()) {
			PassiveEffect.removeEffect(getPlayer(), effect);
		}
		this.passiveEffects.clear();
	}
	
	/**
	 * Add a new effect to the user's current Passive Effects.
	 * If the effect is already present, increases the strength by 1.
	 * 
	 * @param effect the PassiveEffect to add
	 */
	public void addPassiveEffect(PassiveEffect effect) {
		if (this.passiveEffects.containsKey(effect)) {
			this.passiveEffects.put(effect, this.passiveEffects.get(effect) + 1);
		}
		else {
			this.passiveEffects.put(effect, 1);
		}
		PassiveEffect.applyEffect(getPlayer(), effect, passiveEffects.get(effect));
	}
	
	/**
	 * Set the user's current Passive Effects. Will overlay existing map.
	 * 
	 * @param effect the PassiveEffect to remove
	 */
	public void reducePassiveEffect(PassiveEffect effect, Integer reduction) {
		if (this.passiveEffects.containsKey(effect)) {
			if (this.passiveEffects.get(effect) - reduction > 0) {
				PassiveEffect.removeEffect(getPlayer(), effect);
				this.passiveEffects.put(effect, this.passiveEffects.get(effect) - reduction);
				PassiveEffect.applyEffect(getPlayer(), effect, passiveEffects.get(effect));
			}
			else {
				this.passiveEffects.remove(effect);
				PassiveEffect.removeEffect(getPlayer(), effect);
			}
		}
	}

	/**
	 * Send a message to this Player.
	 * 
	 * @param message the message to send to the player
	 * @param type the type of chat for handling purposes
	 */
	public void sendMessage(String message, boolean highlight, String... additionalMatches) {
		Player p = this.getPlayer();

		// Check to make sure user is online
		if (p == null) {
			SblockData.getDB().saveUserData(playerID);
			return;
		}

		// final output, sends message to user
		if (highlight) {
			// Checking for highlights within the message commences

			String[] matches = new String[additionalMatches.length + 1];
				matches[0] = p.getName();
			if (additionalMatches.length > 0) {
				System.arraycopy(additionalMatches, 0, matches, 1, additionalMatches.length);
			}
			StringBuilder msg = new StringBuilder();
			Matcher match = Pattern.compile(RegexUtils.ignoreCaseRegex(matches)).matcher(message);
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
		p.sendMessage(message);
	}

	/**
	 * Sets the Player's chat mute status.
	 * 
	 * @param b true if the Player is being muted
	 */
	public void setMute(boolean b) {
		this.globalMute.set(b);
	}

	/**
	 * Gets the Player's mute status.
	 * 
	 * @return true if the Player is muted
	 */
	public boolean isMute() {
		return this.globalMute.get();
	}

	/**
	 * Sets the Player's current chat suppression status.
	 * 
	 * @param b true if the player is to suppress global channels
	 */
	public void setSuppressing(boolean b) {
		this.suppress.lazySet(b);
	}

	/**
	 * Gets the Player's suppression status.
	 * 
	 * @return true if the Player is suppressing global channels.
	 */
	public boolean isSuppressing() {
		return this.suppress.get();
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param c the Channel to set as current
	 */
	public void setCurrent(Channel c) {
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel("null"), false);
			return;
		}
		if (c.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), c.getName()), false);
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()), false);
			return;
		}
		current = c.getName();
		if (!this.listening.contains(c.getName())) {
			this.addListening(c);
		} else {
			this.sendMessage(ChatMsgs.onChannelSetCurrent(c.getName()), false);
		}
	}

	/**
	 * Sets the Player's current Channel.
	 * 
	 * @param c the Channel to set as current
	 */
	public void setCurrent(String s) {
		Channel c = ChannelManager.getChannelManager().getChannel(s);
		this.setCurrent(c);
	}

	/**
	 * Gets the Channel the Player is currently sending messages to.
	 * 
	 * @return Channel
	 */
	public Channel getCurrent() {
		return ChannelManager.getChannelManager().getChannel(this.current);
	}

	/**
	 * Adds a Channel to the Player's current List of Channels listened to.
	 * 
	 * @param channel the Channel to add
	 * 
	 * @return true if the Channel was added
	 */
	public boolean addListening(Channel channel) {
		if (channel == null) {
			return false;
		}
		if (channel.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), channel.getName()), false);
			return false;
		}
		if (channel.getAccess().equals(AccessLevel.PRIVATE) && !channel.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()), false);
			return false;
		}
		if (!this.listening.contains(channel)) {
			this.listening.add(channel.getName());
		}
		if (!channel.getListening().contains(this.playerID)) {
			channel.addListening(this.playerID);
			this.listening.add(channel.getName());
			channel.sendToAll(this, ChatMsgs.onChannelJoin(this, channel), false);
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()), false);
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
	public boolean addListening(User user, String channelName) {
		Channel channel = ChannelManager.getChannelManager().getChannel(channelName);
		return this.addListening(channel);
	}

	/**
	 * Begin listening to a Set of channels. Used on login.
	 * 
	 * @param channels
	 */
	public void loginAddListening(String[] channels) {
		for (String s : channels) {
			Channel c = ChannelManager.getChannelManager().getChannel(s);
			if (c != null && !c.isBanned(this)
					&& (c.getAccess() != AccessLevel.PRIVATE || c.isApproved(this))) {
				this.listening.add((String) s);
				c.addListening(this.playerID);
			}
		}

		StringBuilder base = new StringBuilder(ChatColor.GREEN.toString())
				.append(this.getPlayerName()).append(ChatColor.YELLOW)
				.append(" began pestering <>").append(ChatColor.YELLOW).append(" at ")
				.append(new SimpleDateFormat("HH:mm").format(new Date()));
		// Heavy loopage ensues
		for (User u : UserManager.getUserManager().getUserlist()) {
			StringBuilder matches = new StringBuilder();
			for (String s : this.listening) {
				if (u.listening.contains(s)) {
					matches.append(ChatColor.GOLD).append(s).append(ChatColor.YELLOW).append(", ");
				}
			}
			if (matches.length() > 0) {
				matches.replace(matches.length() - 3, matches.length() - 1, "");
				StringBuilder msg = new StringBuilder(base.toString().replace("<>", matches.toString()));
				int comma = msg.toString().lastIndexOf(',');
				if (comma != -1) {
					u.sendMessage(msg.replace(comma, comma + 1, " and").toString(), false);
				}
			}
		}

		Bukkit.getConsoleSender().sendMessage(this.getPlayerName() + " began pestering " + StringUtils.join(channels, ' '));
	}

	/**
	 * Remove a Channel from the Player's listening List.
	 * 
	 * @param cName the name of the Channel to remove
	 */
	public void removeListening(String cName) {
		Channel c = ChannelManager.getChannelManager().getChannel(cName);
		if (c == null) {
			this.sendMessage(ChatMsgs.errorInvalidChannel(cName), false);
			this.listening.remove(cName);
			return;
		}
		if (this.listening.remove(cName)) {
			c.removeNick(this, false);
			c.sendToAll(this, ChatMsgs.onChannelLeave(this, c), false);
			c.removeListening(this.playerID);
			if (this.current != null && cName.equals(this.current)) {
				this.current = null;
			}
		} else {
			this.sendMessage(ChatMsgs.errorNotListening(cName), false);
		}
	}

	/**
	 * Silently removes a Channel from the Player's listening list.
	 * 
	 * @param channel the Channel to remove
	 */
	public void removeListeningSilent(Channel channel) {
		channel.removeNick(this, false);
		this.listening.remove(channel.getName());
		if (this.current != null && this.current.equals(channel.getName())) {
			this.current = null;
		}
		channel.removeListening(this.getUUID());
	}

	/**
	 * Tells a Channel the Player is leaving on quit.
	 * 
	 * @param cName the name of the Channel to inform
	 */
	public void removeListeningQuit(String cName) {
		Channel c = ChannelManager.getChannelManager().getChannel(cName);
		if (c != null) {
			c.removeListening(this.playerID);
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
		if (!SblockChat.getComputerRequired()) {
			// Overrides the computer limitation for pre-Entry shenanigans
			return true;
		}
		return SblockMachines.getMachines().getManager().isByComputer(this.getPlayer(), 10);
	}

	/**
	 * Method for handling all Player chat.
	 * 
	 * @param msg the message being sent
	 * @param forceThirdPerson true if the message is to be prepended with a modifier
	 */
	public void chat(String msg, boolean forceThirdPerson) {

		// Check if the user can speak
		if (this.globalMute.get()) {
			this.sendMessage(ChatMsgs.isMute(), false);
			return;
		}

		// default to current channel receiving message
		Channel sendto = ChannelManager.getChannelManager().getChannel(this.current);

		// check if chat is directed at another channel
		int space = msg.indexOf(' ');
		if (msg.charAt(0) == '@' && space > 1) {
			// Check for alternate channel destination. Failing that, warn user.
			String newChannel = msg.substring(1, space);
			if (ChannelManager.getChannelManager().isValidChannel(newChannel)) {
				sendto = ChannelManager.getChannelManager().getChannel(newChannel);
				if (sendto.getAccess().equals(AccessLevel.PRIVATE) && !sendto.isApproved(this)) {
					// User not approved in channel
					this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(sendto.getName()), false);
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
				this.sendMessage(ChatMsgs.errorInvalidChannel(newChannel), false);
				return;
			}
		} else if (sendto == null) {
			this.sendMessage(ChatMsgs.errorNoCurrent(), false);
			return;
		}

		if (sendto.getType() == ChannelType.REGION && this.suppress.get()) {
			this.sendMessage(ChatMsgs.errorSuppressingGlobal(), false);
		} else if (sendto.getType() == ChannelType.RP && !sendto.hasNick(this)) {
			this.sendMessage(ChatMsgs.errorNickRequired(sendto.getName()), false);
			return;
		}

		// Trim whitespace created by formatting codes, etc.
		msg = RegexUtils.trimExtraWhitespace(msg);
		if (msg.length() > 1 && RegexUtils.appearsEmpty(msg.substring(0 , 2).equals("#>") ? msg.substring(2) : msg)) {
			return;
		}

		// Chat is being done via /me
		if (forceThirdPerson) {
			msg = "#>" + msg;
		}

		sendto.sendToAll(this, msg, true);
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
		
		String s = sys + "-----------------------------------------\n" + 
				txt + this.getPlayer().getName() + div + this.classType.getDisplayName() + " of " + this.aspect.getDisplayName() + "\n" + 
				this.mPlanet + div + this.dPlanet.getDisplayName() + div + " Tower: " + this.tower + div + " Flight: " + this.allowFlight + "\n" + 
				" Mute: " + this.globalMute.get() + div + " Current: " + this.current + div + this.listening.toString() + "\n" +
				" Region: " + this.currentRegion + div + " Prev loc: " + this.getPreviousLocationString() + "\n" +
				" IP: " + this.userIP + "\n" +
				" Playtime: " + this.getTimePlayed() + div + " Last Login: " + new SimpleDateFormat("hh:mm 'on' dd/MM/YY").format(new Date(this.login)) + "\n" +
				sys + "-----------------------------------------";
		return s;
	}

	/**
	 * Gets a User by UUID.
	 * 
	 * @param userName the name to match
	 * 
	 * @return the User specified or null if invalid.
	 */
	public static User getUser(UUID userID) {
		return UserManager.getUserManager().getUser(userID);
	}
}
