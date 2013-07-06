package co.sblock.Sblock.Chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.Sblock.Chat.Channel.Channel;

public class User {
	
	private Player pthis;
	private Channel current;
	private boolean isMute;
	private boolean isOnline;
	
	private Set<Channel> listening = new HashSet<Channel>();	//TODO: figure out how the hell to save this between runs
		//also, do I even want/need this?
	
	private static Map<String, User> userList = new HashMap<String, User>(); //TODO: same for this. halp. plz
		
	public User(Player p)	{
		this.pthis = p;
		//this.current = somehow based on current region
		this.isMute = false;
		userList.put(pthis.getName(), this);
		//Channel?.joinChannelFirstTime(Region);
	}
	
	public static void addPlayer (Player p)	{ //Used for first-time logins
		new User(p);
	}
	public static void login(Player p)	{
		userList.get(p.getName()).isOnline = true;
	}
	public static void logout (Player p)	{
		userList.get(p.getName()).isOnline = false;
	}
	public static User getUser (String name)	{
		return userList.get(name);
	}
	public String getName()	{
		return this.pthis.getName();
	}
	
	
	public void chat (AsyncPlayerChatEvent event)	{	//receives message from SblockChatListener
		
	}
	public void sendMessageFromChannel (String s, Channel c)	{	//final output, sends message to user
		//alert for if its player's name is applied here i.e. {!}
		
	}
	
	//Here begins output formatting. Abandon all hope ye who enter
	
	public String getOutputChannelF()	{	//colors for [$channel] applied here
		/* SburbChat code. Handle with care
		
		ChatColor color = ChatColor.GOLD;
		if (sender.getName().equals(this.owner))
		{
			color = ChatColor.AQUA;
		}
		else if (this.modList.contains(sender.getName()))
		{
			color = ChatColor.RED;
		}

		return ChatColor.WHITE + "[" + color + this.name + ChatColor.WHITE + "] ";
		*/
		return null;
	}
	
	public String getOutputNameF()	{	//colors for <$name> applied here
		/*	SburbChat code. Handle with care
		
		ChatColor colorP = ColorDef.RANK_HERO;
		ChatColor colorW = ColorDef.DEFAULT;

		if (this.hasPermission("group.horrorterror"))
			colorP = ColorDef.RANK_ADMIN;
		else if (this.hasPermission("group.denizen"))
			colorP = ColorDef.RANK_MOD;
		else if (this.hasPermission("group.godtier"))
			colorP = ColorDef.RANK_GODTIER;
		else if (this.hasPermission("group.donator"))
			colorP = ColorDef.RANK_DONATOR;
		
		if (pthis.getWorld().getName().equalsIgnoreCase("earth"))
			colorW = ColorDef.WORLD_EARTH;
		else if (pthis.getWorld().getName().equalsIgnoreCase("innercircle"))
			colorW = ColorDef.WORLD_INNERCIRCLE;
		else if (pthis.getWorld().getName().equalsIgnoreCase("outercircle"))
			colorW = ColorDef.WORLD_OUTERCIRCLE;
		else if (pthis.getWorld().getName().equalsIgnoreCase("medium"))
			colorW = ColorDef.WORLD_MEDIUM;
		else if (pthis.getWorld().getName().equalsIgnoreCase("furthestring"))
			colorW = ColorDef.WORLD_FURTHESTRING;
		
		return (isThirdPerson ? "* " : colorW + "<") + colorP + pthis.getName() + ChatColor.WHITE + (isThirdPerson ? "" : colorW + "> " + ChatColor.WHITE);

		 */
		return null;		
	}

	public void sendMessage(String string) {
		// TODO Auto-generated method stub
		
	}
}
