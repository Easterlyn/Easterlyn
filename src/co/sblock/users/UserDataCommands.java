package co.sblock.users;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import co.sblock.chat.ChatMsgs;
import co.sblock.events.SblockEvents;
import co.sblock.machines.utilities.Icon;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;
import co.sblock.utilities.minecarts.FreeCart;

/**
 * Class for holding commands associated with the UserData module.
 * 
 * For more information on how the command system works, please see
 * {@link co.sblock.SblockCommand}
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
	@CommandDescription("Check a player's profile.")
	@CommandUsage("/profile <player>")
	@SblockCommand(consoleFriendly = true)
	public boolean profile(CommandSender sender, String[] target) {
		User user = null;
		if (target == null || target.length == 0) {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
				return true;
			}
			user = User.getUser(((Player) sender).getUniqueId());
		} else {
			Player pTarget = Bukkit.getPlayer(target[0]);
			if (pTarget != null) {
				user = UserManager.getUserManager().getUser(pTarget.getUniqueId());
			}
		}
		if (user == null) {
			sender.sendMessage(ChatColor.YELLOW + "User not found.");
			return true;
		}
		sender.sendMessage(PROFILE_COLOR + "-----------------------------------------\n"
				+ ChatColor.YELLOW + user.getPlayerName() + ": " + user.getPlayerClass().getDisplayName() + " of " + user.getAspect().getDisplayName() + "\n"
				+ PROFILE_COLOR    + "-----------------------------------------\n"
				+ "Dream planet: " + ChatColor.YELLOW + user.getDreamPlanet().getDisplayName() + "\n"
				+ PROFILE_COLOR + "Medium planet: " + ChatColor.YELLOW + user.getMediumPlanet().getShortName());
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
	@CommandDenial
	@CommandDescription("Set player data manually.")
	@CommandUsage("setplayer <playername> <class|aspect|land|dream|prevloc|progression> <value>")
	@CommandPermission("group.horrorterror")
	@SblockCommand(consoleFriendly = true)
	public boolean setplayer(CommandSender sender, String[] args) {
		if (args == null || args.length < 3) {
			return false;
		}
		User user = UserManager.getUserManager().getUser(Bukkit.getPlayer(args[0]).getUniqueId());
		args[1] = args[1].toLowerCase();
		if(args[1].equals("class"))
			user.setPlayerClass(args[2]);
		else if(args[1].equals("aspect"))
			user.setAspect(args[2]);
		else if(args[1].replaceAll("m(edium_?)?planet", "land").equals("land"))
			user.setMediumPlanet(args[2]);
		else if(args[1].replaceAll("d(ream_?)?planet", "dream").equals("dream"))
			user.setDreamPlanet(args[2]);
		else if(args[1].equals("progression"))
			user.setProgression(ProgressionState.valueOf(args[2].toUpperCase()));
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
	@CommandDenial
	@CommandDescription("Set tower location.")
	@CommandUsage("/settower <0-7>")
	@CommandPermission("group.horrorterror")
	@SblockCommand
	public boolean settower(CommandSender sender, String[] number) {
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
	@CommandDescription("Ask someone to be your Sburb Server player!")
	@CommandUsage("/requestserver <player>")
	@SblockCommand
	public boolean requestserver(CommandSender s, String[] args) {
		if (args.length == 0) {
			s.sendMessage(ChatColor.RED + "Who ya gonna call?");
			return true;
		}
		if (s.getName().equalsIgnoreCase(args[0])) {
			s.sendMessage(ChatColor.RED + "Playing with yourself can only entertain you for so long. Find a friend!");
			return true;
		}
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			s.sendMessage(ChatColor.RED + "Unknown user!");
			return true;
		}
		User u = User.getUser(p.getUniqueId());
		if (u == null) {
			s.sendMessage(ChatColor.RED + p.getName() + " needs to relog before you can do that!");
			p.sendMessage(ChatColor.RED + "Your data appears to not have been loaded. Please log out and back in!");
			return true;
		}
		if (u.getClient() != null) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " appears to have a client already! You'd best find someone else.");
			return true;
		}
		if (!u.getPrograms().contains(Icon.SBURBSERVER.getProgramID())) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " does not have the Sburb Server installed!");
			return true;
		}
		if (requests.containsKey(u.getPlayerName())) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " has a pending request to handle already!");
			return true;
		}
		s.sendMessage(ChatColor.YELLOW + "Request sent to " + ChatColor.GREEN + p.getName());
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
	@CommandDescription("Ask someone to be your Sburb Client player!")
	@CommandUsage("/requestclient <player>")
	@SblockCommand
	public boolean requestclient(CommandSender s, String[] args) {
		if (args.length == 0) {
			s.sendMessage(ChatColor.RED + "Who ya gonna call?");
			return true;
		}
		if (s.getName().equalsIgnoreCase(args[0])) {
			s.sendMessage(ChatColor.RED + "Playing with yourself can only entertain you for so long. Find a friend!");
			return true;
		}
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			s.sendMessage(ChatColor.RED + "Unknown user!");
			return true;
		}
		User u = User.getUser(p.getUniqueId());
		if (u == null) {
			s.sendMessage(ChatColor.RED + p.getName() + " needs to relog before you can do that!");
			p.sendMessage(ChatColor.RED + "Your data appears to not have been loaded. Please log out and back in!");
			return true;
		}
		if (u.getServer() != null) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " appears to have a server already! You'd best find someone else.");
			return true;
		}
		if (!u.getPrograms().contains(Icon.SBURBCLIENT.getProgramID())) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " does not have the Sburb Client installed!");
			return true;
		}
		if (requests.containsKey(u.getPlayerName())) {
			s.sendMessage(ChatColor.GOLD + u.getPlayerName() + ChatColor.RED
					+ " has a pending request to handle already!");
			return true;
		}
		s.sendMessage(ChatColor.YELLOW + "Request sent to " + ChatColor.GREEN + p.getName());
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
	@CommandDescription("Accept an open request!")
	@CommandUsage("/acceptrequest")
	@SblockCommand
	public boolean acceptrequest(CommandSender s, String[] args) {
		if (!requests.containsKey(s.getName())) {
			s.sendMessage(ChatColor.RED + "You should get someone to /requestserver or /requestclient before attempting to accept!");
			return true;
		}
		String req = requests.remove(s.getName());
		User u = User.getUser(((Player) s).getUniqueId());
		Player p1 = Bukkit.getPlayer(req.substring(1));
		if (p1 == null) {
			s.sendMessage(ChatColor.GOLD + req.substring(1) + ChatColor.RED + " appears to be offline! Request removed.");
			return true;
		}
		User u1 = User.getUser(p1.getUniqueId());
		if (req.charAt(0) == 'c') {
			u.setClient(u1.getUUID());
			u1.setServer(u.getUUID());
		} else {
			u1.setClient(u.getUUID());
			u.setServer(u1.getUUID());
		}
		s.sendMessage(ChatColor.YELLOW + "Accepted " + ChatColor.GREEN + u1.getPlayerName() + ChatColor.YELLOW + "'s request!");
		u1.getPlayer().sendMessage(ChatColor.GREEN + s.getName() + ChatColor.YELLOW + " accepted your request!");
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
	@CommandDescription("Say \"no\" to peer pressure!")
	@CommandUsage("/declinerequest")
	@SblockCommand
	public boolean declinerequest(CommandSender s, String[] args) {
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
	@CommandDenial
	@CommandDescription("Warps player if aspect matches warp name.")
	@CommandPermission("group.felt")
	@CommandUsage("aspectwarp <warp> <player>")
	@SblockCommand(consoleFriendly = true)
	public boolean aspectwarp(CommandSender sender, String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		Player p = Bukkit.getPlayer(args[1]);
		if (p == null) {
			sender.sendMessage(ChatMsgs.errorInvalidUser(args[1]));
			return true;
		}
		User u = User.getUser(p.getUniqueId());
		if (!u.getAspect().name().equalsIgnoreCase(args[0])) {
			return true;
		}
		sender.getServer().dispatchCommand(sender, "warp " + args[0] + " " + args[1]);
		return true;
	}

	@CommandDenial
	@CommandDescription("Teleports a player with no confirmation to either party involved. Intended for commandsigns.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("silenttp <player> <x> <y> <z> [pitch] [yaw]")
	@SblockCommand(consoleFriendly = true)
	public boolean silenttp(CommandSender sender, String[] args) {
		if (args == null || args.length < 4) {
			return false;
		}
		Player pTarget = Bukkit.getPlayer(args[0]);
		if (pTarget == null) {
			// silently eat player get failure in case CommandSign messes up, have seen it happen.
			return true;
		}
		try {
			Location tpdest = new Location(pTarget.getWorld(), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
			if (args.length >= 6) {
				tpdest.setPitch(Float.valueOf(args[4]));
				tpdest.setYaw(Float.valueOf(args[5]));
			}
			pTarget.teleport(tpdest);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Alias for spawn command to prevent confusion of new users.
	 */
	@CommandDescription("Teleport to this world's spawn.")
	@CommandUsage("/mvs")
	@SblockCommand
	public boolean spawn(CommandSender sender, String[] args) {
		((Player) sender).performCommand("mvs");
		return true;
	}

	@CommandDenial
	@CommandDescription("Spawns a temporary minecart with specified velocity vector at location, then mounts player.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("tempcart <player> <locX> <locY> <locZ> <vecX> <vecZ>")
	@SblockCommand(consoleFriendly = true)
	public boolean tempcart(CommandSender sender, String[] args) {
		if (args == null || args.length < 6) {
			return false;
		}
		Player pTarget = Bukkit.getPlayer(args[0]);
		if (pTarget == null) {
			return true;
		}
		try {
			Location cartDest = new Location(pTarget.getWorld(), Double.valueOf(args[1]), Double.valueOf(args[2]), Double.valueOf(args[3]));
			Vector cartVector = new Vector(Double.valueOf(args[4]), 0, Double.valueOf(args[5]));
			FreeCart.getInstance().spawnCart(pTarget, cartDest, cartVector);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
