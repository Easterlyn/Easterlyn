package co.sblock.commands.chat;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.events.listeners.player.AsyncChatListener;

import net.md_5.bungee.api.ChatColor;

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
		sender.sendMessage(ChatColor.RED + AsyncChatListener.test());
		return true;
	}

}
