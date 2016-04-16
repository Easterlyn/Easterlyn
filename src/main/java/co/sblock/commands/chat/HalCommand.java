package co.sblock.commands.chat;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Language;
import co.sblock.chat.message.Message;
import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.RawAnnouncer;

/**
 * SblockCommand for manipulating the raw message announcer.
 * 
 * @author Jikoo
 */
public class HalCommand extends SblockCommand {

	private final Chat chat;
	private final RawAnnouncer announcer;

	public HalCommand(Sblock plugin) {
		super(plugin, "hal");
		this.setDescription("Force a raw message announcement.");
		this.setUsage("/hal 1-10");
		this.setPermissionLevel("denizen");
		this.chat = plugin.getModule(Chat.class);
		this.announcer = plugin.getModule(RawAnnouncer.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Message message;
		if (!announcer.isEnabled()) {
			message = chat.getHalBase().setMessage(Language.getColor("bot_text") + StringUtils.join(args, ' ')).toMessage();
		} else if (args.length > 0) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > announcer.getMessages().size()) {
					sender.sendMessage(Language.getColor("bad").toString()
							+ announcer.getMessages().size()
							+ " announcements exist currently.");
					msgNum = announcer.getMessages().size();
				}
				message = announcer.getMessages().get(msgNum - 1);
			} catch (NumberFormatException e) {
				message = chat.getHalBase().setMessage(Language.getColor("bot_text") + StringUtils.join(args, ' ')).toMessage();
			}
		} else {
			message = announcer.getMessages().get(ThreadLocalRandom.current().nextInt(announcer.getMessages().size()));
		}
		message.send(Bukkit.getOnlinePlayers());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
