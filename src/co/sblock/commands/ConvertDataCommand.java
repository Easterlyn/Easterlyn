package co.sblock.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import co.sblock.data.sql.SQLClient;

import com.google.common.collect.ImmutableList;

/**
 * 
 * 
 * @author Jikoo
 */
public class ConvertDataCommand extends SblockCommand {

	SQLClient database;
	public ConvertDataCommand() {
		super("convertdata");
		this.setDescription("Convert SQL to yaml.");
		this.setUsage("/convertdata");
		this.setPermission("sblock.ask.adam.before.touching");
		this.setPermissionMessage("&4&lOH NO YOU DI'INT.");
		database = new SQLClient();
		database.enable();
	}

	public boolean execute(CommandSender sender, String label, String[] args) {
		database.loadAllPlayers();
		database.loadAllChannelData();
		database.loadAllMachines();
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
