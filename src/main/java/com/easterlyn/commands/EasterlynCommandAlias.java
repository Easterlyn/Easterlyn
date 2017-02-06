package com.easterlyn.commands;

import com.easterlyn.Easterlyn;

import org.bukkit.command.Command;

/**
 * Abstract class for EasterlynCommands that are essentially slightly manipulated aliases of existing
 * commands.
 * 
 * @author Jikoo
 */
public abstract class EasterlynCommandAlias extends EasterlynCommand {

	private final Command command;

	public EasterlynCommandAlias(Easterlyn plugin, String name, String originalCommand) {
		super(plugin, name);
		this.command = plugin.getCommandMap().getCommand(originalCommand);
		if (command == null) {
			throw new IllegalStateException(originalCommand + " is not a registered command!");
		}
		if (command.getDescription() != null) {
			this.setDescription(command.getDescription());
		}
		this.setPermission(command.getPermission());
		if (command.getPermissionMessage() != null) {
			this.setPermissionMessage(command.getPermissionMessage());
		}
		if (command.getUsage() != null) {
			this.setUsage(command.getUsage());
		}
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
