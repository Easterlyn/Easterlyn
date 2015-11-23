package co.sblock.commands.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;

/**
 * DAMN IT ALL, BASEMENTHERO, FINE.
 * 
 * @author Jikoo
 */
public class TestCommand extends SblockCommand {

	public TestCommand(Sblock plugin) {
		super(plugin, "test");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("TEST FAILED, JERK.");
			return true;
		}
		((Player) sender).chat("test");
		return true;
	}

}
