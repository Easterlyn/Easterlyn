package com.easterlyn.events.listeners.entity;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.VillagerAdjustment;

import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.MerchantRecipe;

/**
 * Listener for VillagerAquireTradeEvents.
 * 
 * @author Jikoo
 */
public class VillagerAcquireTradeListener extends EasterlynListener {

	private final VillagerAdjustment villagers;

	public VillagerAcquireTradeListener(Easterlyn plugin) {
		super(plugin);
		this.villagers = plugin.getModule(VillagerAdjustment.class);
	}

	@EventHandler
	public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
		MerchantRecipe trade = this.villagers.adjustRecipe(event.getRecipe());
		if (trade == null) {
			event.setCancelled(true);
			return;
		}
		event.setRecipe(trade);
	}

}
