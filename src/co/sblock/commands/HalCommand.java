package co.sblock.commands;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ColorDef;
import co.sblock.utilities.Log;
import co.sblock.utilities.rawmessages.JSONUtil;
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
		this.setPermission("group.denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		RawAnnouncer.AnnouncementMessage msg;
		if (args.length == 1) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > RawAnnouncer.getInstance().getMessages().size()) {
					sender.sendMessage(ChatColor.RED.toString() + RawAnnouncer.getInstance().getMessages().size() + " announcements exist currently.");
					msgNum = RawAnnouncer.getInstance().getMessages().size();
				}
				msg = RawAnnouncer.getInstance().getMessages().get(msgNum - 1);
			} catch (NumberFormatException e) {
				msg = RawAnnouncer.getInstance().new AnnouncementMessage(JSONUtil.getWrappedJSON(JSONUtil.toJSONElements(ColorDef.HAL + args[0], true, null)), ColorDef.HAL + args[0]);
			}
		} else if (args.length > 0) {
			String joined = ColorDef.HAL + StringUtils.join(args, ' ');
			msg = RawAnnouncer.getInstance().new AnnouncementMessage(JSONUtil.getWrappedJSON(JSONUtil.toJSONElements(joined, true, null)), joined);
		} else {
			msg = RawAnnouncer.getInstance().getMessages().get((int) (Math.random() * RawAnnouncer.getInstance().getMessages().size()));
		}
		Log.anonymousInfo(msg.getConsole());
		for (Player p : Bukkit.getOnlinePlayers()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "tellraw " + p.getName() + ' ' + msg.getJSON());
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
