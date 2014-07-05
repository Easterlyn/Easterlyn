package co.sblock.users;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import co.sblock.chat.ColorDef;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author Jikoo, Dublek
 */
public enum Region {
	EARTH("earth", ColorDef.WORLD_EARTH, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	OUTERCIRCLE("outercircle", ColorDef.WORLD_OUTERCIRCLE, "http://sblock.co/rpack/Derse.zip"),
	INNERCIRCLE("innercircle", ColorDef.WORLD_INNERCIRCLE, "http://sblock.co/rpack/Prospit.zip"),
	FURTHESTRING("furthestring", ColorDef.WORLD_FURTHESTRING, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	MEDIUM("medium", ColorDef.WORLD_MEDIUM, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	LOWAS("lowas", ColorDef.WORLD_MEDIUM, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	LOLAR("lolar", ColorDef.WORLD_MEDIUM, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	LOHAC("lohac", ColorDef.WORLD_MEDIUM, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	LOFAF("lofaf", ColorDef.WORLD_MEDIUM, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip"),
	UNKNOWN("unknown", ColorDef.WORLD_AETHER, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip");

	/* INNER FIELDS */
	private final String name;
	private final String resourcePack;
	private final ChatColor worldChatColor;

	/**
	 * 
	 * @param name The name of the region
	 * @param color the default chat color of the region
	 * @param sourceURL the texture pack to be used in this region
	 */
	private Region(String name, ChatColor color, String sourceURL) {
		this.name = name;
		this.resourcePack = sourceURL;
		this.worldChatColor = color;
	}


	/**
	 * Gets the name of the Region.
	 * 
	 * @return Region.name() in lower case
	 */
	public String getRegionName() {
		return name;
	}

	/**
	 * @return the url of the resource pack to be used
	 */
	public String getResourcePackURL() {
		return this.resourcePack;
	}

	/**
	 * Gets the ChatColor that indicates users are in a specific Region.
	 * 
	 * @param r the Region to get the ChatColor of
	 * 
	 * @return the relevant ChatColor
	 */
	public ChatColor getRegionColor() {
		return this.worldChatColor;
	}

	/**
	 * Case-insensitive alternative to valueOf.
	 * 
	 * @param s the String to match
	 * 
	 * @return the Region that matches, Region.Earth if invalid.
	 */
	public static Region uValueOf(String s) {
		s = s.toUpperCase();
		try {
			return (Region.valueOf(s));
		} catch (IllegalArgumentException e) {
			if (s.contains("MEDIUM")) {
				return Region.MEDIUM;
			}
			return Region.EARTH;
		}
	}

	/**
	 * Gets the Region that a Location is within.
	 * 
	 * @param l the Location to get the Region of
	 * 
	 * @return the relevant Region
	 */
	public static Region getLocationRegion(Location l) {
		try {
			Region r = Region.uValueOf(l.getWorld().getName());
			if (r.equals(Region.MEDIUM)) {
				boolean plusX = (l.getBlockX() >= 0);
				boolean plusZ = (l.getBlockZ() >= 0);
				if (plusX && plusZ)
					r = Region.LOWAS;
				else if (!plusX && plusZ)
					r = Region.LOLAR;
				else if (!plusX && !plusZ)
					r = Region.LOHAC;
				else if (plusX && !plusZ)
					r = Region.LOFAF;
			}
			return r;
		} catch (IllegalStateException e) {
			// Player is in an invalid world
			// For the sake of region channels, default to earth.
			return Region.EARTH;
		}
	}
}
