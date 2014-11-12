package co.sblock.users;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.chat.ChatMsgs;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.AccessLevel;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.ChannelManager;
import co.sblock.data.SblockData;
import co.sblock.effects.PassiveEffect;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryManager;
import co.sblock.utilities.progression.ServerMode;
import co.sblock.utilities.regex.RegexUtils;
import co.sblock.utilities.spectator.Spectators;

/**
 * The class for storing all Player data.
 * 
 * @author Jikoo
 */
public class User {

	/* Player's UUID */
	private final UUID playerID;

	/* The Player's IP address */
	private String userIP;

	/* Ensures that User data is not overwritten */
	private transient boolean loaded;

	/* Keeps track of current Region for various purposes */
	private Region currentRegion;

	/* Used to calculate elapsed times. */
	public static transient final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("DDD 'days' HH:mm:ss");
	static {
		DATE_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/* Classpect */
	private UserClass classType;
	private UserAspect aspect;
	private Region mPlanet, dPlanet;
	private ProgressionState progression;

	/* Locations to teleport Players to when conditions are met */
	private SerialisableLocation location;
	private transient Location previousLocation;
	private transient Location serverDisableTeleport;

	/* Programs installed to the player's computer */
	private Set<Integer> programs;

	/* Checks made while the Player is logged in, not saved. */
	private transient boolean isServer, allowFlight;

	/* The UUIDs of the Player's server and client players. */
	private UUID server, client;

	/* A map of the Effects applied to the Player and their strength. */
	private transient Map<PassiveEffect, Integer> passiveEffects = new HashMap<>();

	/* The name of the Player's current focused Channel */
	private String currentChannel;

	/* The channels the Player is listening to */
	private Set<String> listening;

	/* Booleans affecting channel message reception. */
	private AtomicBoolean globalMute;
	private transient AtomicBoolean suppress = new AtomicBoolean();

	/**
	 * 
	 * @author ted
	 *
	 * Factory pattern for creating Users
	 * Must be a static class inside the User class for access to the private constructor
	 */
	public static class UserSpawner {
		/* USER DEFAULTS */
		/* these directly mimic the data of the player itself */
		private String IPAddr = "offline";

		private boolean loaded = false;
		private boolean isServer = false;
		private boolean allowFlight = false;

		private UserClass classType = UserClass.HEIR;
		private UserAspect aspect = UserAspect.BREATH;
		private Region mPlanet = Region.LOWAS;
		private Region dPlanet = Region.PROSPIT;
		private ProgressionState progression = ProgressionState.NONE;

		private Location previousLocation = null;
		private Set<Integer> programs = new HashSet<>();
		private Map<PassiveEffect, Integer> passiveEffects = new HashMap<>();
		private String currentChannel = null;
		private HashSet<String> listening = new HashSet<>();
		private AtomicBoolean globalMute = new AtomicBoolean();
		private AtomicBoolean suppress = new AtomicBoolean();

		/**
		 * @param iPAddr the iPAddr to set
		 */
		public UserSpawner setIPAddr(String iPAddr) {
			IPAddr = iPAddr;
			return this;
		}

		/**
		 * @param loaded the loaded to set
		 */
		public UserSpawner setLoaded(boolean loaded) {
			this.loaded = loaded;
			return this;
		}

		/**
		 * @param isServer the isServer to set
		 */
		public UserSpawner setServer(boolean isServer) {
			this.isServer = isServer;
			return this;
		}

		/**
		 * @param allowFlight the allowFlight to set
		 */
		public UserSpawner setAllowFlight(boolean allowFlight) {
			this.allowFlight = allowFlight;
			return this;
		}

		/**
		 * @param classType the classType to set
		 */
		public UserSpawner setClassType(UserClass classType) {
			this.classType = classType;
			return this;
		}

		/**
		 * @param aspect the aspect to set
		 */
		public UserSpawner setAspect(UserAspect aspect) {
			this.aspect = aspect;
			return this;
		}

		/**
		 * @param mPlanet the mPlanet to set
		 */
		public UserSpawner setmPlanet(Region mPlanet) {
			if (!mPlanet.isMedium()) {
				throw new RuntimeException("Invalid medium planet: received " + mPlanet.name());
			}
			this.mPlanet = mPlanet;
			return this;
		}

		/**
		 * @param dPlanet the dPlanet to set
		 */
		public UserSpawner setdPlanet(Region dPlanet) {
			if (!dPlanet.isDream()) {
				throw new RuntimeException("Invalid dream planet: received " + dPlanet.name() + ", expected (INNER|OUTER)CIRCLE.");
			}
			this.dPlanet = dPlanet;
			return this;
		}

		/**
		 * @param progression the progression to set
		 */
		public UserSpawner setProgression(ProgressionState progression) {
			this.progression = progression;
			return this;
		}

		/**
		 * @param previousLocation the previousLocation to set
		 */
		public UserSpawner setPreviousLocation(Location previousLocation) {
			this.previousLocation = previousLocation;
			return this;
		}

		/**
		 * @param programs the programs to set
		 */
		public UserSpawner setPrograms(Set<Integer> programs) {
			this.programs = programs;
			return this;
		}

		/**
		 * @param passiveEffects the passiveEffects to set
		 */
		public UserSpawner setPassiveEffects(Map<PassiveEffect, Integer> passiveEffects) {
			this.passiveEffects = passiveEffects;
			return this;
		}

		/**
		 * @param currentChannel the currentChannel to set
		 */
		public UserSpawner setCurrentChannel(String currentChannel) {
			this.currentChannel = currentChannel;
			return this;
		}

		/**
		 * @param listening the listening to set
		 */
		public UserSpawner setListening(HashSet<String> listening) {
			this.listening = listening;
			return this;
		}

		/**
		 * @param globalMute the globalMute to set
		 */
		public UserSpawner setGlobalMute(AtomicBoolean globalMute) {
			this.globalMute = globalMute;
			return this;
		}

		/**
		 * @param suppress the suppress to set
		 */
		public UserSpawner setSuppress(AtomicBoolean suppress) {
			this.suppress = suppress;
			return this;
		}

		/**
		 * 
		 * @param userID the user id
		 * @return a user with all the traits that have been added to the spawner
		 */
		public User build(UUID userID) {
			try {
				if (Bukkit.getOfflinePlayer(userID).isOnline()) {
					// IP comes out as /123.456.789.0, leading slash must be removed to properly IP ban.
					setIPAddr(Bukkit.getPlayer(userID).getAddress().getAddress().toString()
							.substring(1));
				}
			} catch(Exception e) {
				
			}
			return new User(userID, loaded, classType, aspect, mPlanet, dPlanet, progression,
					isServer, allowFlight, IPAddr, previousLocation, currentChannel,
					passiveEffects, programs, listening, globalMute, suppress);
		}

	}

	/**
	 * Creates a SblockUser object for a Player.
	 * 
	 * @param playerName the name of the Player to create a SblockUser for
	 */
	private User(UUID userID, boolean loaded, UserClass userClass, UserAspect aspect, Region mplanet,
				Region dplanet, ProgressionState progstate, boolean isServer, boolean allowFlight, String IP,
				Location previousLocation, String currentChannel, Map<PassiveEffect, Integer> passiveEffects,
				Set<Integer> programs, Set<String> listening, AtomicBoolean globalMute, AtomicBoolean supress) {
		this.playerID = userID;
		this.loaded = loaded;
		this.classType = userClass;
		this.aspect = aspect;
		this.mPlanet = mplanet;
		this.dPlanet = dplanet;
		this.progression = progstate;
		this.isServer = isServer;
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
		this.passiveEffects = passiveEffects;
		this.listening = listening;
		this.globalMute = globalMute;
		this.suppress = supress;
		this.userIP = IP;
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
	 * Gets the OfflinePlayer.
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
	public Region getMediumPlanet() {
		return this.mPlanet;
	}

	/**
	 * Sets the Player's MediumPlanet.
	 * 
	 * @param mPlanet the new MediumPlanet
	 */
	public void setMediumPlanet(String mPlanet) {
		Region planet = Region.uValueOf(mPlanet);
		if (!planet.isMedium()) {
			throw new RuntimeException("Invalid medium planet: received " + planet.name());
		}
		this.mPlanet = planet;
	}

	/**
	 * Gets the Player's chosen DreamPlanet.
	 * 
	 * @return the Player's DreamPlanet
	 */
	public Region getDreamPlanet() {
		return this.dPlanet;
	}

	/**
	 * Sets the Player's DreamPlanet.
	 * 
	 * @param dPlanet the new DreamPlanet
	 */
	public void setDreamPlanet(String dPlanet) {
		Region planet = Region.uValueOf(dPlanet);
		if (!planet.isDream()) {
			throw new RuntimeException("Invalid dream planet: received " + planet.name() + ", expected (INNER|OUTER)CIRCLE.");
		}
		this.dPlanet = planet;
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
		return DATE_FORMATTER.format(new Date(getPlayer().getStatistic(org.bukkit.Statistic.PLAY_ONE_TICK) * 50L));
	}

	/**
	 * Gets a Set of all Computer programs accessible by the Player.
	 * 
	 * @return the programs installed
	 */
	public Set<Integer> getPrograms() {
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
		String world = this.getPlayer().getWorld().getName();
		Region r = Region.uValueOf(world);
		// TODO fix: store in db, users may not be asleep post-Entry
		if (r.isDream()) {
			r = this.getDreamPlanet();
		}
		return r;
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
			if (!listening.contains(currentRegion.getChannelName())) {
				Channel c = ChannelManager.getChannelManager().getChannel(currentRegion.getChannelName());
				this.addListening(c);
			}
			return;
		}
		if (currentChannel == null || currentRegion != null && currentChannel.equals(currentRegion.getChannelName())) {
			currentChannel = newR.getChannelName();
		}
		if (currentRegion != null && !currentRegion.getChannelName().equals(newR.getChannelName())) {
			this.removeListening(currentRegion.getChannelName());
		}
		if (!this.listening.contains(newR.getChannelName())) {
			this.addListening(ChannelManager.getChannelManager().getChannel(newR.getChannelName()));
		}
		if (newR.isDream()) {
			this.getPlayer().setPlayerTime(newR == Region.DERSE ? 18000L : 6000L, false);
		} else {
			this.getPlayer().resetPlayerTime();
		}
		if (currentRegion == null || !currentRegion.getResourcePackURL().equals(newR.getResourcePackURL())) {
			getPlayer().setResourcePack(newR.getResourcePackURL());
		}
		currentRegion = newR;
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
		User u = UserManager.getUser(client);
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
		p.getInventory().addItem(MachineType.CRUXTRUDER.getUniqueDrop());
		p.getInventory().addItem(MachineType.PUNCH_DESIGNIX.getUniqueDrop());
		p.getInventory().addItem(MachineType.TOTEM_LATHE.getUniqueDrop());
		p.getInventory().addItem(MachineType.ALCHEMITER.getUniqueDrop());
		for (Material mat : ServerMode.getInstance().getApprovedSet()) {
			p.getInventory().addItem(new ItemStack(mat));
		}
		p.sendMessage(ChatColor.GREEN + "Server mode enabled!");
	}

	/**
	 * Disable server mode.
	 */
	public void stopServerMode() {
		if (Bukkit.getOfflinePlayer(client).isOnline()) {
			Player clientPlayer = Bukkit.getPlayer(client);
			for (ItemStack is : getPlayer().getInventory()) {
				if (Captcha.isPunch(is)) {
					clientPlayer.getWorld().dropItem(clientPlayer.getLocation(), is).setPickupDelay(0);
					break;
				}
			}
		}
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
	public Map<PassiveEffect, Integer> getPassiveEffects() {
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
		if (passiveEffects != null)
		{
			for (PassiveEffect effect : passiveEffects.keySet()) {
				PassiveEffect.removeEffect(getPlayer(), effect);
			}
			this.passiveEffects.clear();
		}
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
	 */
	public void sendMessage(String message) {
		Player p = this.getPlayer();

		// Check to make sure user is online
		if (p == null) {
			SblockData.getDB().saveUserData(playerID);
			return;
		}

		p.sendMessage(message);
	}

	/**
	 * Sends a raw message that will attempt to highlight the user.
	 * 
	 * @param message
	 * @param additionalMatches
	 */
	public void rawHighlight(String message, String... additionalMatches) {
		Player p = this.getPlayer();

		String[] matches = new String[additionalMatches.length + 2];
		matches[0] = p.getName();
		matches[1] = ChatColor.stripColor(p.getDisplayName());
		if (additionalMatches.length > 0) {
			System.arraycopy(additionalMatches, 0, matches, 2, additionalMatches.length);
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
			message = message.replaceFirst("\\[(" + ChatColor.COLOR_CHAR + ".*?)\\]", ChatColor.AQUA + "!!$1" + ChatColor.AQUA +"!!");
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

		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + " " + message);
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
		return suppress.get();
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
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), c.getName()));
			return;
		}
		if (c.getAccess().equals(AccessLevel.PRIVATE) && !c.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(c.getName()));
			return;
		}
		currentChannel = c.getName();
		if (!this.listening.contains(c.getName())) {
			this.addListening(c);
		} else {
			this.sendMessage(ChatMsgs.onChannelSetCurrent(c.getName()));
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
		if (channel == null) {
			return false;
		}
		if (channel.isBanned(this)) {
			this.sendMessage(ChatMsgs.onUserBanAnnounce(this.getPlayerName(), channel.getName()));
			return false;
		}
		if (channel.getAccess().equals(AccessLevel.PRIVATE) && !channel.isApproved(this)) {
			this.sendMessage(ChatMsgs.onUserDeniedPrivateAccess(channel.getName()));
			return false;
		}
		if (!this.listening.contains(channel)) {
			this.listening.add(channel.getName());
		}
		if (!channel.getListening().contains(this.playerID)) {
			channel.addListening(this.playerID);
			this.listening.add(channel.getName());
			channel.sendMessage(ChatMsgs.onChannelJoin(this, channel));
			return true;
		} else {
			this.sendMessage(ChatMsgs.errorAlreadyListening(channel.getName()));
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
				this.listening.add(s);
				c.addListening(this.playerID);
			}
		}
		if (this.getPlayer().hasPermission("group.felt") && !this.listening.contains("@")) {
			this.listening.add("@");
			ChannelManager.getChannelManager().getChannel("@").addListening(this.playerID);
		}

		StringBuilder base = new StringBuilder(ChatColor.GREEN.toString())
				.append(this.getPlayer().getDisplayName()).append(ChatColor.YELLOW)
				.append(" logs the fuck in and begins pestering <>").append(ChatColor.YELLOW)
				.append(" at ").append(new SimpleDateFormat("HH:mm").format(new Date()));
		// Heavy loopage ensues
		for (User u : UserManager.getUsers()) {
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
					u.sendMessage(msg.replace(comma, comma + 1, " and").toString());
				} else {
					u.sendMessage(msg.toString());
				}
			} else {
				u.sendMessage(base.toString().replace(" and begins pestering <>", ""));
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
			this.sendMessage(ChatMsgs.errorInvalidChannel(cName));
			this.listening.remove(cName);
			return;
		}
		if (this.listening.remove(cName)) {
			c.removeNick(this, false);
			c.sendMessage(ChatMsgs.onChannelLeave(this, c));
			c.removeListening(this.playerID);
			if (this.currentChannel != null && cName.equals(this.currentChannel)) {
				this.currentChannel = null;
			}
		} else {
			this.sendMessage(ChatMsgs.errorNotListening(cName));
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
		if (this.currentChannel != null && this.currentChannel.equals(channel.getName())) {
			this.currentChannel = null;
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
				this.mPlanet + div + this.dPlanet.getWorldName() + div + " Flight: " + this.allowFlight + "\n" + 
				" Mute: " + this.globalMute.get() + div + " Current: " + this.currentChannel + div + this.listening.toString() + "\n" +
				" Region: " + this.currentRegion + div + " Prev loc: " + this.getPreviousLocationString() + "\n" +
				" IP: " + this.userIP + "\n" +
				" Playtime: " + this.getTimePlayed() + div + " Last Login: Online now!\n" +
				sys + "-----------------------------------------";
		return s;
	}

	public boolean equals(Object object)
	{
		if (object == this)
		{
			return true;
		}
		if (object == null)
		{
			return false;
		}
		if (object instanceof User) {
			User u = (User) object;
			u.getUUID().equals(getUUID());
		}
		return false;
	}
	
	public void setUpForSerialization()
	{
		location = new SerialisableLocation(previousLocation);
	}
	
	public void initAfterDeserialization()
	{
		previousLocation = location.asLocation();
	}
}
