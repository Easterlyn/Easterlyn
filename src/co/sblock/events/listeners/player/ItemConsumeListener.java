package co.sblock.events.listeners.player;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import co.sblock.Sblock;
import co.sblock.events.Events;
import co.sblock.events.listeners.SblockListener;
import co.sblock.micromodules.Cooldowns;

/**
 * Listener for PlayerConsumeItemEvents.
 * 
 * @author Jikoo
 */
public class ItemConsumeListener extends SblockListener {

	private final Cooldowns cooldowns;
	private final Events events;

	public ItemConsumeListener(Sblock plugin) {
		super(plugin);
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.events = plugin.getModule(Events.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsumeMonitor(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.POTION) {
			return;
		}
		cooldowns.addCooldown(event.getPlayer(), "PotionDrink", 1500);
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
		events.getInvisibilityManager().lazyVisibilityUpdate(event.getPlayer());
	}
}
