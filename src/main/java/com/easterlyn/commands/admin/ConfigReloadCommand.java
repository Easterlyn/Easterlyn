package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.module.Module;
import com.easterlyn.users.UserRank;

import org.bukkit.command.CommandSender;

/**
 * SblockCommand for reloading configurations.
 * 
 * @author Jikoo
 */
public class ConfigReloadCommand extends SblockCommand {

	public ConfigReloadCommand(Easterlyn plugin) {
		super(plugin, "sreload");
		this.setDescription("Reload a configuration.");
		this.setPermissionLevel(UserRank.HORRORTERROR);
		this.setUsage("/sreload [module class]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			getPlugin().reloadConfig();
			sender.sendMessage("Reloaded main configuration.");
			return true;
		}
		if (args[0].equalsIgnoreCase("rpack")) {
			((Easterlyn) getPlugin()).reloadResourceHashes();
			sender.sendMessage("Reloaded resource pack hashes.");
			return true;
		}
		try {
			Class<?> clazz = Class.forName(args[0]);
			if (Module.class.isAssignableFrom(clazz)) {
				((Module) ((Easterlyn) getPlugin()).getModule(clazz)).loadConfig();
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
