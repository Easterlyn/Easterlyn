package co.sblock.users;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;

import co.sblock.Sblock;

import net.md_5.bungee.api.ChatColor;

/**
 * Class that keeps track of players currently logged on to the game
 * 
 * @author Jikoo, Dublek
 */
public enum Region {
	EARTH("Earth", "Earth", ChatColor.DARK_GREEN, false, false),
	DERSE("Derse", "Derspit", ChatColor.DARK_PURPLE, false, true),
	PROSPIT("Prospit", "Derspit", ChatColor.YELLOW, false, true),
	FURTHESTRING("FurthestRing", "FurthestRing", ChatColor.BLACK, false, false),
	LOFAF("LOFAF", "LOFAF", ChatColor.WHITE, true, false),
	LOHAC("LOHAC", "LOHAC", ChatColor.RED, true, false),
	LOLAR("LOLAR", "LOLAR", ChatColor.AQUA, true, false),
	LOWAS("LOWAS", "LOWAS", ChatColor.GRAY, true, false),
	DUNGEON("Dungeon", "Dungeon", ChatColor.DARK_GREEN, false, false),
	DEFAULT("Second Earth", "Earth", ChatColor.DARK_GREEN, false, false);

	/* INNER FIELDS */
	private final String displayName;
	private final String worldName;
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
	private Region(String displayName, String worldName, ChatColor color, boolean isMedium, boolean isDream) {
		this.displayName = displayName;
		this.worldName = worldName;
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
	 * Sets a Player's resource pack to this Region's resource pack.
	 */
	public void setResourcePack(Sblock sblock, Player player) {
		if (this == DEFAULT) {
			return;
		}

		PacketContainer packet = new PacketContainer(PacketType.Play.Server.RESOURCE_PACK_SEND);
		packet.getStrings().write(0, "http://sblock.co/rpack/" + this.displayName + ".zip");
		packet.getStrings().write(1, sblock.getResourceHashes().getString(this.displayName, "null"));

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Gets the ChatColor that indicates users are in a specific Region.
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
	 * @param name the String to match
	 * 
	 * @return the Region that matches, Region.UNKNOWN if invalid.
	 */
	public static Region getRegion(String name) {
		name = name.toUpperCase().replace("_NETHER", "").replace("_THE_END", "");
		try {
			return (Region.valueOf(name));
		} catch (IllegalArgumentException | IllegalStateException e) {
			// Compatibility for old dream planet saving
			if (name.equals("INNERCIRCLE")) {
				return PROSPIT;
			}
			if (name.equals("OUTERCIRCLE")) {
				return DERSE;
			}
			if (name.equals("DERSPIT")) {
				return ThreadLocalRandom.current().nextInt() >= .5 ? DERSE : PROSPIT;
			}
			for (Region region : values()) {
				if (region.getDisplayName().toUpperCase().equals(name)) {
					return region;
				}
			}
			return DEFAULT;
		}
	}
}
