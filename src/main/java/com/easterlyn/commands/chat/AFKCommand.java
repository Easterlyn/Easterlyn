package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.micromodules.AwayFromKeyboard;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for toggling AFK status.
 * 
 * @author Jikoo
 */
public class AFKCommand extends SblockCommand {

	private final AwayFromKeyboard afk;

	public AFKCommand(Easterlyn plugin) {
		super(plugin, "afk");
		this.setAliases("away");
		this.afk = plugin.getModule(AwayFromKeyboard.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		if (afk.isActive(player)) {
			afk.setInactive(player);
		} else {
			afk.setActive(player);
		}
		return true;
	}

}
