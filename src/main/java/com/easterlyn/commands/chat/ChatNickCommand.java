package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NickChannel;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.Normalizer;
import java.util.List;

/**
 * Command for changing nickname in a channel.
 * 
 * @author Jikoo
 */
public class ChatNickCommand extends EasterlynCommand {

	// TODO convert to lang, allow setting of nick for softmuted, just don't announce
	private final Chat chat;
	private final Users users;

	public ChatNickCommand(Easterlyn plugin) {
		super(plugin, "nick");
		setDescription(Language.getColor("command") + "/nick remove|list|<nick choice>"
				+ Language.getColor("good") + ": Set a nick in a Nick/RP channel.");
		setUsage("/nick name");
		this.chat = plugin.getModule(Chat.class);
		this.users = plugin.getModule(Users.class);
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
		for (char character : Normalizer.normalize(TextUtils.join(args, ' '),
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

		String nickname = ChatColor.translateAlternateColorCodes('&', sb.toString());
		String cleanName = ChatColor.stripColor(nickname);

		User nickOwner = nickChannel.getNickOwner(cleanName);

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
		return ImmutableList.of();
	}
}
