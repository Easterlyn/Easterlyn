package co.sblock.commands.admin;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.module.Module;

/**
 * SblockCommand for reloading configurations.
 * 
 * @author Jikoo
 */
public class ConfigReloadCommand extends SblockCommand {

	public ConfigReloadCommand(Sblock plugin) {
		super(plugin, "sreload");
		this.setDescription("Reload a configuration.");
		this.setPermissionLevel("horrorterror");
		this.setUsage("/sreload [module class]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			getPlugin().reloadConfig();
			sender.sendMessage("Reloaded main configuration.");
			return true;
		}
		try {
			Class<?> clazz = Class.forName(args[0]);
			if (Module.class.isAssignableFrom(clazz)) {
				((Module) ((Sblock) getPlugin()).getModule(clazz)).loadConfig();
				sender.sendMessage("Reloaded module configuration.");
				return true;
			}
			sender.sendMessage("Specified class is not assignable from Module.");
			return true;
		} catch (ClassNotFoundException e) {
			sender.sendMessage("Specified string is not a full class name. Package is required.");
			return false;
		}
	}

}
