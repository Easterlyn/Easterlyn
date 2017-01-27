package com.easterlyn.events;

import java.util.HashMap;
import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.users.Users;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;
import org.bukkit.scoreboard.Team.Option;
import org.bukkit.scoreboard.Team.OptionStatus;

/**
 * Utility for managing nametag visibility for invisible players
 * 
 * @author Jikoo
 */
public class InvisibilityManager {

	private final HashMap<UUID, BukkitTask> tasks;
	private final Easterlyn plugin;

	public InvisibilityManager(Easterlyn plugin) {
		tasks = new HashMap<>();
		this.plugin = plugin;
	}

	/**
	 * Update the Player specified's nametag on a tick delay. Allows per-gamemode stuff to finish beforehand.
	 * 
	 * @param player the Player to update
	 */
	public void lazyVisibilityUpdate(Player player) {
		final UUID uuid = player.getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					return;
				}
				updateVisibility(player);
			}
		}.runTaskLater(plugin, 1L);
	}

	public void updateVisibility(Player player) {
		if (tasks.containsKey(player.getUniqueId())) {
			tasks.remove(player.getUniqueId()).cancel();
		}
		if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
			setVisible(player);
			return;
		}
		for (PotionEffect potion : player.getActivePotionEffects()) {
			if (potion.getType().equals(PotionEffectType.INVISIBILITY)) {
				setInvisible(player, potion.getDuration());
				break;
			}
		}
	}

	private void setInvisible(Player player, int duration) {
		Team team = player.getScoreboard().getEntryTeam(player.getName());
		if (team == null) {
			Users.team(player, null);
			team = player.getScoreboard().getEntryTeam(player.getName());
		}
		if (team != null) {
			team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.NEVER);
		}
		final UUID uuid = player.getUniqueId();
		tasks.put(uuid, new BukkitRunnable() {
			@Override
			public void run() {
				tasks.remove(uuid);
				Player player = Bukkit.getPlayer(uuid);
				if (player != null) {
					updateVisibility(player);
				}
			}
		}.runTaskLater(plugin, duration));
	}

	private void setVisible(Player player) {
		Team team = player.getScoreboard().getEntryTeam(player.getName());
		if (team == null) {
			Users.team(player, null);
			team = player.getScoreboard().getEntryTeam(player.getName());
		}
		if (team != null) {
			team.setOption(Option.NAME_TAG_VISIBILITY, OptionStatus.ALWAYS);
		}
	}

}
