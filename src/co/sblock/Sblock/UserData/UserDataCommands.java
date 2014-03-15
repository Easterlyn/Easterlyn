package co.sblock.Sblock.UserData;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
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

	/** Map containing all server/client player requests */
	public static Map<String, String> requests = new HashMap<String, String>();

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
		SblockUser user;
		if (target == null || target.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
				return true;
			}
			user = SblockUser.getUser(sender.getName());
		} else {
			user = UserManager.getUserManager().getUser(Bukkit.getPlayer(target[0]).getName());
		}
		if (user == null) {
			sender.sendMessage(ChatColor.YELLOW + "User not found.");
			return true;
		}
		sender.sendMessage(PROFILE_COLOR + "-----------------------------------------\n"
				+ ChatColor.YELLOW + user.getPlayerName() + ": " + user.getClassType().getDisplayName() + " of " + user.getAspect().getDisplayName() + "\n"
				+ PROFILE_COLOR    + "-----------------------------------------\n"
				+ "Dream planet: " + ChatColor.YELLOW + user.getDPlanet().getDisplayName() + "\n"
				+ PROFILE_COLOR + "Medium planet: " + ChatColor.YELLOW + user.getMPlanet().getShortName());
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
	 * Send a request for a server player.
	 * 
	 * @param s the CommandSender
	 * @param args the name of the player to send a request to
	 * 
	 * @return true
	 */
	@SblockCommand(description = "Ask someone to be your Sburb Server player!", usage = "/requestserver <player>")
	public boolean requestServer(CommandSender s, String[] args) {
		if (args.length == 0) {
			s.sendMessage(ChatColor.RED + "Who ya gonna call?");
			return true;
		}
		SblockUser u = SblockUser.getUser(Bukkit.getPlayer(args[0]).getName());
		if (u != null && u.getClient() != null) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " appears to have a client already! You'd best find someone else.");
			return true;
		}
		if (requests.containsKey(u.getPlayerName())) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " has a pending request to handle already!");
			return true;
		}
		requests.put(u.getPlayerName(), "c" + s.getName());
		u.getPlayer().sendMessage(ChatColor.GREEN + s.getName() + ChatColor.YELLOW
				+ " has requested that you be their server!" + ChatColor.AQUA
				+ "\n/acceptrequest" + ChatColor.YELLOW + " or "
				+ ChatColor.AQUA + "/declinerequest");
		return true;
	}

	/**
	 * Send a request for a client player.
	 * 
	 * @param s the CommandSender
	 * @param args the name of the player to send a request to
	 * 
	 * @return true
	 */
	@SblockCommand(description = "Ask someone to be your Sburb Client player!", usage = "/requestclient <player>")
	public boolean requestClient(CommandSender s, String[] args) {
		if (args.length == 0) {
			s.sendMessage(ChatColor.RED + "Who ya gonna call?");
			return true;
		}
		SblockUser u = SblockUser.getUser(Bukkit.getPlayer(args[0]).getName());
		if (u != null && u.getServer() != null) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " appears to have a server already! You'd best find someone else.");
			return true;
		}
		if (requests.containsKey(u.getPlayerName())) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " has a pending request to handle already!");
			return true;
		}
		requests.put(u.getPlayerName(), "s" + s.getName());
		u.getPlayer().sendMessage(ChatColor.GREEN + s.getName() + ChatColor.YELLOW
				+ " has requested that you be their client!" + ChatColor.AQUA
				+ "\n/acceptrequest" + ChatColor.YELLOW + " or "
				+ ChatColor.AQUA + "/declinerequest");
		return true;
	}

	/**
	 * Accept a pending server or client request.
	 * 
	 * @param s the CommandSender
	 * @param args ignored
	 * 
	 * @return true
	 */
	@SblockCommand(description = "Accept an open request!", usage = "/acceptrequest")
	public boolean acceptRequest(CommandSender s, String[] args) {
		if (!requests.containsKey(s.getName())) {
			s.sendMessage(ChatColor.RED + "You should get someone to /requestserver or /requestclient before attempting to accept!");
			return true;
		}
		String req = requests.remove(s.getName());
		SblockUser u = SblockUser.getUser(s.getName());
		SblockUser u1 = SblockUser.getUser(req.substring(1));
		if (u1 == null) {
			s.sendMessage(ChatColor.GOLD + req.substring(1) + ChatColor.RED + " appears to be offline! Request removed.");
			return true;
		}
		if (req.charAt(0) == 'c') {
			u.setClient(u1.getPlayerName());
			u1.setServer(u.getPlayerName());
		} else {
			u1.setClient(u.getPlayerName());
			u.setServer(u1.getPlayerName());
		}
		s.sendMessage(ChatColor.YELLOW + "Accepted " + ChatColor.GREEN + u1.getPlayerName() + ChatColor.YELLOW + "'s request!");
		u1.getPlayer().sendMessage(ChatColor.GREEN + u1.getPlayerName() + ChatColor.YELLOW + " accepted your request!");
		return true;
	}

	/**
	 * Decline a pending server or client request.
	 * 
	 * @param s the CommandSender
	 * @param args ignored
	 * 
	 * @return true
	 */
	@SblockCommand(description = "Say \"no\" to peer pressure!", usage = "/declinerequest")
	public boolean declineRequest(CommandSender s, String[] args) {
		if (!requests.containsKey(s.getName())) {
			s.sendMessage(ChatColor.RED + "You vigorously decline... no one."
					+ "\nPerhaps you should get someone to /requestserver or /requestclient first?");
		}
		String name = requests.remove(s.getName()).substring(1);
		Player p = Bukkit.getPlayer(name);
		if (p != null) {
			p.sendMessage(ChatColor.GOLD + s.getName() + ChatColor.RED + " has declined your request!");
		}
		s.sendMessage(ChatColor.RED + "Declined request from " + ChatColor.GOLD + name
				+ ChatColor.RED + "!");
		return true;
		
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
