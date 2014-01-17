package co.sblock.Sblock.Chat.Channel;

public enum AccessLevel {
	PUBLIC, PRIVATE;
	
	public static AccessLevel getAccess(String access) {
		access = access.toUpperCase();
		try {
			return AccessLevel.valueOf(access);
		} catch (IllegalStateException | IllegalArgumentException e) {
			return null;
		}
	}
}
