package co.sblock.events;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock;
import co.sblock.users.Users;

/**
 * Utility for managing nametag visibility for invisible players
 * 
 * @author Jikoo
 */
public class InvisibilityManager {

	private final HashMap<UUID, BukkitTask> tasks;

	public InvisibilityManager() {
		tasks = new HashMap<>();
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
		}.runTaskLater(Sblock.getInstance(), 1L);
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
		Team team = player.getScoreboard().getPlayerTeam(player);
		if (team == null) {
			Users.team(player);
			team = player.getScoreboard().getPlayerTeam(player);
		}
		team.setNameTagVisibility(NameTagVisibility.NEVER);
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
		}.runTaskLater(Sblock.getInstance(), duration));
	}

	private void setVisible(Player player) {
		Team team = player.getScoreboard().getPlayerTeam(player);
		if (team == null) {
			Users.team(player);
			team = player.getScoreboard().getPlayerTeam(player);
		}
		team.setNameTagVisibility(NameTagVisibility.ALWAYS);
	}
}
