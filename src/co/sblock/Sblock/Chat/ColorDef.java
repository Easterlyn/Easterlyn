package co.sblock.Sblock.Chat;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ColorDef {
	
	public static final ChatColor DEFAULT = ChatColor.WHITE;
	
	public static final ChatColor RANK_ADMIN = ChatColor.DARK_RED;
	public static final ChatColor RANK_MOD = ChatColor.BLUE;
	public static final ChatColor RANK_HELPER = ChatColor.DARK_GRAY;
	public static final ChatColor RANK_GODTIER = ChatColor.GOLD; 
	public static final ChatColor RANK_DONATOR = ChatColor.GREEN; 
	public static final ChatColor RANK_HERO = ChatColor.WHITE;
	
	public static final ChatColor CHATRANK_OWNER = ChatColor.RED;
	public static final ChatColor CHATRANK_MOD = ChatColor.AQUA;
	public static final ChatColor CHATRANK_MEMBER = ChatColor.GOLD;
	
	public static final ChatColor WORLD_EARTH = ChatColor.DARK_GREEN; 
	public static final ChatColor WORLD_INNERCIRCLE = ChatColor.YELLOW; 
	public static final ChatColor WORLD_OUTERCIRCLE = ChatColor.DARK_PURPLE;
	public static final ChatColor WORLD_MEDIUM = ChatColor.GRAY; 
	public static final ChatColor WORLD_FURTHESTRING = ChatColor.BLACK;
	
    private static final String RAINBOW[] = { "DARK_RED", "RED", "GOLD", "YELLOW", "GREEN", "DARK_GREEN", "AQUA", "DARK_AQUA", "BLUE", "DARK_BLUE", "LIGHT_PURPLE", "DARK_PURPLE" };

    //TODO This is the /color list command from ColorMe. Goal is to refactor it so it works here.
/*    
    public void listColors(CommandSender sender) {
    	plugin.logDebug("Actions -> listColors");
    	String message = plugin.localization.getString("color_list");
    	plugin.message(sender, null, message, null, null, null, null);
    	StringBuffer buf = new StringBuffer();
    	// As long as all colors aren't reached, including magic manual
    	for (ChatColor value : ChatColor.values()) {
    	    // get the name from the integer
    	    String color = value.name().toLowerCase();
    	    String colorChar = Character.toString(value.getChar());
    	    if (colorChar.equalsIgnoreCase("r")
    		    || colorChar.equalsIgnoreCase("n")
    		    || colorChar.equalsIgnoreCase("m") 
    		    || colorChar.equalsIgnoreCase("k")) {
    		continue;
    	    }
    	    // color the name of the color
    	    if (plugin.config.getBoolean("colors." + color)) {
    		buf.append(ChatColor.valueOf(color.toUpperCase()) + color + " (\u0026" + colorChar + ") " + ChatColor.WHITE);
    	    }
    	}
    	if (plugin.config.getBoolean("colors.strikethrough")) {
    	    buf.append(ChatColor.STRIKETHROUGH + "striketrough" + ChatColor.WHITE + " (\u0026m) ");
    	}
    	if (plugin.config.getBoolean("colors.underline")) {
    	    buf.append(ChatColor.UNDERLINE + "underline" + ChatColor.WHITE + " (\u0026n) ");
    	}
    	if (plugin.config.getBoolean("colors.magic")) {
    	    buf.append("magic (" + ChatColor.MAGIC + "a" + ChatColor.WHITE + ", \u0026k) ");
    	}
    	// Include custom colors
    	if (plugin.config.getBoolean("colors.random")) {
    	    buf.append(randomColor("random (\u0026random)" + " "));
    	}
    	if (plugin.config.getBoolean("colors.rainbow")) {
    	    buf.append(rainbowColor("rainbow (\u0026rainbow)") + " ");
    	}
    	if (plugin.config.getBoolean("colors.custom")) {
    	    buf.append(ChatColor.RESET);
    	    for (String color : plugin.colors.getKeys(false)) {
    		buf.append(color + " ");
    	    }
    	}
    	if (plugin.config.getBoolean("colors.mixed")) {
    	    buf.append(ChatColor.RESET + "" + ChatColor.DARK_RED + "\nMixed colors are enabled. Example: blue-bold");
    	}
    	sender.sendMessage(buf.toString());
        }
*/
}
