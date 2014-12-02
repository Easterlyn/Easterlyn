package co.sblock.commands;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

/**
 * SblockCommand for sending a bash-friendly tellraw command.
 * 
 * @author Jikoo
 */
public class RSSUpdateCommand extends SblockCommand {

	public RSSUpdateCommand() {
		super("rssupdate");
		this.setDescription("A /tellraw that's a lot easier than escaping crap for bash commands.");
		this.setUsage("/rssupdate <feed name> <url> <title>");
		this.setPermission("group.horrorterror");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 3) {
			return false;
		}
		String json = " {\"text\":\"\",\"extra\":[{\"text\":\"[\",\"color\":\"white\"},"
				+ "{\"text\":\"#\",\"color\":\"red\"},{\"text\":\"] <\",\"color\":\"white\"},"
				+ "{\"text\":\"{NAME}\",\"color\":\"yellow\",\"hoverEvent\":{\"action\":\"show_text\","
				+ "\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"RSS feed\",\"color\":\"yellow\"}]}}},"
				+ "{\"text\":\"> \",\"color\":\"white\"},{\"text\":\"Update: \",\"color\":\"yellow\"},"
				+ "{\"text\":\"{TITLE}\",\"color\":\"blue\",\"underlined\":\"true\",\"clickEvent\":"
				+ "{\"action\":\"open_url\",\"value\":\"{LINK}\"},\"hoverEvent\":{\"action\":\"show_text\","
				+ "\"value\":{\"text\":\"\",\"extra\":[{\"text\":\"Click to go: \",\"color\":\"dark_aqua\"},"
				+ "{\"text\":\"{LINK}\",\"color\":\"blue\",\"underlined\":\"true\"}]}}}]}";

		json = json.replaceAll("\\{NAME\\}", args[0].replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\""))
				.replaceAll("\\{LINK\\}", args[1].replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\""))
				.replaceAll("\\{TITLE\\}", StringUtils.join(args, ' ', 2, args.length).replaceAll("\\\\", "\\\\").replaceAll("\"", "\\\""));

		for (Player p : Bukkit.getOnlinePlayers()) {
			Bukkit.dispatchCommand(sender, "tellraw " + p.getName() + json);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
