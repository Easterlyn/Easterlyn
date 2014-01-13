package co.sblock.Sblock.UserData;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Events.EventModule;

/**
 * Class for holding commands associated with this module.
 * 
 * For more information on how the command system works, please see
 * {@link co.sblock.Sblock.SblockCommand}
 * 
 * @author FireNG
 */
public class UserDataCommands implements CommandListener {

	/** The standard profile color. */
	public static final ChatColor PROFILE_COLOR = ChatColor.DARK_AQUA;

	/**
	 * Gets the profile of a <code>SblockUser</code>.
	 * 
	 * @param sender
	 *            the <code>CommandSender</code>
	 * @param target
	 *            the <code>SblockUser</code> to look up
	 * @return <code>true</code> if command was used correctly
	 */
	@SblockCommand(consoleFriendly = true)
	public boolean profile(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
		}
		SblockUser user = UserManager.getUserManager().getUser(target[0]);
		if (user == null)
			sender.sendMessage(ChatColor.YELLOW + "User not found.");
		else {
			String message = 
					PROFILE_COLOR + "-----------------------------------------\n"
			   + ChatColor.YELLOW + target + ": " + user.getClassType().getDisplayName() + " of " + user.getAspect().getDisplayName() + "\n"
			   + PROFILE_COLOR    + "-----------------------------------------\n"
								  + "Dream planet: " + ChatColor.YELLOW + user.getDPlanet().getDisplayName() + "\n"
			   + PROFILE_COLOR    + "Medium planet: " + ChatColor.YELLOW + user.getMPlanet().getShortName() + "\n"
			   + PROFILE_COLOR    + "Echeladder rank: " + ChatColor.YELLOW + "Coming soon!";
			sender.sendMessage(message);
		}
		return true;
	}
	
	/**
	 * Set <code>SblockUser</code> data.
	 * 
	 * @param sender
	 *            the <code>CommandSender</code>
	 * @param playerToModify
	 *            the name of the <code>SblockUser</code> to modify
	 * @param type
	 *            the type of modification
	 * @param value
	 *            the value to set
	 * @return <code>true</code> if command was used correctly
	 */
	@SblockCommand(consoleFriendly = true)
	public boolean setplayer(CommandSender sender, String playerToModify, String type, String value)
	{
		if (sender instanceof Player && !sender.hasPermission("groups.horrorterror")) {
			sender.sendMessage(ChatColor.BLACK +
					"Such changes are not undertaken so easily by mere mortals.");
			return true;
		}
		SblockUser user = UserManager.getUserManager().getUser(playerToModify);
		type = type.toLowerCase();
		if(type.equals("class"))
			user.setPlayerClass(value);
		else if(type.equals("aspect"))
			user.setAspect(value);
		else if(type.replaceAll("m(edium_?)?planet", "land").equals("land"))
			user.setMediumPlanet(value);
		else if(type.replaceAll("d(ream_?)?planet", "dream").equals("dream"))
			user.setDreamPlanet(value);
		else if (type.equals("setprev")) {
			user.setPreviousLocation(user.getPlayer().getLocation());
		} else
			return false;
		return true;
	}
	
	/**
	 * Set specified tower <code>Location</code> to <code>CommandSender</code>'s
	 * current <code>Location</code>.
	 * 
	 * @param sender
	 *            the <code>CommandSender</code>
	 * @param number
	 *            the tower number to set
	 * @return <code>true</code> if command was used correctly
	 */
	@SblockCommand(consoleFriendly = false)
	public boolean settower(CommandSender sender, String number)
	{
		if (sender instanceof Player && !sender.hasPermission("groups.horrorterror")) {
			sender.sendMessage(ChatColor.BLACK +
					"Such changes are not undertaken so easily by mere mortals.");
			return true;
		}
		switch (Region.uValueOf(((Player)sender).getWorld().getName())) {
		case INNERCIRCLE:
		case OUTERCIRCLE:
			try {
				EventModule.getEventModule().getTowerData()
						.add(((Player)sender).getLocation(), Byte.valueOf(number));
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + number + " is not a valid number! Remember, 0-7.");
			}
			return true;
		default:
			sender.sendMessage(ChatColor.RED + "Invalid dream world, get thee hence!");
			return false;
		}
	}
}
