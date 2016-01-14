package co.sblock.commands;

import org.bukkit.command.Command;

import co.sblock.Sblock;

/**
 * Abstract class for SblockCommands that are essentially slightly manipulated aliases of existing
 * commands.
 * 
 * @author Jikoo
 */
public abstract class SblockCommandAlias extends SblockCommand {

	private final Command command;

	public SblockCommandAlias(Sblock plugin, String name, String originalCommand) {
		super(plugin, name);
		this.command = plugin.getCommandMap().getCommand(originalCommand);
		if (command == null) {
			throw new IllegalStateException("Command cannot be null");
		}
		this.setDescription(command.getDescription());
		this.setPermission(command.getPermission());
		this.setPermissionMessage(command.getPermissionMessage());
		this.setUsage(command.getUsage());
	}

	/**
	 * Get the Command backing this alias.
	 * 
	 * @return the Command
	 */
	protected Command getCommand() {
		return this.command;
	}

}
