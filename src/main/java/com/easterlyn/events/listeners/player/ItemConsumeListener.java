package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.Cooldowns;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffectType;

/**
 * Listener for PlayerConsumeItemEvents.
 * 
 * @author Jikoo
 */
public class ItemConsumeListener extends EasterlynListener {

	private final Cooldowns cooldowns;
	private final Events events;

	public ItemConsumeListener(Easterlyn plugin) {
		super(plugin);
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.events = plugin.getModule(Events.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerItemConsumeMonitor(PlayerItemConsumeEvent event) {
		if (event.getItem().getType() != Material.POTION) {
			return;
		}
		cooldowns.addCooldown(event.getPlayer(), "ExpBottle", 1500);
		if (!event.getItem().hasItemMeta()) {
			return;
		}

		if (!event.getItem().hasItemMeta()) {
			return;
		}
		ItemMeta meta = event.getItem().getItemMeta();
		if (!(meta instanceof PotionMeta)) {
			return;
		}
		if (((PotionMeta) meta).hasCustomEffect(PotionEffectType.INVISIBILITY)) {
			events.getInvisibilityManager().lazyVisibilityUpdate(event.getPlayer());
		}
	}

}
