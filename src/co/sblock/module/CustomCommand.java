package co.sblock.module;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

/**
 * A Command wrapper used to register commands automatically without needing
 * to enter them in the plugin.yml
 * 
 * @author Jikoo
 */
public class CustomCommand extends Command implements PluginIdentifiableCommand {

	public CustomCommand(String name) {
		super(name);
	}

	/**
	 * @see org.bukkit.command.Command#execute(CommandSender, String, String[])
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		try {
			return Bukkit.getPluginManager().getPlugin("Sblock").onCommand(sender, this, label, args);
		} catch (NullPointerException e) {
			// Sblock has not enabled properly.
			return false;
		}
	}

	/**
	 * @see org.bukkit.command.PluginIdentifiableCommand#getPlugin()
	 */
	@Override
	public Plugin getPlugin() {
		return Bukkit.getPluginManager().getPlugin("Sblock");
	}

	// TODO override tab completion

}
