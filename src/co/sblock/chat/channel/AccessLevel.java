package co.sblock.chat.channel;

/**
 *  the access level of a channel (determines who can join without an invitation)
 * @author Dublek, tmathmeyer
 */
public enum AccessLevel {
	PUBLIC, PRIVATE;

	/**
	 * TODO: make it throw a custom exception for better error handling
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
