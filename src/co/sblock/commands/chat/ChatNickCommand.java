package co.sblock.commands.chat;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChatMsgs;
import co.sblock.chat.Color;
import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for changing nickname in a channel.
 * 
 * @author Jikoo
 */
public class ChatNickCommand extends SblockCommand {

	public ChatNickCommand() {
		super("nick");
		setDescription(Color.COMMAND + "/nick remove|list|<nick choice>"
				+ Color.GOOD + ": Set a nick in a Nick/RP channel.");
		setUsage("/nick name");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		OfflineUser user = Users.getGuaranteedUser(((Player) sender).getUniqueId());
		Channel channel = user.getCurrentChannel();
		if (channel == null) {
			user.sendMessage(ChatMsgs.errorCurrentChannelNull());
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		if (!(channel instanceof NickChannel)) {
			user.sendMessage(ChatMsgs.unsupportedOperation(channel.getName()));
			return true;
		}
		if (args[0].equalsIgnoreCase("list")) {
			if (channel instanceof RPChannel) {
				StringBuilder sb = new StringBuilder(Color.GOOD.toString()).append("Nicks: ");
				for (CanonNick n : CanonNick.values()) {
					if (n != CanonNick.SERKITFEATURE) {
						sb.append(Color.GOOD_EMPHASIS).append(n.getId());
						sb.append(Color.GOOD).append(", ");
					}
				}
				user.sendMessage(sb.substring(0, sb.length() - 4).toString());
				return true;
			}
			user.sendMessage(Color.GOOD + "You can use any nick you want in a nick channel.");
			return true;
		}
		NickChannel nick = (NickChannel) channel;
		if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("off")) {
			String oldName = nick.removeNick(user);
			if (oldName != null) {
				nick.sendMessage(ChatMsgs.onUserRmNick(user.getPlayerName(), oldName, nick.getName()));
			} else {
				sender.sendMessage(Color.BAD + "You do not have a nick currently.");
			}
			return true;
		}
		StringBuilder sb = new StringBuilder();
		for (char character : Normalizer.normalize(StringUtils.join(args, ' '),
				Normalizer.Form.NFD).toCharArray()) {
			if (character > '\u001F' && character < '\u007E') {
				sb.append(character);
			}
		}
		if (sb.length() == 0) {
			sender.sendMessage(Color.BAD
					+ "Nicks must be 1+ characters long when stripped of non-ASCII characters.");
			return true;
		}
		String cleanName = sb.toString();
		String nickname = sb.toString();
		if (nick instanceof RPChannel) {
			CanonNick canonNick = CanonNick.getNick(nickname);
			if (canonNick == null) {
				sender.sendMessage(ChatMsgs.errorNickNotCanon(nickname));
				return true;
			}
			nickname = canonNick.name();
			cleanName = canonNick.getDisplayName();
		}
		OfflineUser nickOwner = nick.getNickOwner(nickname);
		if (nickOwner != null) {
			if (!nickOwner.getUUID().equals(user.getUUID())) {
				sender.sendMessage(ChatMsgs.errorNickTaken(cleanName));
				return true;
			}
			// Don't send a message for nick changes if owner is changing back to the same nick (or an RP variant)
		} else {
			nick.sendMessage(ChatMsgs.onUserSetNick(user.getPlayerName(), cleanName, nick.getName()));
		}
		nick.setNick(user, ChatColor.translateAlternateColorCodes('&', nickname));
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
			args[0] = args[0].toLowerCase();
			for (CanonNick nick : CanonNick.values()) {
				if (nick != CanonNick.SERKITFEATURE && nick.getId().toLowerCase().startsWith(args[0])) {
					matches.add(nick.getId());
				}
			}
			return matches;
		}
		return ImmutableList.of();
	}
}
