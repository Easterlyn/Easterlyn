package co.sblock.events.listeners;

import java.util.HashMap;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import co.sblock.effects.FXManager;
import co.sblock.effects.fx.SblockFX;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.progression.ServerMode;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for BlockBreakEvents.
 * 
 * @author Jikoo
 */
public class BlockBreakListener implements Listener {

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

		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleBreak(event));
		}

		OfflineUser u = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (u != null && u.isServer()) {
			event.setCancelled(event.isCancelled() || !ServerMode.getInstance().isWithinRange(u, event.getBlock()));
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onBlockBreakOccurred(BlockBreakEvent event) {
		// Effect application
		HashMap<String, SblockFX> effects = FXManager.getInstance().itemScan(event.getPlayer().getItemInHand());
		for (SblockFX fx : effects.values()) {
			fx.applyEffect(Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser(), event);
		}
	}
}
