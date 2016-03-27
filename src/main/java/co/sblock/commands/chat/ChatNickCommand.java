package co.sblock.commands.chat;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Language;
import co.sblock.chat.channel.CanonNick;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.channel.NickChannel;
import co.sblock.chat.channel.RPChannel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.User;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for changing nickname in a channel.
 * 
 * @author Jikoo
 */
public class ChatNickCommand extends SblockCommand {

	// TODO convert to lang, allow setting of nick for softmuted, just don't announce
	private final Chat chat;
	private final Users users;

	public ChatNickCommand(Sblock plugin) {
		super(plugin, "nick");
		this.users = plugin.getModule(Users.class);
		setDescription(Language.getColor("command") + "/nick remove|list|<nick choice>"
				+ Language.getColor("good") + ": Set a nick in a Nick/RP channel.");
		setUsage("/nick name");
		this.chat = plugin.getModule(Chat.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		if (chat.testForMute(player)) {
			return true;
		}
		User user = users.getUser(player.getUniqueId());
		Channel channel = user.getCurrentChannel();
		if (channel == null) {
			user.sendMessage(getLang().getValue("chat.error.noCurrentChannel"));
			return true;
		}
		if (args.length == 0) {
			return false;
		}
		if (!(channel instanceof NickChannel)) {
			user.sendMessage(getLang().getValue("chat.error.unsupportedOperation").replace("{CHANNEL}", args[0]));
			return true;
		}
		if (args[0].equalsIgnoreCase("list")) {
			if (channel instanceof RPChannel) {
				getLang();
				StringBuilder sb = new StringBuilder(Language.getColor("good").toString()).append("Nicks: ");
				for (CanonNick n : CanonNick.values()) {
					if (n != CanonNick.SERKITFEATURE) {
						sb.append(Language.getColor("emphasis.good")).append(n.getId());
						sb.append(Language.getColor("good")).append(", ");
					}
				}
				user.sendMessage(sb.substring(0, sb.length() - 4).toString());
				return true;
			}
			user.sendMessage(Language.getColor("good") + "You can use any nick you want in a nick channel.");
			return true;
		}
		NickChannel nickChannel = (NickChannel) channel;
		if (args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("off")) {
			String oldName = nickChannel.removeNick(user);
			if (oldName != null) {
				nickChannel.sendMessage(getLang().getValue("chat.channel.denick")
						.replace("{CHANNEL}", nickChannel.getName())
						.replace("{PLAYER}", user.getDisplayName()).replace("{NICK}", oldName));
			} else {
				sender.sendMessage(Language.getColor("bad") + "You do not have a nick currently.");
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
			sender.sendMessage(Language.getColor("bad")
					+ "Nicks must be 1+ characters long when stripped of non-ASCII characters.");
			return true;
		}
		String nickname = sb.toString();
		String cleanName = null;
		if (nickChannel instanceof RPChannel) {
			CanonNick canonNick = CanonNick.getNick(nickname);
			if (canonNick == null) {
				sender.sendMessage(getLang().getValue("chat.error.nickNotCanon").replace("{NICK}", nickname));
				return true;
			}
			nickname = canonNick.name();
			cleanName = canonNick.getDisplayName();
		} else {
			nickname = ChatColor.translateAlternateColorCodes('&', nickname);
			cleanName = ChatColor.stripColor(nickname);
		}
		User nickOwner = nickChannel.getNickOwner(nickname);
		if (nickOwner != null) {
			if (!nickOwner.getUUID().equals(user.getUUID())) {
				sender.sendMessage(getLang().getValue("chat.error.nickTaken").replace("{NICK}", cleanName));
				return true;
			}
			// Only send command sender, not whole channel, a message when changing to the same nick (or an RP variant)
			sender.sendMessage(getLang().getValue("chat.channel.nick")
					.replace("{CHANNEL}", nickChannel.getName())
					.replace("{PLAYER}", user.getDisplayName()).replace("{NICK}", cleanName));
		} else {
			nickChannel.sendMessage(getLang().getValue("chat.channel.nick")
					.replace("{CHANNEL}", nickChannel.getName())
					.replace("{PLAYER}", user.getDisplayName()).replace("{NICK}", cleanName));
		}
		nickChannel.setNick(user, ChatColor.translateAlternateColorCodes('&', nickname));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || args.length > 1) {
			return ImmutableList.of();
		}
		if (users.getUser(((Player) sender).getUniqueId()).getCurrentChannel() instanceof RPChannel) {
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
