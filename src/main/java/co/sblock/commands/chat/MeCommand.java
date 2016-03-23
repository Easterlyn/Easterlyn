package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * SblockCommand for performing an emote.
 * 
 * @author Jikoo
 */
public class MeCommand extends SblockAsynchronousCommand {

	private final Users users;

	public MeCommand(Sblock plugin) {
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
		MessageBuilder builder = new MessageBuilder((Sblock) getPlugin()).setThirdPerson(true)
				.setSender(users.getUser(player.getUniqueId()))
				.setMessage(StringUtils.join(args, ' ', 0, args.length));

		if (!builder.canBuild(true) || !builder.isSenderInChannel(true)) {
			return true;
		}

		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(true, player, builder.toMessage()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player)) {
			return ImmutableList.of("NoConsoleSupport");
		}
 		if (args.length > 1 || !args[0].isEmpty() && args[0].charAt(0) != '@') {
			return super.tabComplete(sender, alias, args);
		}
		User user = users.getUser(((Player) sender).getUniqueId());
		ArrayList<String> matches = new ArrayList<>();
		String toMatch = args.length == 0 || args[0].isEmpty() ? new String() : args[0].substring(1);
		for (String s : user.getListening()) {
			if (StringUtil.startsWithIgnoreCase(s, toMatch)) {
				matches.add('@' + s);
			}
		}
		return matches;
	}
}
