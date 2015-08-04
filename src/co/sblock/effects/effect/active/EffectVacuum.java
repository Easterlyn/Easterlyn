package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;

/**
 * Cause all nearby items to be sucked to the player.
 * 
 * @author Jikoo
 */
public class EffectVacuum extends Effect implements EffectBehaviorActive {

	public EffectVacuum() {
		super(300, 1, 5, "Vacuum");
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockBreakEvent.class);
	}

	@Override
	public void handleEvent(Event event, Player player, int level) {
		final double radius = level * 1.5;
		final UUID uuid = player.getUniqueId();
		new BukkitRunnable() {
			@Override
			public void run() {
				Player player = Bukkit.getPlayer(uuid);
				if (player == null) {
					// Logged out
					return;
				}

				for (Entity e : player.getNearbyEntities(radius, radius, radius)) {
					if (e.getType() == EntityType.DROPPED_ITEM) {
						e.getWorld().playEffect(e.getLocation(), org.bukkit.Effect.FLYING_GLYPH, 0);
						e.teleport(player);
						player.playSound(e.getLocation(), Sound.ENDERDRAGON_WINGS, 1F, 1.5F);
					}
				}
			}
		}.runTask(Sblock.getInstance());
	}

}
