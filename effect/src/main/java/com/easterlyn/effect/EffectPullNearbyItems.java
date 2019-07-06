package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Cause all nearby items to be sucked to the player.
 *
 * @author Jikoo
 */
public class EffectPullNearbyItems extends Effect {

	public EffectPullNearbyItems(EasterlynEffects plugin) {
		super(plugin, "Vacuum", EquipmentSlots.TOOL, 300, 3, 3);
	}

	@Override
	public void applyEffect(LivingEntity entity, int level, Event event) {
		final double radius = level * 1.5;
		final UUID uuid = entity.getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					// Logged out
					return;
				}

				for (Entity near : player.getNearbyEntities(radius, radius, radius)) {
					if (!(near instanceof Item)) {
						continue;
					}
					near.getWorld().spawnParticle(Particle.ENCHANTMENT_TABLE, near.getLocation(), 1);
					near.teleport(player);
					player.playSound(near.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 0.1F, 1.5F);
					Item item = (Item) near;
					if (item.getPickupDelay() < 1000) {
						item.setPickupDelay(0);
					}
				}
			}
		}.runTask(getPlugin());
	}

}
