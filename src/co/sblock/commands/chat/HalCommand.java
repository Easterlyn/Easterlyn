package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;
import co.sblock.utilities.Log;
import co.sblock.utilities.rawmessages.RawAnnouncer;

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
		// TODO transition to Message system
		RawAnnouncer.AnnouncementMessage msg;
		if (args.length > 0) {
			try {
				int msgNum = Integer.valueOf(args[0]);
				if (msgNum > RawAnnouncer.getInstance().getMessages().size()) {
					sender.sendMessage(ChatColor.RED.toString() + RawAnnouncer.getInstance().getMessages().size() + " announcements exist currently.");
					msgNum = RawAnnouncer.getInstance().getMessages().size();
				}
				msg = RawAnnouncer.getInstance().getMessages().get(msgNum - 1);
			} catch (NumberFormatException e) {
				return false;
			}
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
