package co.sblock.Sblock.UserData;

import org.bukkit.ChatColor;
import org.bukkit.Location;

import co.sblock.Sblock.Chat.ColorDef;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author Jikoo, Dublek
 */
public enum Region {
	EARTH, INNERCIRCLE, OUTERCIRCLE, FURTHESTRING,
	MEDIUM, LOWAS, LOLAR, LOHAC, LOFAF, UNKNOWN;

	/**
	 * Gets the name of the <code>Region</code>.
	 * @return <code>Region.name()</code> in lower case
	 */
	public String getRegionName() {
		return this.name().toLowerCase();
	}

	/**
	 * Case-insensitive alternative to valueOf.
	 * 
	 * @param s
	 *            the <code>String</code> to match
	 * @return the <code>Region</code> that matches, <code>Region.Earth</code>
	 *         if invalid.
	 */
	public static Region uValueOf(String s) {
		try {
			return (Region.valueOf(s.toUpperCase()));
		} catch (IllegalArgumentException e) {
			return Region.EARTH;
		}
	}

	/**
	 * Gets the <code>ChatColor</code> that indicates users are in a specific
	 * <code>Region</code>.
	 * 
	 * @param r
	 *            the <code>Region</code> to get the <code>ChatColor</code> of
	 * @return the relevant <code>ChatColor</code>
	 */
	public static ChatColor getRegionColor(Region r) {
		switch (r) {
		case EARTH:
			return ColorDef.WORLD_EARTH;
		case FURTHESTRING:
			return ColorDef.WORLD_FURTHESTRING;
		case INNERCIRCLE:
			return ColorDef.WORLD_INNERCIRCLE;
		case LOFAF:
		case LOHAC:
		case LOLAR:
		case LOWAS:
		case MEDIUM:
			return ColorDef.WORLD_MEDIUM;
		case OUTERCIRCLE:
			return ColorDef.WORLD_OUTERCIRCLE;
		default:
			return ColorDef.DEFAULT;
		}
	}

	/**
	 * Gets the <code>Region</code> that a <code>Location</code> is within.
	 * 
	 * @param l
	 *            the <code>Location</code> to get the <code>Region</code> of
	 * @return the relevant <code>Region</code>
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
