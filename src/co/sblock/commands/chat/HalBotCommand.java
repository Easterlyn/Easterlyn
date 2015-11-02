package co.sblock.commands.chat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.channel.Channel;
import co.sblock.commands.SblockCommand;
import co.sblock.users.Users;

/**
 * SblockCommand for triggering a Hal AI response.
 * 
 * @author Jikoo
 */
public class HalBotCommand extends SblockCommand {

	public HalBotCommand() {
		super("halbot");
		this.setDescription("Trigger a Lil Hal response.");
		this.setUsage("/megahal [channel] <seed mesage>");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		boolean specifiedTarget = false;
		Channel target = ChannelManager.getChannelManager().getChannel(args[0]);
		if (target != null) {
			specifiedTarget = true;
		} else {
			if (sender instanceof Player) {
				target = Users.getGuaranteedUser(((Player) sender).getUniqueId()).getCurrentChannel();
				if (target == null) {
					return true;
				}
			} else {
				target = ChannelManager.getChannelManager().getChannel("#");
			}
			return true;
		}
		if (args.length == 1 && specifiedTarget) {
			return false;
		}
		Chat.getChat().getHal().triggerResponse(target,
				StringUtils.join(args, ' ', specifiedTarget ? 1 : 0, args.length));
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
