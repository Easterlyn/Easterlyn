package co.sblock.Sblock.Chat2.Channel;

public enum ChannelType {

	NORMAL, REGION, RP, NICK, TEMP;
	
	public static ChannelType getType(String s) {
		s = s.replaceAll("\\W", "").toUpperCase();
		s = s.replace("NICKNAME", "NICK");
		s = s.replace("REGION", "NOPE, PLAYERS CAN'T MAKE THESE.");
		try {
			return ChannelType.valueOf(s);
		} catch (IllegalStateException e) {
			return null;
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}
