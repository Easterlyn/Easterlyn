package co.sblock.commands.chat;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.AwayFromKeyboard;

/**
 * Command for toggling AFK status.
 * 
 * @author Jikoo
 */
public class AFKCommand extends SblockCommand {

	private final AwayFromKeyboard afk;

	public AFKCommand(Sblock plugin) {
		super(plugin, "afk");
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.setAliases("away");
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
