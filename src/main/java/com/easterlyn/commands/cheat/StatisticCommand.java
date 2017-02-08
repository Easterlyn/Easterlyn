package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * EasterlynCommand for editing a Player's Statistics.
 * 
 * @author Jikoo
 */
public class StatisticCommand extends EasterlynCommand {

	public StatisticCommand(Easterlyn plugin) {
		super(plugin, "stat");
		this.setDescription("Statistically, you can use this command.");
		this.setPermissionLevel(UserRank.ADMIN);
		this.setUsage("/stat <target> <stat> [integer]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length < 1) {
			return false;
		}

		Statistic stat = null;
		Material statMaterial = null;
		EntityType statEntity = null;
		Integer setStatTo = null;
		if (args.length > 2) {
			try {
				setStatTo = Integer.valueOf(args[2]);
			} catch (NumberFormatException e) {
				return false;
			}
		}

		// Messy crap ensues. Too lazy for robust handling.
		// Proper format is STATISTIC, STATISTIC:ENTITY, or STATISTIC:MATERIAL
		if (args.length > 1) {
			String[] statString = args[1].split(":");
			try {
				stat = Statistic.valueOf(statString[0].toUpperCase());
				if (statString.length > 1) {
					try {
						statEntity = EntityType.valueOf(statString[0].toUpperCase());
					} catch (IllegalArgumentException e) {}
					try {
						statMaterial = Material.valueOf(statString[0].toUpperCase());
					} catch (IllegalArgumentException e1) {}
				}
			} catch (IllegalArgumentException e) {}
		}

		if (stat == null) {
			StringBuilder sb = new StringBuilder("Valid stats: ");
			for (Statistic statistic : Statistic.values()) {
				sb.append(statistic.name()).append(' ');
			}
			sender.sendMessage(sb.toString());
			return false;
		}

		Player player = Bukkit.getPlayer(args[0]);
		if (player == null) {
			sender.sendMessage("Invalid user " + args[0]);
			return true;
		}

		StringBuilder sb = new StringBuilder(stat.name());
		if (statEntity != null) {
			sb.append(':').append(statEntity.name());
		} else if (statMaterial != null) {
			sb.append(':').append(statMaterial.name());
		}
		sb.append(" for ").append(player.getName()).append(" is ");

		if (statEntity != null) {
			sb.append(player.getStatistic(stat, statEntity));
		} else if (statMaterial != null) {
			sb.append(player.getStatistic(stat, statMaterial));
		} else {
			sb.append(player.getStatistic(stat));
		}

		sender.sendMessage(sb.toString());

		if (setStatTo != null) {
			if (statEntity != null) {
				player.setStatistic(stat, statEntity, setStatTo);
			} else if (statMaterial != null) {
				player.setStatistic(stat, statMaterial, setStatTo);
			} else {
				player.setStatistic(stat, setStatTo);
			}
			sender.sendMessage("Set to " + setStatTo);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		// CHAT: tab-complete
		return ImmutableList.of();
	}
}
