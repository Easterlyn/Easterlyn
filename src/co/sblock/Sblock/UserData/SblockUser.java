package co.sblock.Sblock.UserData;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
	private Location previousLocation = Bukkit.getWorld("Earth").getSpawnLocation();

	/** The total time the <code>Player</code> has spent logged in */
	private long timePlayed = 0L;

	/** The <code>Player</code>'s last login */
	private Date login = new Date();

	/** The <code>Player</code>'s IP address */
	private String userIP;

	/** Programs installed to the player's computer */
	private Set<Integer> programs = new HashSet<Integer>();

	/** UHC modes: negative = off; 1 = standard UHC; 2 = pre-1.8b food healing */
	private byte uhc = 1;

	// javadoc
	private HashMap<String, Long> cooldowns = new HashMap<String, Long>();
	/**
	 * Creates a <code>SblockUser</code> object for a <code>Player</code>.
	 * 
	 * @param playerName
	 *            the name of the <code>Player</code> to create a <code>SblockUser</code> for
	 */
	public SblockUser(String playerName) {
		this.playerName = playerName;
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

	// javadoc
	public void addCooldown(String name, long lengthMillis) {
		cooldowns.put(name, new Date().getTime() + lengthMillis);
	}

	// javadoc
	public void clearCooldown(String name) {
		cooldowns.remove(name);
	}

	// javadoc
	public long getRemainingMilliseconds(String cooldownName) {
		if (cooldowns.containsKey(cooldownName)) {
			long now = new Date().getTime();
			if (cooldowns.get(cooldownName) - now > 0) {
				return cooldowns.get(cooldownName) - now;
			} else {
				return cooldowns.remove(cooldownName) - now;
			}
		}
		return 0;
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
}
