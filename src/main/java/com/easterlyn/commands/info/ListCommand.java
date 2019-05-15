package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.TextUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command for listing all players online in their respective groups.
 *
 * @author Jikoo
 */
public class ListCommand extends EasterlynCommand {

	public ListCommand(Easterlyn plugin) {
		super(plugin, "list");
		this.setAliases("players", "playerlist", "playing", "who");
		this.setDescription("List players online in their respective groups.");
		this.setUsage("/list");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Player senderPlayer = null;
		if (sender instanceof Player) {
			senderPlayer = (Player) sender;
		}
		int total = 0;
		Map<String, List<String>> groups = new HashMap<>();
		UserRank[] ranks = UserRank.values();
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
			for (int i = ranks.length - 1; i >= 0; --i) {
				UserRank rank = ranks[i];
				if (rank == UserRank.DANGER_DANGER_HIGH_VOLTAGE) {
					continue;
				}
				if (i == 0 || player.hasPermission(rank.getPermission())) {
					groups.compute(rank.getFriendlyName(), (k, v) -> {
						if (v == null) {
							v = new ArrayList<>();
						}
						v.add(nameBuilder.toString());
						return v;
					});
					break;
				}
			}
		}

		sender.sendMessage(String.format("%1$s%2$s+---------- %3$s%4$s%1$s/%3$s%5$s %1$sonline %2$s----------+",
				Language.getColor("good"), ChatColor.STRIKETHROUGH, Language.getColor("emphasis.good"), total, Bukkit.getMaxPlayers()));


		for (int i = ranks.length - 1; i >= 0; --i) {
			String groupName = ranks[i].getFriendlyName();

			if (i > 0 && groupName.equals(ranks[i-1].getFriendlyName())) {
				continue;
			}

			List<String> group = groups.get(groupName);
			if (group == null || group.isEmpty()) {
				continue;
			}
			sender.sendMessage(Language.getColor("good") + groupName + ": " + TextUtils.join(group.toArray(), Language.getColor("good") + ", "));
		}
		return true;
	}

}
