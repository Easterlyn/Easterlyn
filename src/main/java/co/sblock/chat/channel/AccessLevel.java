package co.sblock.chat.channel;

/**
 * The access level of a channel (determines who can join without an invitation)
 * @author Dublek
 */
public enum AccessLevel {
	PUBLIC, PRIVATE;

	/**
	 * @param level the access level
	 * @return the access level
	 */
	public static AccessLevel getAccessLevel(String level) {
		try {
			return AccessLevel.valueOf(level.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}
}
