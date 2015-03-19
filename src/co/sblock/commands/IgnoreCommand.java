package co.sblock.commands;

import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * 
 * 
 * @author Jikoo
 */
public class IgnoreCommand extends SblockAsynchronousCommand {

	public IgnoreCommand() {
		super("ignore");
		setDescription("Ignore someone.");
		setUsage("/(un)ignore <player>");
		setAliases("unignore");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			return false;
		}
		UUID uuid = getUniqueId(args[0]);
		if (uuid == null) {
			sender.sendMessage(ChatColor.RED + "Unknown player!");
			return true;
		}
		// TODO Auto-generated method stub
		return false;
	}

}
