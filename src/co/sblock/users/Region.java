package co.sblock.users;

import org.bukkit.ChatColor;

import co.sblock.chat.ColorDef;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author Jikoo, Dublek
 */
public enum Region {
	EARTH("Earth", "Earth", "#EARTH", ColorDef.WORLD_EARTH, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", false, false),
	DERSE("Derse", "Derspit", "#DERSPIT", ColorDef.WORLD_OUTERCIRCLE, "http://sblock.co/rpack/Derse.zip", false, true),
	PROSPIT("Prospit", "Derspit", "#DERSPIT", ColorDef.WORLD_INNERCIRCLE, "http://sblock.co/rpack/Prospit.zip", false, true),
	FURTHESTRING("FurthestRing", "FurthestRing", "#FURTHESTRING", ColorDef.WORLD_FURTHESTRING, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", false, false),
	LOFAF("LOFAF", "LOFAF", "#LOFAF", ColorDef.WORLD_LOFAF, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", true, false),
	LOHAC("LOHAC", "LOHAC", "#LOHAC", ColorDef.WORLD_LOHAC, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", true, false),
	LOLAR("LOLAR", "LOLAR", "#LOLAR", ColorDef.WORLD_LOLAR, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", true, false),
	LOWAS("LOWAS", "LOWAS", "#LOWAS", ColorDef.WORLD_LOWAS, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", true, false),
	DUNGEON("Dungeon", "Dungeon", "#EARTH", ColorDef.WORLD_EARTH,  "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", false, false),
	KRINGLEFEST("KringleFest", "Dungeon", "#EARTH", ColorDef.WORLD_AETHER,  "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", false, false),
	UNKNOWN("Second Earth", "Earth", "#Aether", ColorDef.WORLD_AETHER, "http://sblock.co/rpack/Sblock_Modified_Faithful_NoSound.zip", false, false);

	/* INNER FIELDS */
	private final String displayName;
	private final String worldName;
	private final String channelName;
	private final String resourcePack;
	private final ChatColor worldChatColor;
	private final boolean isMedium;
	private final boolean isDream;

	/**
	 * @param worldName The name of the world
	 * @param channelName The name of the region's channel
	 * @param color the default chat color of the region
	 * @param sourceURL the resource pack to be used in this region
	 * @param isMedium true if the planet is in the Medium
	 * @param isDream true if the planet is a dream planet
	 */
	private Region(String displayName, String worldName, String channelName, ChatColor color, String sourceURL, boolean isMedium, boolean isDream) {
		this.displayName = displayName;
		this.worldName = worldName;
		this.channelName = channelName;
		this.resourcePack = sourceURL;
		this.worldChatColor = color;
		this.isMedium = isMedium;
		this.isDream = isDream;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Gets the name of the World.
	 * 
	 * @return the World name
	 */
	public String getWorldName() {
		return this.worldName;
	}

	/**
	 * Gets the name of the Region's Channel.
	 * 
	 * @return Region.name() in lower case
	 */
	public String getChannelName() {
		return channelName;
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
	public ChatColor getColor() {
		return this.worldChatColor;
	}

	/**
	 * @return true if the planet is in the Medium
	 */
	public boolean isMedium() {
		return isMedium;
	}

	/**
	 * @return true if the planet is a dream planet
	 */
	public boolean isDream() {
		return isDream;
	}

	/**
	 * Case-insensitive alternative to valueOf.
	 * 
	 * @param s the String to match
	 * 
	 * @return the Region that matches, Region.UNKNOWN if invalid.
	 */
	public static Region getRegion(String s) {
		s = s.toUpperCase().replace("_NETHER", "").replace("_THE_END", "");
		try {
			return (Region.valueOf(s));
		} catch (IllegalArgumentException | IllegalStateException e) {
			// Compatibility for old dream planet saving
			if (s.equalsIgnoreCase("INNERCIRCLE")) {
				return PROSPIT;
			}
			if (s.equalsIgnoreCase("OUTERCIRCLE")) {
				return DERSE;
			}
			if (s.equalsIgnoreCase("Derspit")) {
				return Math.random() >= .5 ? DERSE : PROSPIT;
			}
			for (Region region : values()) {
				if (region.getDisplayName().toUpperCase().equals(s)) {
					return region;
				}
			}
			return UNKNOWN;
		}
	}
}
