package co.sblock.commands.admin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.BanEntry;
import org.bukkit.BanList;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for undoing a dual IP and UUID ban.
 * 
 * @author Jikoo
 */
public class UnBanCommand extends SblockCommand {

	private final Chat chat;

	public UnBanCommand(Sblock plugin) {
		super(plugin, "unban");
		this.chat = plugin.getModule(Chat.class);
		this.setAliases("unsban", "pardon", "unbanip", "pardonip");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		BanList bans = Bukkit.getBanList(Type.IP);
		BanList pbans = Bukkit.getBanList(Type.NAME);
		if (bans.isBanned(args[0])) {
			pbans.pardon(bans.getBanEntry(args[0]).getReason()
					.replaceAll(".*<name=(\\w{1,16}+)>.*", "$1"));
			bans.pardon(args[0]);
		} else if (pbans.isBanned(args[0])) {
			bans.pardon(pbans.getBanEntry(args[0]).getReason()
					.replaceAll(".*<ip=(([0-9]{1,3}\\.){3}[0-9]{1,3})>.*", "$1"));
			pbans.pardon(args[0]);
		} else  {
			sender.sendMessage(getLang().getValue("command.unban.unfound").replace("{TARGET}", args[0]));
			return true;
		}
		if (args[0].contains(".")) {
			sender.sendMessage(getLang().getValue("command.unban.possibleIP").replace("{TARGET}", args[0]));
		} else {
			chat.getHalBase().setMessage(getLang().getValue("command.unban.unban").replace("{TARGET}", args[0]))
					.toMessage().send(Bukkit.getOnlinePlayers(), false);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length > 1 || !sender.hasPermission(this.getPermission())) {
			return ImmutableList.of();
		}
		ArrayList<String> matches = new ArrayList<>();
		for (BanEntry ban : Bukkit.getBanList(Type.NAME).getBanEntries()) {
			if (StringUtil.startsWithIgnoreCase(ban.getTarget(), ban.getTarget())) {
				matches.add(ban.getTarget());
			}
		}
		for (BanEntry ban : Bukkit.getBanList(Type.IP).getBanEntries()) {
			if (StringUtil.startsWithIgnoreCase(ban.getTarget(), ban.getTarget())) {
				matches.add(ban.getTarget());
			}
		}
		return matches;
	}
}
