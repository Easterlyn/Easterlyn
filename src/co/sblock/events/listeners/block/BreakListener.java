package co.sblock.events.listeners.block;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.effects.Effects;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.Spectators;
import co.sblock.progression.ServerMode;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * Listener for BlockBreakEvents.
 * 
 * @author Jikoo
 */
public class BreakListener implements Listener {

	/**
	 * The event handler for Machine deconstruction.
	 * 
	 * @param event the BlockBreakEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onBlockBreak(BlockBreakEvent event) {
		if (event.getBlock().getType().name().endsWith("_ORE") && !Spectators.getInstance().canMineOre(event.getPlayer())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You cannot mine ore shortly after exiting spectate mode!");
			return;
		}

		Pair<Machine, ConfigurationSection> pair = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleBreak(event, pair.getRight()));
		}

		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			event.setCancelled(event.isCancelled() || !ServerMode.getInstance().isWithinRange(user, event.getBlock()));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreakOccurred(BlockBreakEvent event) {
		Effects.getInstance().handleEvent(event, event.getPlayer(), false);
	}
}
