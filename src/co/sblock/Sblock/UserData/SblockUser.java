package co.sblock.Sblock.UserData;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.SblockEffects.PassiveEffect;

/**
 * SblockUser is the class for storing all Player data.
 * 
 * @author Jikoo, Dublek, FireNG
 */
public class SblockUser {

	/** The Player */
	private String playerName;

	/** The Player's chosen class */
	private UserClass classType = UserClass.HEIR;

	/** The Player's chosen aspect */
	private UserAspect aspect = UserAspect.BREATH;

	/** The Player's chosen Medium planet. */
	private MediumPlanet mPlanet = MediumPlanet.LOWAS;

	/** The Player's chosen dream planet. */
	private DreamPlanet dPlanet = DreamPlanet.PROSPIT;

	/** The Player's tower number */
	private byte tower = (byte)(8 * Math.random());

	/** true if the Player is in dreamstate */
	private boolean sleeping = false;

	/** The Player's location prior to sleeping to swap worlds */
	private Location previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();

	/** The total time the Player has spent logged in */
	private long timePlayed = 0L;

	/** The Player's last login */
	private Date login = new Date();

	/** The Player's IP address */
	private String userIP;

	/** Programs installed to the player's computer */
	private Set<Integer> programs = new HashSet<Integer>();

	/** UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing */
	private byte uhc = -1;

	/** Boolean for ensuring that all PlayerData has been loaded. */
	private boolean loaded = false;

	/** true if the user is in server mode. */
	private boolean server = false;

	/** true if the user has a server. */
	private boolean hasServer = false;
	
	/** A map of the Effects applied to the Player and their strength. */
	private HashMap<PassiveEffect, Integer> passiveEffects;
	
	
	/**
	 * Creates a SblockUser object for a Player.
	 * 
	 * @param playerName the name of the Player to create a SblockUser for
	 */
	public SblockUser(String playerName) {
		this.playerName = playerName;
		this.setUserIP();
		this.passiveEffects = new HashMap<PassiveEffect, Integer>();
	}

	/**
	 * Gets the Player.
	 * 
	 * @return the Player
	 */
	public Player getPlayer() {
		return Bukkit.getPlayerExact(playerName);
	}

	/**
	 * Gets the OfflinePlayer.
	 * 
	 * @return the OfflinePlayer
	 */
	public OfflinePlayer getOfflinePlayer() {
		return Bukkit.getOfflinePlayer(playerName);
	}

	/**
	 * Gets the name of the Player.
	 * 
	 * @return the Player
	 */
	public String getPlayerName() {
		return this.playerName;
	}

	/**
	 * Gets the Player's chosen UserClass.
	 * 
	 * @return the UserClass, null if unchosen
	 */
	public UserClass getClassType() {
		return this.classType;
	}

	/**
	 * Gets the Player's chosen UserAspect.
	 * 
	 * @return the UserAspect, null if unchosen
	 */
	public UserAspect getAspect() {
		return this.aspect;
	}

	/**
	 * Gets the Player's chosen MediumPlanet.
	 * 
	 * @return the Player's MediumPlanet
	 */
	public MediumPlanet getMPlanet() {
		return this.mPlanet;
	}

