package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ListCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("list|ls")
	@Description("{@@sink.module.list.description}")
	@CommandPermission("easterlyn.command.list")
	@Syntax("")
	@CommandCompletion("")
	public void list(BukkitCommandIssuer issuer) {
		Player sender = issuer.getPlayer();
		UserRank[] ranks = UserRank.values();
		Multimap<String, User> groupedUsers = HashMultimap.create();
		int total = 0;

		for (Player player : Bukkit.getOnlinePlayers()) {
			if (sender != null && !sender.canSee(player)) {
				continue;
			}

			++total;

			for (int i = ranks.length - 1; i >= 0; --i) {
				UserRank rank = ranks[i];
				if (i == 0 || player.hasPermission(rank.getPermission())) {
					groupedUsers.put(rank.getFriendlyName(), core.getUserManager().getUser(player.getUniqueId()));
					break;
				}
			}
		}

		issuer.sendInfo(MessageKey.of("sink.module.list.header"), "{value}", String.valueOf(total),
				"{max}", String.valueOf(Bukkit.getMaxPlayers()));

		for (int i = ranks.length - 1; i >= 0; --i) {
			String groupName = ranks[i].getFriendlyName();

			if (i > 0 && groupName.equals(ranks[i-1].getFriendlyName())
					|| !groupedUsers.containsKey(groupName)) {
				continue;
			}

			Collection<User> users = groupedUsers.get(groupName);

			List<BaseComponent> components = new ArrayList<>(users.size() * 2 + 2);
			TextComponent component = new TextComponent(groupName);
			component.setColor(ranks[i].getColor().asBungee());
			components.add(component);
			component = new TextComponent(": ");
			component.setColor(ChatColor.YELLOW);
			components.add(component);

			TextComponent separator = new TextComponent(", ");
			separator.setColor(ChatColor.YELLOW);

			users.forEach(user -> {
				components.add(user.getMention());
				components.add(separator);
			});
			components.remove(components.size() -1);

			issuer.getIssuer().spigot().sendMessage(components.toArray(new BaseComponent[0]));
		}

	}

}
