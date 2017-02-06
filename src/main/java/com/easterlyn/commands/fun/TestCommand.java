package com.easterlyn.commands.fun;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.events.listeners.player.AsyncChatListener;

import org.bukkit.command.CommandSender;

/**
 * DAMN IT ALL, BASEMENTHERO, FINE.
 * 
 * @author Jikoo
 */
public class TestCommand extends EasterlynCommand {

	public TestCommand(Easterlyn plugin) {
		super(plugin, "test");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		sender.sendMessage(Language.getColor("bad") + AsyncChatListener.test());
		return true;
	}

}
