package co.sblock.events.packets;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.events.Events;
import co.sblock.users.OfflineUser;
import co.sblock.users.Region;
import co.sblock.users.Users;

/**
 * Causes a sleep teleport to occur.
 * 
 * @author Jikoo
 */
public class SleepTeleport extends BukkitRunnable {
	private UUID uuid;

	/**
	 * @param p the Player to teleport
	 */
	public SleepTeleport(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public void run() {
		Player player = Bukkit.getPlayer(uuid);
		if (player == null) {
			return;
		}
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		if (user == null) {
			return;
		}
		if (!Events.getInstance().getTasks().containsKey(player.getUniqueId())) {
			return;
		}
		Location location = player.getLocation();
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
		Events.getInstance().fakeWakeUp(player);
	}
}
