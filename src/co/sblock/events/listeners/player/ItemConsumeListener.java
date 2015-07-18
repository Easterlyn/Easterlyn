package co.sblock.events.listeners.player;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.events.Events;
import co.sblock.utilities.general.Cooldowns;

/**
 * Listener for PlayerConsumeItemEvents.
 * 
 * @author Jikoo
 */
public class ItemConsumeListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsumeMonitor(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.POTION) {
			return;
		}
		Cooldowns.getInstance().addCooldown(event.getPlayer().getUniqueId(), "PotionDrink", 1500);
		if (!event.getItem().hasItemMeta()) {
			return;
		}

		Potion potion = Potion.fromItemStack(event.getItem());
		boolean invisibility = false;
		for (PotionEffect effect : potion.getEffects()) {
			if (effect.getType().equals(PotionEffectType.INVISIBILITY)) {
				invisibility = true;
				break;
			}
		}
		if (!invisibility) {
			return;
		}
		Events.getInstance().getInvisibilityManager().lazyVisibilityUpdate(event.getPlayer());
	}
}
