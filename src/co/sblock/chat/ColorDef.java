package co.sblock.chat;

import net.md_5.bungee.api.ChatColor;

/**
 * Class for defining rank and region colors more easily.
 * 
 * @author Dublek, Jikoo
 */

public class ColorDef {

	public static final ChatColor DEFAULT = ChatColor.WHITE;

	public static final ChatColor RANK_HORRORTERROR = ChatColor.DARK_RED;
	public static final ChatColor RANK_DENIZEN = ChatColor.BLUE;
	public static final ChatColor RANK_FELT = ChatColor.GREEN;
	public static final ChatColor RANK_HELPER = ChatColor.DARK_GRAY;
	public static final ChatColor RANK_GODTIER = ChatColor.GOLD;
	public static final ChatColor RANK_DONATOR = ChatColor.DARK_GREEN;
	public static final ChatColor RANK_HERO = ChatColor.WHITE;

	public static final ChatColor CHANNEL_OWNER = ChatColor.RED;
	public static final ChatColor CHANNEL_MOD = ChatColor.AQUA;
	public static final ChatColor CHANNEL_MEMBER = ChatColor.GOLD;

	public static final ChatColor RAINBOW[] = {
		ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
		ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.AQUA, ChatColor.DARK_AQUA,
		ChatColor.BLUE, ChatColor.DARK_BLUE, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE};

	public static final String HAL = ChatColor.WHITE + "[" + ChatColor.RED + "#" + ChatColor.WHITE
			+ "] <" + ChatColor.DARK_RED + "Lil Hal" + ChatColor.WHITE + "> " + ChatColor.RED;

	public static String listColors() {
		StringBuilder sb = new StringBuilder();
		for (ChatColor c : ChatColor.values()) {
			sb.append(c).append('&').append(c.toString().substring(1)).append(' ');
			sb.append(c.name().toLowerCase()).append(ChatColor.RESET).append(' ');
		}
		return sb.toString();
	}
}
