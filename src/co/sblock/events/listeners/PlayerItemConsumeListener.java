package co.sblock.events.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.effects.FXManager;

/**
 * Listener for PlayerConsumeItemEvents.
 * 
 * @author Jikoo
 */
public class PlayerItemConsumeListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsumeMonitor(PlayerItemConsumeEvent event) {
		if (!event.getItem().hasItemMeta() || event.getItem().getType() != Material.POTION) {
			return;
		}
		Potion potion = Potion.fromItemStack(event.getItem());
		boolean invisibility = false;
		for (PotionEffect effect : potion.getEffects()) {
			if (effect.getType() == PotionEffectType.INVISIBILITY) {
				invisibility = true;
				break;
			}
		}
		if (!invisibility) {
			return;
		}
		FXManager.getInstance().getInvisibilityManager().lazyVisibilityUpdate(event.getPlayer());
	}
}
