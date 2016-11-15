package co.sblock.commands.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import net.md_5.bungee.api.ChatColor;

/**
 * Command for listing all players online in their respective groups.
 * 
 * @author Jikoo
 */
public class ListCommand extends SblockCommand {

	private final String[] groupNames;
	private final Map<String, List<String>> groups;

	public ListCommand(Sblock plugin) {
		super(plugin, "list");
		this.setAliases("players", "playerlist", "playing", "who");
		this.setDescription("List players online in their respective groups.");
		this.setUsage("/list");

		groupNames = new String[] {"Horrorterror", "Denizen", "Felt", "Helper", "Donator", "Godtier", "Hero"};
		groups = new HashMap<>(groupNames.length);
		for (String groupName : groupNames) {
			groups.put(groupName, new ArrayList<>());
		}
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Player senderPlayer = null;
		if (sender instanceof Player) {
			senderPlayer = (Player) sender;
		}
		int total = 0;
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (senderPlayer != null && !senderPlayer.canSee(player)) {
				continue;
			}
			total++;

			StringBuilder nameBuilder = new StringBuilder();
			Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(player.getName());
			if (team != null && team.getPrefix() != null) {
				nameBuilder.append(team.getPrefix());
			}
			if (player.getDisplayName() != null) {
				nameBuilder.append(player.getDisplayName());
			} else {
				nameBuilder.append(player.getName());
			}
			for (String groupName : groupNames) {
				if (groupName.equals("Hero") || player.hasPermission("sblock.group." + groupName.toLowerCase())) {
					groups.get(groupName).add(nameBuilder.toString());
					break;
				}
			}
		}

		sender.sendMessage(String.format("%1$s%2$s+---------- %3$s%4$s%1$s/%3$s%5$s %1$sonline %2$s----------+",
				Language.getColor("good"), ChatColor.STRIKETHROUGH, Language.getColor("emphasis.good"), total, Bukkit.getMaxPlayers()));

		for (String groupName : groupNames) {
			List<String> group = groups.get(groupName);
			if (group.isEmpty()) {
				continue;
			}
			sender.sendMessage(Language.getColor("good") + groupName + ": " + StringUtils.join(group, Language.getColor("good") + ", "));
			// Clear list for next use
			group.clear();
		}
		return true;
	}

}
