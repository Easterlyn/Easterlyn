package co.sblock.Sblock.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import co.sblock.Sblock.DatabaseManager;

/**
 * <code>SblockPlayer</code> is the class for storing all <code>Player</code>
 * data.
 * 
 * @author Jikoo
 * @author Dublek
 * @author FireNG
 */
public class SblockPlayer {

	/** The <code>Player</code> */
	private String playerName;

	/** The <code>Player</code>'s chosen class */
	private PlayerClass classType = PlayerClass.UNKNOWN;

	/** The <code>Player</code>'s chosen aspect */
	private PlayerAspect aspect = PlayerAspect.UNKNOWN;

	/** The <code>Player</code>'s chosen Medium planet. */
	private MediumPlanet mPlanet = MediumPlanet.UNKNOWN;

	/** The <code>Player</code>'s chosen dream planet. */
	private DreamPlanet dPlanet = DreamPlanet.UNKNOWN;

	/** The <code>Player</code>'s tower number */
	private short tower = -1;

	/** <code>true</code> if the player is in dreamstate */
	private boolean sleeping = false;

	private Location previousLocation;

	/**
	 * Creates a SblockPlayer object for a player.
	 * 
	 * @param p
	 *            the <code>Player</code> to load data for
	 */
	public SblockPlayer(String playerName) {
		this.playerName = playerName;
		DatabaseManager.getDatabaseManager().loadPlayerData(this);
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
	public PlayerClass getClassType() {
		return this.classType;
	}

	/**
	 * Gets the <code>Player</code>'s chosen aspect
	 * 
	 * @return the aspect, <code>null</code> if unchosen
	 */
	public PlayerAspect getAspect() {
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
	 * Sets the class type.
	 * 
	 * @param pclass
	 *            the new class type
	 */
	public void setPlayerClass(String pclass) {
		this.classType = PlayerClass.getClass(pclass);
	}

	/**
	 * Sets the aspect.
	 * 
	 * @param aspect
	 *            the new aspect
	 */
	public void setAspect(String aspect) {
		this.aspect = PlayerAspect.getAspect(aspect);
	}

	/**
	 * Sets the Medium planet.
	 * 
	 * @param mPlanet
	 *            the new Medium planet
	 */
	public void setMediumPlanet(String mPlanet) {
		this.mPlanet = MediumPlanet.getPlanet(mPlanet);
	}

	/**
	 * Sets the dream planet.
	 * 
	 * @param dPlanet
	 *            the new dream planet
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
}