	/**
	 * Gets the Player's chosen DreamPlanet.
	 * 
	 * @return the Player's DreamPlanet
	 */
	public DreamPlanet getDPlanet() {
		return this.dPlanet;
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
	 * Gets the Player's dreamstate.
	 * 
	 * 
	 * @return true if the Player is in dreamstate
	 */
	public boolean isSleeping() {
		return this.sleeping;
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
	 * Sets the Player's UserAspect.
	 * 
	 * @param aspect the new UserAspect
	 */
	public void setAspect(String aspect) {
		this.aspect = UserAspect.getAspect(aspect);
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
	 * Sets the Player's DreamPlanet.
	 * 
	 * @param dPlanet the new DreamPlanet
	 */
	public void setDreamPlanet(String dPlanet) {
		this.dPlanet = DreamPlanet.getPlanet(dPlanet);
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
	 * Sets the Player's dreamstate.
	 * 
	 * @param sleeping true if the Player is in dreamstate
	 */
	public void updateSleepstate() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				sleeping = getPlayer().getWorld().getName().contains("Circle")
						|| getPlayer().getGameMode().equals(GameMode.CREATIVE);
				getPlayer().setAllowFlight(sleeping);
				getPlayer().setFlying(sleeping);
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
	 * Sets the Player's previous Location from a String. Only for use in
	 * DatabaseManager.
	 * 
	 * @param s
	 */
	public void setPreviousLocationFromString(String s) {
		String[] loc = s.split(",");
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
	 * Gets the Location of the Player prior to sleep teleportation.
	 * 
	 * @return the previousLocation
	 */
	public Location getPreviousLocation() {
		return this.previousLocation;
	}

	/**
	 * The String representation of the Player's Location prior to last sleep
	 * teleport.
	 * 
	 * @return String
	 */
	public String getPreviousLocationString() {
		return this.previousLocation.getWorld().getName() + ","
				+ this.previousLocation.getBlockX() + ","
				+ this.previousLocation.getBlockY() + ","
				+ this.previousLocation.getBlockZ();
	}

	/**
	 * Sets the Player's total time ingame from a String. For use in
	 * DatabaseManager only.
	 * 
	 * @param s String
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
	 * The String representation of the Player's total time ingame.
	 * 
	 * @return the Player's time ingame
	 */
	public String getTimePlayed() {
		long current = this.timePlayed + new Date().getTime() - this.login.getTime();
		long hrs = TimeUnit.MILLISECONDS.toHours(current);
		long mins = TimeUnit.MILLISECONDS.toMinutes(current) -
				TimeUnit.HOURS.toMinutes(hrs);
		long secs = TimeUnit.MILLISECONDS.toSeconds(current) -
				TimeUnit.HOURS.toSeconds(hrs) - TimeUnit.MINUTES.toSeconds(mins);
		long millis = current - TimeUnit.HOURS.toMillis(hrs) -
				TimeUnit.MINUTES.toMillis(mins) - TimeUnit.SECONDS.toMillis(secs);
		return String.format("%02d:%02d:%02d:%03d", hrs, mins, secs, millis);
	}

	/**
	 * future feature
	 * 
	 * @return isGodTier
	 */
	public boolean isGodTier() {
		return false;
	}

	/**
	 * Returns the Player's UHC mode.
	 * <p>
	 * UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing
	 * 
	 * @return the Player's UHC mode
	 */
	public byte getUHCMode() {
		return this.uhc;
	}

	/**
	 * Sets the Player's UHC mode.
	 * <p>
	 * UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing
	 * 
	 * @param b the UHC mode to set
	 */
	public void setUHCMode(Byte b) {
		// DB returns 0 if null
		if (b != 0) {
			this.uhc = b;
		}
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
	 * Add an Entry to the Set of programs accessible by the Player at their
	 * Computer.
	 * 
	 * @param i the number of the program to add
	 */
	public void addProgram(int i) {
		this.programs.add(i);
	}

	/**
	 * Restore the Player's installed programs from a String. For use in
	 * DatabaseManager only.
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
			sb.append(i).append('\u002C');
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
	 * Sets the SblockUser's IP if the Player is online.
	 */
	public void setUserIP() {
		if (this.getPlayer().isOnline())
			this.userIP = this.getPlayer().getAddress().getAddress()
					.getHostAddress();
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
	 * Gets a SblockUser by Player name.
	 * 
	 * @param userName the name to match
	 * 
	 * @return the SblockUser specified or null if invalid.
	 */
	public static SblockUser getUser(String userName) {
		return UserManager.getUserManager().getUser(userName);
	}

	/**
	 * Check if the user is in server mode.
	 * 
	 * @return true if the user is in server mode
	 */
	public boolean isServer() {
		return this.server;
	}

	/**
	 * Set the user's server mode.
	 * 
	 * @param b the boolean to set server mode to
	 */
	public boolean setServer(boolean b) {
		if (!this.hasServer) {
			this.server = b;
			// if b
			// get client, setHasServer true
			// if !b
			// tp to original location
			// remove fly
			// remove invisibility effects (teams + invis. Team = server's name?)
			//   team color can be nothing - will (probably) be entirely heroes.
			return true;
		}
		return false;
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
			this.passiveEffects.remove(effect);
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
			this.passiveEffects.put(effect, 0);	
		}
		else {
			this.passiveEffects.put(effect, this.passiveEffects.get(effect) + 1);
		}
	}
	
	/**
	 * Set the user's current Passive Effects. Will overlay existing map.
	 * 
	 * @param effect the PassiveEffect to remove
	 */
	public void removePassiveEffect(PassiveEffect effect) {
		if (this.passiveEffects.containsKey(effect)) {
			if (this.passiveEffects.get(effect) > 0) {
				this.passiveEffects.put(effect, this.passiveEffects.get(effect) - 1);
			}
			else {
				this.passiveEffects.remove(effect);
				PassiveEffect.removeEffect(getPlayer(), effect);
			}
		}
	}
	
}
