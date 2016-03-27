package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for toggling chat suppression.
 * 
 * @author Jikoo
 */
public class ChatSuppressCommand extends SblockCommand {

	private final Users users;

	public ChatSuppressCommand(Sblock plugin) {
		super(plugin, "suppress");
		this.users = plugin.getModule(Users.class);
		setDescription("Toggle chat suppression.");
		setUsage(ChatColor.AQUA + "/suppress");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		User user = users.getUser(((Player) sender).getUniqueId());
		user.setSuppression(!user.getSuppression());
		// TODO convert to lang
		user.sendMessage(Language.getColor("good") + "Suppression toggled " + (user.getSuppression() ? "on" : "off") + "!");
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
