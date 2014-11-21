package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

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

	// TODO tab complete channel for 0, perhaps amusing ones for 1?
}
