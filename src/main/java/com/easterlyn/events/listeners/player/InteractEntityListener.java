package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.VillagerAdjustment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.Merchant;

/**
 * Listener for PlayerInteractEntityEvents to rebalance villagers.
 *
 * @author Jikoo
 */
public class InteractEntityListener extends EasterlynListener {

	private final VillagerAdjustment villagers;

	public InteractEntityListener(Easterlyn plugin) {
		super(plugin);
		this.villagers = plugin.getModule(VillagerAdjustment.class);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
		if (event.getRightClicked() instanceof Merchant) {
			villagers.adjustMerchant((Merchant) event.getRightClicked());
		}
	}

}
