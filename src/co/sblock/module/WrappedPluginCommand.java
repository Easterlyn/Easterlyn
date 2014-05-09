package co.sblock.module;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;


/**
 * 
 * 
 * @author Jikoo
 */
public class WrappedPluginCommand extends Command implements PluginIdentifiableCommand {

	private Plugin plugin;
	private CommandExecutor executor;

	public WrappedPluginCommand(PluginCommand cmd) {
		super(cmd.getPlugin().getName() + ":" + cmd.getName(), cmd.getDescription(), cmd.getUsage(), cmd.getAliases());
		this.plugin = cmd.getPlugin();
		this.executor = cmd.getExecutor();
	}

	public CommandExecutor getExecutor() {
		return executor;
	}

	@Override
	public Plugin getPlugin() {
		return plugin;
	}

	@Override
	public boolean execute(CommandSender sender, String commandLabel, String[] args) {
		return executor.onCommand(sender, this, commandLabel, args);
	}

}
