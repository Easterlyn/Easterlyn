package com.easterlyn.commands.chat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for toggling chat suppression.
 * 
 * @author Jikoo
 */
public class ChatSuppressCommand extends EasterlynCommand {

	private final Users users;

	public ChatSuppressCommand(Easterlyn plugin) {
		super(plugin, "suppress");
		setDescription("Toggle chat suppression.");
		setUsage(ChatColor.AQUA + "/suppress");
		this.users = plugin.getModule(Users.class);
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
