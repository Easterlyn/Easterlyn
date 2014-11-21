package co.sblock.commands;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList.Type;
import org.bukkit.command.CommandSender;

/**
 * SblockCommand for undoing a dual IP and UUID ban.
 * 
 * @author Jikoo
 */
public class UnSuperBanCommand extends SblockCommand {

	public UnSuperBanCommand() {
		super("unsban");
		this.setDescription("DO THE WINDY THING.");
		this.setUsage("/unsban <UUID|name|IP>");
		this.setPermission("group.horrorterror");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		BanList bans = Bukkit.getBanList(Type.IP);
		BanList pbans = Bukkit.getBanList(Type.NAME);
		if (bans.isBanned(args[0])) {
			pbans.pardon(bans.getBanEntry(args[0]).getReason()
					.replaceAll(".*<name=(\\w{1,16}+)>.*", "$1"));
			bans.pardon(args[0]);
		} else if (pbans.isBanned(args[0])) {
			bans.pardon(pbans.getBanEntry(args[0]).getReason()
					.replaceAll(".*<ip=(([0-9]{1,3}\\.){3}[0-9]{1,3})>.*", "$1"));
			pbans.pardon(args[0]);
		} else  {
			sender.sendMessage(ChatColor.RED + "No bans were found for " + args[0]);
			return true;
		}
		if (args[0].contains(".")) {
			sender.sendMessage(ChatColor.GREEN + "Not globally announcing unban: " + args[0]
					+ " may be an IP.");
		} else {
			Bukkit.broadcastMessage(ChatColor.RED + "[Lil Hal] " + args[0] + " has been unbanned.");
		}
		return true;
	}
}
