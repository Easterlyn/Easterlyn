package co.sblock.chat.channel;

public enum ChannelType {

	NORMAL,
	REGION,
	RP,
	NICK;

	public static ChannelType getType(String s) {
		s = s.replaceAll("\\W", "").toUpperCase();
		s = s.replace("NICKNAME", "NICK");
		s = s.replace("REGION", "NOPE, PLAYERS CAN'T MAKE THESE.");
		try {
			return ChannelType.valueOf(s);
		} catch (IllegalStateException | IllegalArgumentException e) {
			return null;
		}
	}
}
