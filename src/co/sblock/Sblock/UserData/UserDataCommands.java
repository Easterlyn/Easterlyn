package co.sblock.Sblock.UserData;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.ChatMsgs;
import co.sblock.Sblock.Events.SblockEvents;

/**
 * Class for holding commands associated with the UserData module.
 * 
 * For more information on how the command system works, please see
 * {@link co.sblock.Sblock.SblockCommand}
 * 
 * @author FireNG, Jikoo
 */
public class UserDataCommands implements CommandListener {

	/** The standard profile color. */
	public static final ChatColor PROFILE_COLOR = ChatColor.DARK_AQUA;

	/**
	 * Gets the profile of a SblockUser.
	 * 
	 * @param sender the CommandSender
	 * @param target the SblockUser to look up
	 * 
	 * @return true if command was used correctly
	 */
	@SblockCommand(consoleFriendly = true, description = "Check a player's profile.", usage = "")
	public boolean profile(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
		}
		SblockUser user = UserManager.getUserManager().getUser(target[0]);
		if (user == null)
			sender.sendMessage(ChatColor.YELLOW + "User not found.");
		else {
			String message =  PROFILE_COLOR + "-----------------------------------------\n"
					+ ChatColor.YELLOW + target + ": " + user.getClassType().getDisplayName() + " of " + user.getAspect().getDisplayName() + "\n"
					+ PROFILE_COLOR    + "-----------------------------------------\n"
					+ "Dream planet: " + ChatColor.YELLOW + user.getDPlanet().getDisplayName() + "\n"
					+ PROFILE_COLOR + "Medium planet: " + ChatColor.YELLOW + user.getMPlanet().getShortName();
			sender.sendMessage(message);
		}
		return true;
	}
	
	/**
	 * Set SblockUser data.
	 * 
	 * @param sender the CommandSender
	 * @param args the String[] of arguments where 0 is player name, 1 is data
	 *        being changed, and 2 is the new value
	 * 
	 * @return true if command was used correctly
	 */
	@SblockCommand(consoleFriendly = true, description = "Set player data",
			usage = "setplayer <playername> <class|aspect|land|dream|prevloc> <value>")
	public boolean setplayer(CommandSender sender, String[] args) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
			return true;
		}
		if (args == null || args.length < 3) {
			return false;
		}
		SblockUser user = UserManager.getUserManager().getUser(args[0]);
		args[1] = args[1].toLowerCase();
		if(args[1].equals("class"))
			user.setPlayerClass(args[2]);
		else if(args[1].equals("aspect"))
			user.setAspect(args[2]);
		else if(args[1].replaceAll("m(edium_?)?planet", "land").equals("land"))
			user.setMediumPlanet(args[2]);
		else if(args[1].replaceAll("d(ream_?)?planet", "dream").equals("dream"))
			user.setDreamPlanet(args[2]);
		else if (args[1].equals("prevloc")) {
			user.setPreviousLocation(user.getPlayer().getLocation());
		} else
			return false;
		return true;
	}
	
	/**
	 * Set specified tower Location to CommandSender's current Location.
	 * 
	 * @param sender the CommandSender
	 * @param number the tower number to set
	 * 
	 * @return true if command was used correctly
	 */
	@SblockCommand(description = "Set tower location.", usage = "/settower <0-7>")
	public boolean settower(CommandSender sender, String[] number) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
			return true;
		}
		if (number == null || number.length == 0) {
			return false;
		}
		switch (Region.uValueOf(((Player)sender).getWorld().getName())) {
		case INNERCIRCLE:
		case OUTERCIRCLE:
			try {
				SblockEvents.getEvents().getTowerData()
						.add(((Player)sender).getLocation(), Byte.valueOf(number[0]));
			} catch (NumberFormatException e) {
				sender.sendMessage(ChatColor.RED + number[0] + " is not a valid number! Remember, 0-7.");
			}
			return true;
		default:
			sender.sendMessage(ChatColor.RED + "You do not appear to be in a dream planet.");
			return true;
		}
	}

	/**
	 * A simple command warp wrapper to prevent users from using tower warps to other aspects.
	 * 
	 * @param sender the CommandSender
	 * @param args the String[] of arguments where 0 is aspect/warp, 1 is player name
	 * 
	 * @return true if command was used correctly
	 */
	@SblockCommand(consoleFriendly = true, description = "Warps player if aspect matches warp name.",
			usage = "aspectwarp <warp> <player>")
	public boolean aspectwarp(CommandSender sender, String[] args) {
		if (sender instanceof Player && !sender.hasPermission("group.denizen")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
		}
		if (args == null || args.length < 2) {
			return false;
		}
		SblockUser u = SblockUser.getUser(args[1]);
		if (u == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		if (!u.getAspect().name().equalsIgnoreCase(args[0])) {
			return true;
		}
		sender.getServer().dispatchCommand(sender, "warp " + args[0] + " " + args[1]);
		return true;
	}

	/**
	 * Alias for spawn command to prevent confusion of new users.
	 */
	@SblockCommand(description = "Teleport to this world's spawn.", usage = "/mvs")
	public boolean spawn(CommandSender sender, String[] args) {
		((Player) sender).performCommand("mvs");
		return true;
	}
}
