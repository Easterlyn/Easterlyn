package co.sblock.commands.admin;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for updating simple config values.
 * 
 * @author Jikoo
 */
public class ConfigChangeCommand extends SblockCommand {

	public ConfigChangeCommand() {
		super("configupdate");
		this.setDescription("Live config editing.");
		this.setUsage("/sblockconfig <path> <value>");
		this.setPermissionLevel("horrorterror");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			return false;
		}
		Sblock sblock = Sblock.getInstance();
		if (args[1].equalsIgnoreCase("null")) {
			sblock.getConfig().set(args[0], null);
			sblock.saveConfig();
			sender.sendMessage("Removed option " + args[0]);
			return true;
		}
		if (args[1].equalsIgnoreCase("false")) {
			sblock.getConfig().set(args[0], false);
			sblock.saveConfig();
			sender.sendMessage("Set " + args[0] + " to Boolean: false");
			return true;
		}
		if (args[1].equalsIgnoreCase("true")) {
			sblock.getConfig().set(args[0], null);
			sblock.saveConfig();
			sender.sendMessage("Set " + args[0] + " to Boolean: true");
			return true;
		}
		try {
			int integer = Integer.valueOf(args[1]);
			sblock.getConfig().set(args[0], integer);
			sblock.saveConfig();
			sender.sendMessage("Set " + args[0] + " to Integer: " + integer);
		} catch (NumberFormatException e) {
			sblock.getConfig().set(args[0], args[1]);
			sender.sendMessage("Set " + args[0] + " to String: " + args[1]);
		}
		return true;
	}
}
