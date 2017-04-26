package com.easterlyn.commands;

import com.easterlyn.Easterlyn;

import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Base for commands that should be processed off the main thread.
 * 
 * @author Jikoo
 */
public abstract class EasterlynAsynchronousCommand extends EasterlynCommand {

	public EasterlynAsynchronousCommand(Easterlyn plugin, String name) {
		super(plugin, name);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		new BukkitRunnable() {
			@Override
			public void run() {
				EasterlynAsynchronousCommand.super.execute(sender, label, args);
			}
		}.runTaskAsynchronously(getPlugin());
		return true;
	}

}
