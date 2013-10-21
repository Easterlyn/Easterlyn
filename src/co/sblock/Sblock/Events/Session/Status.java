package co.sblock.Sblock.Events.Session;

/**
 * The status of Minecraft's servers.
 * 
 * @author Jikoo
 */
public enum Status {
	LOGIN("Login servers are down, don't close your client!",
			"Login servers are back up, closing Minecraft is ok!", null),
	SESSION("Session servers are down, don't disconnect!",
			"Session servers are back up, multiplayer will work!",
			"MC session servers are down! Can't log in."),
	BOTH("Session and login servers are down, don't disconnect!",
			"Session and login servers are back up! Continue play as usual.",
			"MC session servers are down! Can't log in."),
	NEITHER(null, null, null);

	/** The message to broadcast initially. */
	private String announcement;
	/** The message to broadcast when over. */
	private String allClear;
	/** The server list MOTD to set. */
	private String MOTD;
	private Status(String announcement, String allclear, String MOTD) {
		this.announcement = announcement;
		this.allClear = allclear;
		this.MOTD = MOTD;
	}

	/**
	 * Check if the <code>Status</code> has a message to broadcast initially.
	 * 
	 * @return true if the message is not <code>null</code>
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
	 * Check if the <code>Status</code> has a message to broadcast when over.
	 * 
	 * @return true if the message is not <code>null</code>
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
	 * Check if the <code>Status</code> has a server list MOTD.
	 * 
	 * @return true if the message is not <code>null</code>
	 */
	public boolean hasMOTDChange() {
		return MOTD != null;
	}

	/** 
	 * Gets the message to set as server list MOTD.
	 * 
	 * @return String
	 */
	public String getMOTDChange() {
		return MOTD;
	}
}
