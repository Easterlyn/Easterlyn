package co.sblock.commands.chat;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Color;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.utilities.messages.RawAnnouncer;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for manipulating the raw message announcer.
 * 
 * @author Jikoo
 */
public class HalCommand extends SblockCommand {

	public HalCommand() {
		super("hal");
		this.setDescription("Force a raw message announcement.");
		this.setUsage("/hal 1-8");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Message message;
		if (args.length > 0) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > RawAnnouncer.getInstance().getMessages().size()) {
					sender.sendMessage(Color.BAD.toString()
							+ RawAnnouncer.getInstance().getMessages().size()
							+ " announcements exist currently.");
					msgNum = RawAnnouncer.getInstance().getMessages().size();
				}
				message = RawAnnouncer.getInstance().getMessages().get(msgNum - 1);
			} catch (NumberFormatException e) {
				message = new MessageBuilder().setSender(ChatColor.DARK_RED + "Lil Hal")
						.setChannel(ChannelManager.getChannelManager().getChannel("#"))
						.setNameClick("/report ").setMessage(StringUtils.join(args, ' '))
						.toMessage();
			}
		} else {
			message = RawAnnouncer.getInstance().getMessages().get((int) (Math.random() * RawAnnouncer.getInstance().getMessages().size()));
		}
		message.send(Bukkit.getOnlinePlayers());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
