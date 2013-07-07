package co.sblock.Sblock.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

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
    private PlayerClass classType;

    /** The <code>Player</code>'s chosen aspect */
    private PlayerAspect aspect;

    /** The <code>Player</code>'s chosen Medium planet. */
    private MediumPlanet mPlanet;

    /** The <code>Player</code>'s chosen dream planet. */
    private DreamPlanet dPlanet;

    /** The <code>Player</code>'s tower number */
    private int tower;

    /** <code>true</code> if the player is in dreamstate */
    private boolean sleeping;

    private Location previousLocation;

    /**
     * Creates a SblockPlayer object for a player new to the server (Or whose
     * database record couldn't be found), instantiating the player with default
     * values.
     * 
     * @param p
     *            the <code>Player</code> to load data for
     */
    SblockPlayer(String playerName) {
	this(playerName, PlayerClass.UNKNOWN, PlayerAspect.UNKNOWN,
		MediumPlanet.UNKNOWN, DreamPlanet.UNKNOWN, -1, false, null);
    }

    SblockPlayer(String playerName, PlayerClass pClass, PlayerAspect aspect,
	    MediumPlanet mPlanet, DreamPlanet dPlanet, int towerNum,
	    boolean isAsleep, Location prevLocation) {
	this.playerName = playerName;
	this.classType = pClass;
	this.aspect = aspect;
	this.mPlanet = mPlanet;
	this.dPlanet = dPlanet;
	this.tower = towerNum;
	this.sleeping = isAsleep;
	this.previousLocation = prevLocation;
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
    public int getTower() {
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
    public void setPlayerClass(PlayerClass pclass) {
	this.classType = pclass;
    }

    /**
     * Sets the aspect.
     * 
     * @param aspect
     *            the new aspect
     */
    public void setAspect(PlayerAspect aspect) {
	this.aspect = aspect;
    }

    /**
     * Sets the Medium planet.
     * 
     * @param mPlanet
     *            the new Medium planet
     */
    public void setMediumPlanet(MediumPlanet mPlanet) {
	this.mPlanet = mPlanet;
    }

    /**
     * Sets the dream planet.
     * 
     * @param dPlanet
     *            the new dream planet
     */
    public void setDreamPlanet(DreamPlanet dPlanet) {
	this.dPlanet = dPlanet;
    }

    /**
     * Sets the tower number generated for the <code>Player</code>
     * 
     * @param tower
     *            the number of the tower the player will "dream" to
     */
    public void setTower(int tower) {
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
