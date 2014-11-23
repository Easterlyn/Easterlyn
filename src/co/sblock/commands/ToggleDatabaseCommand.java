package co.sblock.commands;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.data.SblockData;

/**
 * SblockCommand for toggling database implementation.
 * 
 * @author Jikoo, tmathmeyer
 */
public class ToggleDatabaseCommand extends SblockCommand {

	public ToggleDatabaseCommand() {
		super("database");
		this.setDescription("Toggle the database implementation.");
		this.setUsage("/database");
		this.setPermission("sblock.ask.adam.before.touching");
		this.setPermissionMessage("&4&lOH NO YOU DI'INT.");
	}

	/* (non-Javadoc)
	 * @see co.sblock.commands.SblockCommand#execute(org.bukkit.command.CommandSender, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (sender.isOp()) {
			sender.sendMessage("Toggled database implementation to " + SblockData.toggleDBImpl());
		} else {
			sender.sendMessage("Op yosef son");
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
