package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.events.event.EasterlynAsyncChatEvent;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommand for performing an emote.
 * 
 * @author Jikoo
 */
public class MeCommand extends EasterlynAsynchronousCommand {

	private final Users users;

	public MeCommand(Easterlyn plugin) {
		super(plugin, "me");
		this.users = plugin.getModule(Users.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		Player player = (Player) sender;
		MessageBuilder builder = new MessageBuilder((Easterlyn) getPlugin()).setThirdPerson(true)
				.setSender(users.getUser(player.getUniqueId()))
				.setMessage(TextUtils.join(args, ' ', 0, args.length));

		if (builder.canNotBuild(true) || builder.isSenderNotInChannel(true)) {
			return true;
		}

		Bukkit.getPluginManager().callEvent(new EasterlynAsyncChatEvent(true, player, builder.toMessage()));
		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player)) {
			return ImmutableList.of("NoConsoleSupport");
		}
 		if (args.length > 1 || !args[0].isEmpty() && args[0].charAt(0) != '@') {
			return super.tabComplete(sender, alias, args);
		}
		User user = users.getUser(((Player) sender).getUniqueId());
		ArrayList<String> matches = new ArrayList<>();
		String toMatch = args.length == 0 || args[0].isEmpty() ? "" : args[0].substring(1);
		for (String s : user.getListening()) {
			if (StringUtil.startsWithIgnoreCase(s, toMatch)) {
				matches.add('@' + s);
			}
		}
		return matches;
	}
}
