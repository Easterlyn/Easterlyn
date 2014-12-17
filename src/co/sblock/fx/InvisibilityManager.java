package co.sblock.fx;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Team;

import co.sblock.Sblock;
import co.sblock.users.Users;

/**
 * Utility for managing nametag visibility for invisible players
 * 
 * @author Jikoo
 */
public class InvisibilityManager {

	private HashMap<UUID, BukkitTask> tasks;

	public InvisibilityManager() {
		tasks = new HashMap<>();
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
			if (potion.getType() == PotionEffectType.INVISIBILITY) {
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
		try {
			Field f = team.getClass().getDeclaredField("team");
			f.setAccessible(true);
			net.minecraft.server.v1_8_R1.ScoreboardTeam nmsTeam = (net.minecraft.server.v1_8_R1.ScoreboardTeam) f.get(team);
			nmsTeam.a(net.minecraft.server.v1_8_R1.EnumNameTagVisibility.NEVER);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			FXManager.getInstance().getLogger().warning("Unable to set nametag visibility!");
			FXManager.getInstance().getLogger().err(e);
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
		}.runTaskLater(Sblock.getInstance(), duration));
	}

	private void setVisible(Player player) {
		Team team = player.getScoreboard().getPlayerTeam(player);
		if (team == null) {
			Users.team(player);
			team = player.getScoreboard().getPlayerTeam(player);
		}
		try {
			Field f = team.getClass().getDeclaredField("team");
			f.setAccessible(true);
			net.minecraft.server.v1_8_R1.ScoreboardTeam nmsTeam = (net.minecraft.server.v1_8_R1.ScoreboardTeam) f.get(team);
			nmsTeam.a(net.minecraft.server.v1_8_R1.EnumNameTagVisibility.ALWAYS);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			FXManager.getInstance().getLogger().warning("Unable to set nametag visibility!");
			FXManager.getInstance().getLogger().err(e);
		}
	}
}
