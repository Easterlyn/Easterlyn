package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.Effect;

/**
 * Cause all nearby items to be sucked to the player.
 * 
 * @author Jikoo
 */
public class EffectVacuum extends Effect implements BehaviorActive {

	public EffectVacuum(Sblock plugin) {
		super(plugin, 300, 1, 3, "Vacuum");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockBreakEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
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
					near.getWorld().playEffect(near.getLocation(), org.bukkit.Effect.FLYING_GLYPH, 0);
					near.teleport(player);
					player.playSound(near.getLocation(), Sound.ENDERDRAGON_WINGS, 0.2F, 1.5F);
					Item item = (Item) near;
					if (item.getPickupDelay() < 1000) {
						item.setPickupDelay(0);
					}
				}
			}
		}.runTask(getPlugin());
	}

}
