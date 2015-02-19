package co.sblock.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Command for toggling chat suppression.
 * 
 * @author Jikoo
 */
public class ChatSuppressCommand extends SblockCommand {

	public ChatSuppressCommand() {
		super("suppress");
		setDescription("Toggle chat suppression.");
		setUsage(ChatColor.AQUA + "/suppress");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		user.setSuppression(!user.getSuppression());
		user.sendMessage(ChatColor.YELLOW + "Suppression toggled " + (user.getSuppression() ? "on" : "off") + "!");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length > 0) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}
}
