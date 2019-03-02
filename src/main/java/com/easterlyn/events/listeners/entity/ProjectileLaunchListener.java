package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.Cooldowns;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class ProjectileLaunchListener extends EasterlynListener {

	private final Cooldowns cooldowns;

	public ProjectileLaunchListener(Easterlyn plugin) {
		super(plugin);
		this.cooldowns = plugin.getModule(Cooldowns.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof ThrownExpBottle) || !(event.getEntity().getShooter() instanceof Player)) {
			return;
		}

		event.setCancelled(cooldowns.getRemainder((Player) event.getEntity().getShooter(), "ExpBottleUse") > 0);
	}

}
