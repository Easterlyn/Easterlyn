package com.easterlyn.events.listeners.block;

import java.util.Map.Entry;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.discord.Discord;
import com.easterlyn.events.Events;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.users.BukkitSerializer;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.PermissionUtils;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Listener for BlockPlaceEvents.
 * 
 * @author Jikoo
 */
public class PlaceListener extends SblockListener {

	private final Discord discord;
	private final Events events;
	private final Language lang;
	private final Machines machines;

	public PlaceListener(Easterlyn plugin) {
		super(plugin);
		this.discord = plugin.getModule(Discord.class);
		this.events = plugin.getModule(Events.class);
		this.lang = plugin.getModule(Language.class);
		this.machines = plugin.getModule(Machines.class);

		PermissionUtils.addParent("sblock.events.creative.unfiltered", "sblock.felt");
	}

	/**
	 * Event handler for Machine construction.
	 * 
	 * @param event the BlockPlaceEvent
	 */
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPlace(BlockPlaceEvent event) {

		Player player = event.getPlayer();

		if (player.getGameMode() == GameMode.CREATIVE && !player.hasPermission("sblock.events.creative.unfiltered")
				&& events.getCreativeBlacklist().contains(event.getItemInHand().getType())) {
			event.setCancelled(true);
			return;
		}

		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getBlock());
		if (pair != null) {
			// Block registered as part of a machine. Most likely removed by explosion or similar.
			// Prevents place PGO as diamond block, blow up PGO, place and break dirt in PGO's
			// location to unregister, wait for CreeperHeal to regenerate diamond block for profit.
			event.setCancelled(true);
			player.sendMessage(lang.getValue("machines.noTouch"));
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
