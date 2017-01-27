package com.easterlyn.events.packets;

import java.util.UUID;

import com.easterlyn.Easterlyn;
import com.easterlyn.micromodules.DreamTeleport;
import com.easterlyn.users.Region;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Causes a sleep teleport to occur.
 * 
 * @author Jikoo
 */
public class SleepTeleport extends BukkitRunnable {

	private final DreamTeleport dream;
	private final Users users;
	private final UUID uuid;

	/**
	 * @param p the Player to teleport
	 */
	public SleepTeleport(Easterlyn plugin, UUID uuid) {
		this.dream = plugin.getModule(DreamTeleport.class);
		this.users = plugin.getModule(Users.class);
		this.uuid = uuid;
	}

	@Override
	public void run() {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null || !dream.getSleepTasks().containsKey(player.getUniqueId())) {
			return;
		}
		User user = users.getUser(player.getUniqueId());
		Location location = player.getLocation().clone();
		if (user.getPreviousLocation().getWorld().getName().equals(player.getWorld().getName())) {
			if (user.getCurrentRegion().isDream()) {
				player.teleport(Bukkit.getWorld("Earth").getSpawnLocation());
			} else {
				player.teleport(Bukkit.getWorld(user.getDreamPlanet().getWorldName()).getSpawnLocation());
			}
		} else {
			if (Region.getRegion(user.getPreviousLocation().getWorld().getName()).isDream() && user.getCurrentRegion().isDream()) {
				player.teleport(Bukkit.getWorld("Earth").getSpawnLocation());
			} else if (!Region.getRegion(user.getPreviousLocation().getWorld().getName()).isDream() && !user.getCurrentRegion().isDream()) {
				player.teleport(Bukkit.getWorld(user.getDreamPlanet().getWorldName()).getSpawnLocation());
			} else {
				player.teleport(user.getPreviousLocation());
			}
		}
		user.setPreviousLocation(location);
		dream.fakeWakeUp(player);
	}

}
