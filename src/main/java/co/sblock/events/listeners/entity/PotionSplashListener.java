package co.sblock.events.listeners.entity;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.Sblock;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;

/**
 * Listener for PotionSplashEvents.
 * 
 * @author Jikoo
 */
public class PotionSplashListener extends SblockListener {

	private final Events events;

	public PotionSplashListener(Sblock plugin) {
		super(plugin);
		this.events = plugin.getModule(Events.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		boolean invisibility = false;
		for (PotionEffect effect : event.getPotion().getEffects()) {
			if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
				invisibility = true;
				break;
			}
		}
		if (!invisibility) {
			return;
		}
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (entity instanceof Player) {
				events.getInvisibilityManager().lazyVisibilityUpdate((Player) entity);
			}
		}
	}
}
