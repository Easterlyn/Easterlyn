package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.PBO;
import co.sblock.machines.utilities.MachineType;
import co.sblock.machines.utilities.Direction;
import co.sblock.users.UserManager;
import co.sblock.utilities.jesse.JesseChurch;

/**
 * Listener for BlockPlaceEvents.
 * 
 * @author Jikoo
 */
public class BlockPlaceListener implements Listener {

	/**
	 * Event handler for Machine construction.
	 * 
	 * @param event the BlockPlaceEvent
	 */
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		if (JesseChurch.theOneAndOnly().isAnActOfGod(event)) {
			JesseChurch.hisRoyalHighness().dealWithJesse(event);
			return; // "and nothing beside remained" --Percy Shelly
		}

		Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(event.getBlock());
		if (m != null) {
			// Block registered as part of a machine. Most likely removed by explosion or similar.
			// Prevents place PGO as diamond block, blow up PGO, place and break dirt in PGO's
			// location to unregister, wait for CreeperHeal to regenerate diamond block for profit.
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You decide against fussing with the internals of this machine.");
		}

		// Server mode placement
		if (UserManager.getUser(event.getPlayer().getUniqueId()).isServer()) {
			if (event.getItemInHand().isSimilar(MachineType.COMPUTER.getUniqueDrop())) {
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

		// Machine place logic
		for (MachineType mt : MachineType.values()) {
			if (mt.getUniqueDrop().isSimilar(event.getItemInHand())) {
				if (mt == MachineType.PERFECT_BUILDING_OBJECT) {
					new PBO(event.getBlock().getLocation(), "").assemble(event);
					break;
				}
				try {
					SblockMachines.getMachines().getManager().addMachine(
							event.getBlock().getLocation(), mt, event.getPlayer().getUniqueId().toString(),
							Direction.getFacingDirection(event.getPlayer()), mt.getData(event)).assemble(event);
				} catch (NullPointerException e) {
					SblockMachines.getMachines().getLogger().debug("Invalid machine placed.");
					event.setBuild(false);
					event.setCancelled(true);
				}
				break;
			}
		}
	}
}
