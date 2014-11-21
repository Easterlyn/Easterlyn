package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.rawmessages.EscapedElement;

/**
 * SblockCommand for /aether, the command executed to make IRC chat mimic normal channels.
 * 
 * @author Jikoo
 */
public class AetherCommand extends SblockCommand {

	public AetherCommand() {
		super("aether");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setUsage("/aether <text>");
		this.setPermission("group.horrorterror");
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		// TODO Wrap links and shit so client can click 'em
		String message = ChatColor.WHITE + "[" + ChatColor.GOLD + "#Aether" + ChatColor.WHITE + "]" + ChatColor.WHITE;
		if (!args[0].equals(">")) {
			message += " ";
		}
		message += StringUtils.join(args, ' ');
		Bukkit.getConsoleSender().sendMessage(message);
		message = new EscapedElement(message).toString();
		for (Player p : Bukkit.getOnlinePlayers()) {
			User u = UserManager.getUser(p.getUniqueId());
			if (!u.isSuppressing()) {
				u.rawHighlight(message);
			}
		}
		return true;
	}
}
