package co.sblock.Sblock.UserData;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

/**
 * Class for holding commands associated with this module.
 * 
 * For more information on how the command system works, please see
 * {@link co.sblock.Sblock.SblockCommand}
 * 
 * @author FireNG
 * 
 */
public class UserDataCommands implements CommandListener {

	public static final ChatColor PROFILE_COLOR = ChatColor.DARK_AQUA;

	@SblockCommand(consoleFriendly = true)
	public boolean profile(CommandSender sender, String playerToLookup) {
		SblockUser user = UserManager.getUserManager().getUser(
				playerToLookup);
		if (user == null)
			sender.sendMessage(ChatColor.YELLOW + "User not found.");
		else {
			String message = 
					PROFILE_COLOR + "-----------------------------------------\n"
			   + ChatColor.YELLOW + playerToLookup + ": " + user.getClassType().getDisplayName() + " of " + user.getAspect().getDisplayName() + "\n"
			   + PROFILE_COLOR    + "-----------------------------------------\n"
								  + "Dream planet: " + ChatColor.YELLOW + user.getDPlanet().getDisplayName() + "\n"
			   + PROFILE_COLOR    + "Medium planet: " + ChatColor.YELLOW + user.getDPlanet().getDisplayName() + "\n"
			   + PROFILE_COLOR    + "Echeladder rank: " + ChatColor.YELLOW + "Coming soon!";
			sender.sendMessage(message);
		}
		return true;
	}
	
	@SblockCommand(consoleFriendly = true)
	public boolean setplayer(CommandSender sender, String playerToModify, String type, String value)
	{
		SblockUser user = UserManager.getUserManager().getUser(playerToModify);
		if(type.equalsIgnoreCase("class"))
			user.setPlayerClass(value);
		else if(type.equalsIgnoreCase("aspect"))
			user.setAspect(value);
		else if(type.equalsIgnoreCase("land"))
			user.setMediumPlanet(value);
		else if(type.equalsIgnoreCase("dream"))
			user.setDreamPlanet(value);
		else
			return false;
		return true;
	}
	
	@SblockCommand(consoleFriendly = true)
	public boolean newplayer(CommandSender sender, String playerClass, String aspect, String medPlanet, String dreamPlanet)
	{
		return false; //TODO Implement
	}
}
