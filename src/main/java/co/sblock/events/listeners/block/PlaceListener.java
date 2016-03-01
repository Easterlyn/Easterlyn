package co.sblock.events.listeners.block;

import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.discord.Discord;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.Direction;
import co.sblock.users.BukkitSerializer;
import co.sblock.utilities.InventoryUtils;

/**
 * Listener for BlockPlaceEvents.
 * 
 * @author Jikoo
 */
public class PlaceListener extends SblockListener {

	private final Discord discord;
	private final Machines machines;

	public PlaceListener(Sblock plugin) {
		super(plugin);
		this.discord = plugin.getModule(Discord.class);
		this.machines = plugin.getModule(Machines.class);
	}

	/**
	 * Event handler for Machine construction.
	 * 
	 * @param event the BlockPlaceEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		Player player = event.getPlayer();
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			// Block registered as part of a machine. Most likely removed by explosion or similar.
			// Prevents place PGO as diamond block, blow up PGO, place and break dirt in PGO's
			// location to unregister, wait for CreeperHeal to regenerate diamond block for profit.
			event.setCancelled(true);
			player.sendMessage(Color.BAD + "You decide against fussing with the internals of this machine.");
			// If the blocks are not exploded, there's a larger issue. Rather than shaft the person
			// who found it, generate a report and repair it.
			if (!machines.isExploded(event.getBlock())) {
				pair.getLeft().reassemble(pair.getRight());
				discord.postReport("Repairing broken " + pair.getLeft().getName() + " at "
						+ BukkitSerializer.locationToBlockCenterString(event.getBlock().getLocation())
						+ " after internal placement by " + event.getPlayer().getName());
			}
			return;
		}

		// Machine place logic
		for (Entry<String, Machine> entry : machines.getMachinesByName().entrySet()) {
			if (entry.getValue().getUniqueDrop().isSimilar(event.getItemInHand())) {
				pair = machines.addMachine(event.getBlock().getLocation(),
						entry.getValue().getName(), event.getPlayer().getUniqueId(),
						Direction.getFacingDirection(event.getPlayer()));
				if (pair == null) {
					event.setCancelled(true);
					return;
				}
				pair.getLeft().assemble(event, pair.getRight());
				if (!event.isCancelled() && player.getGameMode() != GameMode.CREATIVE) {
					if (player.getInventory().getItemInMainHand().equals(event.getItemInHand())) {
						player.getInventory().setItemInMainHand(InventoryUtils.decrement(event.getItemInHand(), 1));
					} else {
						player.getInventory().setItemInOffHand(InventoryUtils.decrement(event.getItemInHand(), 1));
					}
				}
				event.setCancelled(true);
				break;
			}
		}
	}
}
