package com.easterlyn.kitchensink.listener;

import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import com.easterlyn.util.ExperienceUtil;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PVPKeepInventory implements Listener {

	private final String key = "lastPVPDamage";
	private EasterlynCore core;

	public PVPKeepInventory(EasterlynCore core) {
		this.core = core;
	}

	@EventHandler
	public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
		if (!(event.getEntity() instanceof Player)) {
			return;
		}

		boolean playerDamage = false;
		if (event.getDamager() instanceof Player) {
			playerDamage = true;
		} else if (event.getDamager() instanceof Firework) {
			playerDamage = ((Firework) event.getDamager()).getSpawningEntity() != null;
		} else if (event.getDamager() instanceof Projectile) {
			playerDamage = ((Projectile) event.getDamager()).getShooter() instanceof Player;
		}

		if (!playerDamage) {
			return;
		}

		User user = core.getUserManager().getUser(event.getEntity().getUniqueId());
		user.getTemporaryStorage().put(key, System.currentTimeMillis());
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		User user = core.getUserManager().getUser(event.getEntity().getUniqueId());
		Object object = user.getTemporaryStorage().get(key);
		if (!(object instanceof Long)) {
			return;
		}
		long timestamp = (long) object;
		if (timestamp < System.currentTimeMillis() - 6000L) {
			return;
		}
		event.setDroppedExp(ExperienceUtil.getExp(event.getEntity()));
		int dropped = ExperienceUtil.getExp(event.getEntity()) / 10;
		if (dropped > 30) {
			dropped = 30;
		}
		event.setDroppedExp(dropped);
		ExperienceUtil.changeExp(event.getEntity(), -dropped);
		event.setKeepLevel(true);
		event.setKeepInventory(true);
	}

}
