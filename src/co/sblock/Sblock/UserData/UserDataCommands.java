package co.sblock.Sblock.UserData;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;
import co.sblock.Sblock.Chat.ChatMsgs;
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
	 * @param sender the <code>CommandSender</code>
	 * @param args the String[] of arguments where 0 is player name, 1 is data
	 *        being changed, and 2 is the new value
	 */
	@SblockCommand(consoleFriendly = true)
	public boolean setplayer(CommandSender sender, String[] args) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
			return true;
		}
		if (args == null || args.length < 3) {
			sender.sendMessage("setplayer <playername> <class|aspect|land|dream|prevloc> <value>");
			return true;
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
			sender.sendMessage("setplayer <playername> <class|aspect|land|dream|prevloc> <value>");
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
	public boolean settower(CommandSender sender, String number) {
		if (sender instanceof Player && !sender.hasPermission("group.horrorterror")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
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
			sender.sendMessage(ChatColor.RED + "You do not appear to be in a dream planet.");
			return true;
		}
	}

	/**
	 * A simple command warp wrapper to prevent users from using tower warps to other aspects.
	 */
	@SblockCommand(consoleFriendly = true)
	public boolean aspectwarp(CommandSender sender, String[] args) {
		if (sender instanceof Player && !sender.hasPermission("group.denizen")) {
			sender.sendMessage(ChatMsgs.permissionDenied());
		}
		if (args == null || args.length < 2) {
			sender.sendMessage("aspectwarp <warp> <player>: Warps player if aspect matches warp name.");
			return true;
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
	@SblockCommand
	public boolean spawn(CommandSender sender, String[] args) {
		((Player) sender).performCommand("mvs");
		return true;
	}
}
