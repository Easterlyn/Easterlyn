package co.sblock.commands.chat;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.RawAnnouncer;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for manipulating the raw message announcer.
 * 
 * @author Jikoo
 */
public class HalCommand extends SblockCommand {

	private final RawAnnouncer announcer;
	private final ChannelManager manager;

	public HalCommand(Sblock plugin) {
		super(plugin, "hal");
		this.setDescription("Force a raw message announcement.");
		this.setUsage("/hal 1-10");
		this.setPermissionLevel("denizen");
		this.announcer = plugin.getModule(RawAnnouncer.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Message message;
		if (args.length > 0) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > announcer.getMessages().size()) {
					sender.sendMessage(Color.BAD.toString()
							+ announcer.getMessages().size()
							+ " announcements exist currently.");
					msgNum = announcer.getMessages().size();
				}
				message = announcer.getMessages().get(msgNum - 1);
			} catch (NumberFormatException e) {
				message = new MessageBuilder((Sblock) getPlugin()).setSender(ChatColor.DARK_RED + ((Sblock) getPlugin()).getBotName())
						.setChannel(manager.getChannel("#")).setNameClick("/report ")
						.setMessage(StringUtils.join(args, ' ')).toMessage();
			}
		} else {
			message = announcer.getMessages().get((int) (Math.random() * announcer.getMessages().size()));
		}
		message.send(Bukkit.getOnlinePlayers());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
