package co.sblock.Sblock;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * A Command wrapper used to register commands automatically without needing
 * to enter them in the plugin.yml
 * 
 * @author Jikoo
 */
public class CustomCommand extends Command {

	public CustomCommand(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see org.bukkit.command.Command#execute(org.bukkit.command.CommandSender, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		return Sblock.getInstance().onCommand(sender, this, label, args);
	}

}
