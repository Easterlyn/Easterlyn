package co.sblock.events.listeners.block;

import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Direction;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for BlockPlaceEvents.
 * 
 * @author Jikoo
 */
public class PlaceListener implements Listener {

	/**
	 * Event handler for Machine construction.
	 * 
	 * @param event the BlockPlaceEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (pair != null) {
			// Block registered as part of a machine. Most likely removed by explosion or similar.
			// Prevents place PGO as diamond block, blow up PGO, place and break dirt in PGO's
			// location to unregister, wait for CreeperHeal to regenerate diamond block for profit.
			event.setCancelled(true);
			event.getPlayer().sendMessage(Color.BAD + "You decide against fussing with the internals of this machine.");
		}

		// Server mode placement
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			if (event.getItemInHand().isSimilar(Machines.getMachineByName("Computer").getUniqueDrop())) {
				event.setCancelled(true);
			} else {
				final int slot = event.getPlayer().getInventory().getHeldItemSlot();
				final ItemStack placed = event.getItemInHand().clone();
				final Player player = event.getPlayer();
				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					@Override
					public void run() {
						player.getInventory().setItem(slot, placed);
						player.updateInventory();
					}
				});
			}
		}

		Machines.getInstance();
		// Machine place logic
		for (Entry<String, Machine> entry : Machines.getMachinesByName().entrySet()) {
			if (entry.getValue().getUniqueDrop() == null) {
				continue;
			}
			if (entry.getValue().getUniqueDrop().isSimilar(event.getItemInHand())) {
				try {
					pair = Machines.getInstance().addMachine(event.getBlock().getLocation(),
							entry.getKey(), event.getPlayer().getUniqueId(),
							Direction.getFacingDirection(event.getPlayer()));
					pair.getLeft().assemble(event, pair.getRight());
				} catch (NullPointerException e) {
					Machines.getInstance().getLogger().debug("Invalid machine placed.");
					event.setBuild(false);
					event.setCancelled(true);
				}
				break;
			}
		}
	}
}
