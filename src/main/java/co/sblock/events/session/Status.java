package co.sblock.events.session;

import net.md_5.bungee.api.ChatColor;

/**
 * Enum representing the status of Minecraft's servers.
 * 
 * @author Jikoo
 */
public enum Status {
	LOGIN("Minecraft's login servers aren't responding. Don't close your client!",
			"Minecraft's login servers are back up, closing Minecraft is ok!", null),
	SESSION("Minecraft's session servers aren't responding, don't disconnect!",
			"Minecraft's session servers are back up, multiplayer will work!",
			"Minecraft's session servers aren't responding! You might not be able to log in."),
	BOTH("Minecraft's session and login servers aren't responding, don't disconnect!",
			"Minecraft's session and login servers are back up! Continue play as usual.",
			"Minecraft's session servers aren't responding! You might not be able to log in."),
	NEITHER(null, null, null);

	/* The messages to broadcast under certain conditions */
	private final String announcement, allClear, motd;

	/**
	 * Constructor for Status.
	 * 
	 * @param announcement the message to broadcast when Status is set
	 * @param allclear the message to broadcast when Status is changed
	 * @param MOTD the MOTD to set in the server list
	 */
	private Status(String announcement, String allclear, String MOTD) {
		this.announcement = announcement;
		this.allClear = allclear;
		this.motd = MOTD;
	}

	/**
	 * Check if the Status has a message to broadcast initially.
	 * 
	 * @return true if the message is not null
	 */
	public boolean hasAnnouncement() {
		return announcement != null;
	}

	/** 
	 * Gets the message to broadcast initially.
	 * 
	 * @return String
	 */
	public String getAnnouncement() {
		return announcement;
	}

	/**
	 * Check if the Status has a message to broadcast when over.
	 * 
	 * @return true if the message is not null
	 */
	public boolean hasAllClear() {
		return allClear != null;
	}

	/** 
	 * Gets the message to broadcast when over.
	 * 
	 * @return String
	 */
	public String getAllClear() {
		return allClear;
	}

	/**
	 * Check if the Status has a server list MOTD.
	 * 
	 * @return true if the message is not null
	 */
	public boolean hasMOTDChange() {
		return motd != null;
	}

	/** 
	 * Gets the message to set as server list MOTD.
	 * 
	 * @return String
	 */
	public String getMOTDChange() {
		return ChatColor.RED + motd;
	}
}
