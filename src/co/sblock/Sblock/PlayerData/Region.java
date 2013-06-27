package co.sblock.Sblock.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.Location;

public enum Region {
	EARTH, INNERCIRCLE, OUTERCIRCLE, FURTHESTRING, MEDIUM, LOWAS, LOLAR, LOHAC, LOFAF;

	public String getRegionName() {
		return this.name().toLowerCase();
	}

	public static Region uValueOf(String s) {
		return (Region.valueOf(s.toUpperCase()));
	}

	public static ChatColor getRegionColor(Region r) {
		switch (r) {
		case EARTH:
			return ChatColor.DARK_GREEN;
		case FURTHESTRING:
			return ChatColor.DARK_GRAY;
		case INNERCIRCLE:
			return ChatColor.GOLD;
		case LOFAF:
		case LOHAC:
		case LOLAR:
		case LOWAS:
		case MEDIUM:
			// TODO need colors for these suckers :D
			return ChatColor.DARK_BLUE;
		case OUTERCIRCLE:
			return ChatColor.DARK_PURPLE;
		default:
			return ChatColor.RESET;
		}
	}

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
			return Region.EARTH;
		}
	}
}
