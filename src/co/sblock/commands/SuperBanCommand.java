package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList.Type;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * SblockCommand for a dual ip and UUID ban.
 * 
 * @author Jikoo
 */
public class SuperBanCommand extends SblockCommand {

	public SuperBanCommand() {
		super("sban");
		this.setDescription("YOU CAN'T ESCAPE THE RED MILES.");
		this.setUsage("/sban <target> [optional reason]");
		this.setPermission("group.denizen");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		String target = args[0];
		StringBuilder reason = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			reason.append(ChatColor.translateAlternateColorCodes('&', args[i])).append(' ');
		}
		if (args.length == 1) {
			reason.append("Git wrekt m8.");
		}
		if (target.contains(".")) { // IPs probably shouldn't be announced.
			Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(target, reason.toString(), null, sender.getName());
		} else {
			Bukkit.broadcastMessage(ChatColor.DARK_RED + target
					+ " has been wiped from the face of the multiverse. " + reason.toString());
			Player p = Bukkit.getPlayer(target);

			if (p != null) { // This method is actually more efficient than getting an OfflinePlayer without a UUID
				User victim = UserManager.getUser(p.getUniqueId());
				Bukkit.getBanList(Type.NAME).addBan(victim.getPlayerName(),
						"<ip=" + victim.getUserIP() + ">" + reason, null, sender.getName());
				Bukkit.getBanList(Type.IP).addBan(victim.getUserIP(),
						"<name=" + victim.getPlayerName() + ">" + reason, null, sender.getName());
				victim.getPlayer().kickPlayer(reason.toString());
			} else {
				Bukkit.getBanList(org.bukkit.BanList.Type.NAME).addBan(target, reason.toString(), null, sender.getName());
			}
		}
		return true;
	}
}
