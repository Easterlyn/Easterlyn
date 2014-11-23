package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.SblockChat;
import co.sblock.chat.channel.Channel;

/**
 * SblockCommand for triggering a MegaHal response.
 * 
 * @author Jikoo
 */
public class MegaHalCommand extends SblockCommand {

	public MegaHalCommand() {
		super("megahal");
		this.setDescription("Trigger a Lil Hal response.");
		this.setUsage("/megahal [channel] [seedword]");
		this.setPermission("group.felt");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		Channel target;
		if (args.length >= 1) {
			target = ChannelManager.getChannelManager().getChannel(args[0]);
			if (target == null) {
				sender.sendMessage(ChatColor.RED + "Invalid channel: " + args[0]);
				return true;
			}
		} else {
			target = ChannelManager.getChannelManager().getChannel("#");
		}
		SblockChat.getChat().getHal().triggerResponse(target, args.length > 1 ? StringUtils.join(args, ' ', 1, args.length) : null, false);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length != 1) {
			return super.tabComplete(sender, alias, args);
		} else {
			ArrayList<String> matches = new ArrayList<>();
			for (String channel : ChannelManager.getChannelManager().getChannelList().keySet()) {
				if (StringUtil.startsWithIgnoreCase(channel, args[0])) {
					matches.add(channel);
				}
			}
			return matches;
		}
	}
}
