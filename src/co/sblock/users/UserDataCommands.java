package co.sblock.users;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import co.sblock.chat.ChatMsgs;
import co.sblock.data.SblockData;
import co.sblock.machines.utilities.Icon;
import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;
import co.sblock.utilities.Broadcast;
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

	@CommandDescription("Check data stored for a player")
	@CommandUsage("/whois <exact player>")
	@SblockCommand(consoleFriendly = true)
	public boolean whois(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
		}
		if (sender instanceof Player && !sender.hasPermission("group.denizen")) {
			((Player) sender).performCommand("profile " + target[0]);
			return true;
		}
		Player p = Bukkit.getPlayer(target[0]);
		if (p == null) {
			SblockData.getDB().startOfflineLookup(sender, target[0]);
			return true;
		}
		User u = User.getUser(p.getUniqueId());
		sender.sendMessage(u.toString());
		return true;
	}

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

	@CommandDescription("Ask someone to be your Sburb server player!")
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

	@CommandDescription("Ask someone to be your Sburb client player!")
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

	@CommandDenial
	@CommandDescription("YOU CAN'T ESCAPE THE RED MILES.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/sban <target>")
	@SblockCommand(consoleFriendly = true)
	public boolean sban(CommandSender sender, String[] args) {
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Specify a player.");
			return true;
		}
		String target = args[0];
		StringBuilder reason = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			reason.append(args[i]).append(' ');
		}
		if (args.length == 1) {
			reason.append("Git wrekt m8.");
		}
		if (target.contains(".")) { // IPs probably shouldn't be announced.
			Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(target, reason.toString(), null, "sban");
		} else {
			Broadcast.general(ChatColor.DARK_RED + target
					+ " has been wiped from the face of the multiverse. " + reason.toString());
			Player p = Bukkit.getPlayer(target);
			if (p != null) {
				User victim = User.getUser(p.getUniqueId());
				SblockData.getDB().addBan(victim, reason.toString());
				SblockData.getDB().deleteUser(victim.getPlayerName());
				victim.getPlayer().kickPlayer(reason.toString());
				Bukkit.dispatchCommand(sender, "lwc admin purge " + p.getUniqueId());
			} else {
				Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target, reason.toString(), null, "sban");
			}
		}
		Bukkit.dispatchCommand(sender, "lwc admin purge " + target);
		return true;
	}

	@CommandDenial
	@CommandDescription("DO THE WINDY THING.")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/unsban <UUID|name|IP>")
	@SblockCommand(consoleFriendly = true)
	public boolean unsban(CommandSender sender, String[] target) {
		if (target == null || target.length == 0) {
			return false;
		}
		SblockData.getDB().removeBan(target[0]);
		if (target[0].contains(".")) {
			sender.sendMessage(ChatColor.GREEN + "Not globally announcing unban: " + target[0]
					+ " may be an IP.");
		} else {
			Bukkit.broadcastMessage(ChatColor.RED + "[Lil Hal] " + target[0] + " has been unbanned.");
		}
		return true;
	}

	@CommandDescription("Run an eye over the server rules.")
	@CommandUsage("/? Rules")
	@SblockCommand(consoleFriendly = true)
	public boolean rules(CommandSender sender, String[] args) {
		Bukkit.dispatchCommand(sender, "? Rules");
		return true;
	}

	@CommandDescription("See what's what.")
	@CommandUsage("/?")
	@SblockCommand(consoleFriendly = true)
	public boolean help(CommandSender sender, String[] args) {
		Bukkit.dispatchCommand(sender, "? " + StringUtils.join(args, ' ').trim());
		return true;
	}
}
