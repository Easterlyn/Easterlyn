package co.sblock.Sblock.PlayerData;

import org.bukkit.entity.Player;

/**
 * <code>SblockPlayer</code> is the class for storing all <code>Player</code>
 * data.
 * 
 * @author Jikoo
 * @author Dublek
 */
public class SblockPlayer {

	/** The <code>Player</code> */
	private Player player;

	/** The <code>Player</code>'s chosen class */
	private String classType;

	/** The <code>Player</code>'s chosen aspect */
	private String aspect;

	/** The <code>Player</code>'s chosen Medium planet. */
	private String mPlanet;

	/** The <code>Player</code>'s chosen dream planet. */
	private String dPlanet;

	/** The <code>Player</code>'s tower number */
	private short tower;

	/** <code>true</code> if the player is in dreamstate */
	private boolean sleeping;

	/**
	 * Instantiates a new SblockPlayer.
	 * 
	 * @param p
	 *            the <code>Player</code> to load data for
	 */
	public SblockPlayer(Player p) {
		this.player = p;
		/*
		 * pg will be our PostgreSQL function collection
		 * 
		 * String[] playerData = pg.loadPlayerData(p.getName()); //load
		 * classpect + planets this.setClassType(playerData[0]);
		 * this.setAspect(playerData[1]); this.setMPlanet(playerData[2]);
		 * this.setDPlanet(playerData[3]); try {
		 * this.setTower(Short.parseShort(playerData[4])); } catch
		 * (NumberFormatException e) { //This error shouldn't be able to happen
		 * but.. just in case! this.setTower(pg.loadPlayerTower(p.getName()); }
		 * this.setSleeping(pg.loadPlayerDreamstate(p.getName()));
		 */
	}

	/**
	 * Gets the <code>Player</code>.
	 * 
	 * @return the <code>Player</code>
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Gets the <code>Player</code>'s chosen class
	 * 
	 * @return the class type, <code>null</code> if unchosen
	 */
	public String getClassType() {
		return this.classType;
	}

	/**
	 * Gets the <code>Player</code>'s chosen aspect
	 * 
	 * @return the aspect, <code>null</code> if unchosen
	 */
	public String getAspect() {
		return this.aspect;
	}

	/**
	 * Gets the <code>Player</code>'s chosen Medium planet.
	 * 
	 * @return the <code>Player</code>'s Medium planet
	 */
	public String getMPlanet() {
		return this.mPlanet;
	}

	/**
	 * Gets the <code>Player</code>'s chosen dream planet.
	 * 
	 * @return the <code>Player</code>'s dream planet
	 */
	public String getDPlanet() {
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
	 * Sets the <code>Player</code>'s dreamstate
	 * 
	 * @return <code>true</code> if the <code>Player</code> is in dreamstate
	 */
	public boolean getSleeping() {
		return this.sleeping;
	}

	public Region getPlayerRegion() {
		return Region.getLocationRegion(player.getLocation());
	}

	/**
	 * Sets the class type.
	 * 
	 * @param className
	 *            the new class type
	 */
	public void setClassType(String className) {
		this.classType = className;
	}

	/**
	 * Sets the aspect.
	 * 
	 * @param aspect
	 *            the new aspect
	 */
	public void setAspect(String aspect) {
		this.aspect = aspect;
	}

	/**
	 * Sets the Medium planet.
	 * 
	 * @param mPlanet
	 *            the new Medium planet
	 */
	public void setMPlanet(String mPlanet) {
		this.mPlanet = mPlanet;
	}

	/**
	 * Sets the dream planet.
	 * 
	 * @param dPlanet
	 *            the new dream planet
	 */
	public void setDPlanet(String dPlanet) {
		this.dPlanet = dPlanet;
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
	public void setSleeping(boolean sleeping) {
		this.sleeping = sleeping;
	}
}
