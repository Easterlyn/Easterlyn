package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.data.SblockData;
import co.sblock.users.User;
import co.sblock.users.UserManager;

/**
 * SblockCommand featuring a SuperBan along with several protection removal commands.
 * 
 * @author Jikoo
 */
public class UltraBanCommand extends SblockCommand {

	public UltraBanCommand() {
		super("ultraban");
		this.setDescription("YOU REALLY CAN'T ESCAPE THE RED MILES.");
		this.setUsage("/ultraban <target>");
		this.setPermission("group.horrorterror");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!Bukkit.dispatchCommand(sender, "sban " + StringUtils.join(args, ' '))) {
			// sban will return its own usage failure, no need to double message.
			return true;
		}

		Player p = Bukkit.getPlayer(args[0]);
		if (p != null) {
			User victim = UserManager.getUser(p.getUniqueId());
			SblockData.getDB().deleteUser(victim.getUUID());
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lwc admin purge " + p.getUniqueId());
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lwc admin purge " + args[0]);
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ps delete " + args[0]);
		return true;
	}
}
