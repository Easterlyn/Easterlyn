package co.sblock.Sblock.Chat;

import org.bukkit.ChatColor;

/**
 * Class for defining rank and region colors more easily.
 * 
 * @author Dublek, Jikoo
 */

public class ColorDef {
	
	public static final ChatColor DEFAULT = ChatColor.WHITE;
	
	public static final ChatColor RANK_ADMIN = ChatColor.DARK_RED;
	public static final ChatColor RANK_MOD = ChatColor.BLUE;
	public static final ChatColor RANK_FELT = ChatColor.GREEN;
	public static final ChatColor RANK_HELPER = ChatColor.DARK_GRAY;
	public static final ChatColor RANK_GODTIER = ChatColor.GOLD; 
	public static final ChatColor RANK_DONATOR = ChatColor.DARK_GREEN; 
	public static final ChatColor RANK_HERO = ChatColor.WHITE;
	
	public static final ChatColor CHATRANK_OWNER = ChatColor.RED;
	public static final ChatColor CHATRANK_MOD = ChatColor.AQUA;
	public static final ChatColor CHATRANK_MEMBER = ChatColor.GOLD;
	
	public static final ChatColor WORLD_EARTH = ChatColor.DARK_GREEN; 
	public static final ChatColor WORLD_INNERCIRCLE = ChatColor.YELLOW; 
	public static final ChatColor WORLD_OUTERCIRCLE = ChatColor.DARK_PURPLE;
	public static final ChatColor WORLD_MEDIUM = ChatColor.GRAY; 
	public static final ChatColor WORLD_FURTHESTRING = ChatColor.BLACK;
	
	public static final ChatColor RAINBOW[] = {
		ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD, ChatColor.YELLOW,
		ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.AQUA, ChatColor.DARK_AQUA,
		ChatColor.BLUE, ChatColor.DARK_BLUE, ChatColor.LIGHT_PURPLE, ChatColor.DARK_PURPLE};

	public static String listColors() {
		StringBuilder sb = new StringBuilder();
		for (ChatColor c : ChatColor.values()) {
			sb.append(c).append('\u0026').append(c.getChar()).append('\u0020');
			sb.append(c.name().toLowerCase()).append(ChatColor.RESET).append('\u0020');
		}
		return sb.toString();
	}
}
