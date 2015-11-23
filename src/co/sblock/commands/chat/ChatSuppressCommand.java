package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for toggling chat suppression.
 * 
 * @author Jikoo
 */
public class ChatSuppressCommand extends SblockCommand {

	public ChatSuppressCommand(Sblock plugin) {
		super(plugin, "suppress");
		setDescription("Toggle chat suppression.");
		setUsage(ChatColor.AQUA + "/suppress");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		OfflineUser user = Users.getGuaranteedUser(((Sblock) getPlugin()), ((Player) sender).getUniqueId());
		user.setSuppression(!user.getSuppression());
		user.sendMessage(Color.GOOD + "Suppression toggled " + (user.getSuppression() ? "on" : "off") + "!");
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
