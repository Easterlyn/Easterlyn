package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChatMsgs;
import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.ChannelType;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * Command for changing nickname in a channel.
 * 
 * @author Jikoo
 */
public class ChatNickCommand extends SblockCommand {

	public ChatNickCommand() {
		super("nick");
		setDescription(ChatColor.AQUA + "/nick remove|list|<nick choice>"
				+ ChatColor.YELLOW + ": Set a nick in a Nick/RP channel.");
		setUsage("/nick name");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		Channel c = user.getCurrentChannel();
		if (c == null) {
			user.sendMessage(ChatMsgs.errorNoCurrent());
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		if (!(c instanceof NickChannel)) {
			user.sendMessage(ChatMsgs.unsupportedOperation(c.getName()));
			return true;
		}
		if (args[0].equalsIgnoreCase("list")) {
			if (c.getType() == ChannelType.NICK) {
				user.sendMessage(ChatColor.YELLOW + "You can use any nick you want in a nick channel.");
				return true;
			}
			StringBuilder sb = new StringBuilder(ChatColor.YELLOW.toString()).append("Nicks: ");
			for (CanonNick n : CanonNick.values()) {
				if (n != CanonNick.SERKITFEATURE) {
					sb.append(ChatColor.AQUA).append(n.getName());
					sb.append(ChatColor.YELLOW).append(", ");
				}
			}
			user.sendMessage(sb.substring(0, sb.length() - 4).toString());
			return true;
		}
		if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("off")) {
			c.removeNick(user, true);
			return true;
		}
		StringBuilder sb = new StringBuilder();
		for (char character : StringUtils.join(args, ' ', 2, args.length).toCharArray()) {
			if (Character.isAlphabetic(character) || Character.isDigit(character) || Character.isSpaceChar(character)) {
				sb.append(character);
			}
		}
		c.setNick(user, sb.toString());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length > 1) {
			return ImmutableList.of();
		}
		if (Users.getGuaranteedUser(((Player) sender).getUniqueId()).getCurrentChannel() instanceof RPChannel) {
			ArrayList<String> matches = new ArrayList<>();
			args[0] = args[0].toUpperCase();
			for (CanonNick nick : CanonNick.values()) {
				if (nick != CanonNick.SERKITFEATURE && nick.name().startsWith(args[0])) {
					matches.add(nick.name());
				}
			}
			return matches;
		}
		return ImmutableList.of();
	}
}
