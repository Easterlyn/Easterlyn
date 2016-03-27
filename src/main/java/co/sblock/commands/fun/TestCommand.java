package co.sblock.commands.fun;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;
import co.sblock.events.listeners.player.AsyncChatListener;

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
		sender.sendMessage(Language.getColor("bad") + AsyncChatListener.test());
		return true;
	}

}
