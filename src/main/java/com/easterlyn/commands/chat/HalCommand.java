package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.chat.message.Message;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.micromodules.RawAnnouncer;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * EasterlynCommand for manipulating the raw message announcer.
 * 
 * @author Jikoo
 */
public class HalCommand extends EasterlynCommand {

	private final Chat chat;
	private final RawAnnouncer announcer;

	public HalCommand(Easterlyn plugin) {
		super(plugin, "hal");
		this.setDescription("Force a raw message announcement.");
		this.setPermissionLevel(UserRank.ADMIN);
		this.setUsage("/hal 1-10");
		this.chat = plugin.getModule(Chat.class);
		this.announcer = plugin.getModule(RawAnnouncer.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Message message;
		if (!announcer.isEnabled()) {
			message = chat.getHalBase().setMessage(Language.getColor("bot_text") + TextUtils.join(args, ' ')).toMessage();
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
				message = chat.getHalBase().setMessage(Language.getColor("bot_text") + TextUtils.join(args, ' ')).toMessage();
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
