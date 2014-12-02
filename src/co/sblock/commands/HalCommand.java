package co.sblock.commands;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.UserManager;
import co.sblock.utilities.Log;
import co.sblock.utilities.rawmessages.MessageElement;
import co.sblock.utilities.rawmessages.MessageHalement;
import co.sblock.utilities.rawmessages.RawAnnouncer;

/**
 * SblockCommand for manipulating the raw message announcer.
 * 
 * @author Jikoo
 */
public class HalCommand extends SblockCommand {

	public HalCommand() {
		super("hal");
		this.setDescription("Force a raw message announcement or talk as Hal.");
		this.setUsage("/hal [1-9|text]");
		this.setPermission("group.horrorterror");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		MessageElement msg;
		if (args.length == 1) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > RawAnnouncer.getAnnouncer().getMessages().size()) {
					sender.sendMessage(ChatColor.RED.toString() + RawAnnouncer.getAnnouncer().getMessages().size() + " announcements exist currently.");
					msgNum = RawAnnouncer.getAnnouncer().getMessages().size();
				}
				msg = RawAnnouncer.getAnnouncer().getMessages().get(msgNum - 1);
			} catch (NumberFormatException e) {
				msg = new MessageHalement(args[0]);
			}
		} else if (args.length > 0) {
			msg = new MessageHalement(StringUtils.join(args, ' '));
		} else {
			msg = RawAnnouncer.getAnnouncer().getMessages().get((int) (Math.random() * RawAnnouncer.getAnnouncer().getMessages().size()));
		}
		Log.anonymousInfo(msg.getConsoleFriendly());
		String announcement = msg.toString();
		for (Player p : Bukkit.getOnlinePlayers()) {
			UserManager.getUser(p.getUniqueId()).rawHighlight(announcement);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
