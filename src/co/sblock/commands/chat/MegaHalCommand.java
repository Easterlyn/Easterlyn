package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.commands.SblockCommand;

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
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Channel target;
		if (args.length >= 1) {
			target = ChannelManager.getChannelManager().getChannel(args[0]);
			if (target == null) {
				sender.sendMessage(Color.BAD + "Invalid channel: " + args[0]);
				return true;
			}
		} else {
			target = ChannelManager.getChannelManager().getChannel("#");
		}
		Chat.getChat().getHal().triggerResponse(target, args.length > 1 ? StringUtils.join(args, ' ', 1, args.length) : null, false);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
 		if (!sender.hasPermission(this.getPermission()) || args.length > 2) {
			return ImmutableList.of();
		}
		if (args.length == 2) {
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
