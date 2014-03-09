/**
 * 
 */
package co.sblock.Sblock.Events.Listeners;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Bed;

import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.SblockEffects.ActiveEffect;
import co.sblock.Sblock.SblockEffects.ActiveEffectType;
import co.sblock.Sblock.SblockEffects.EffectManager;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;

/**
 * @author Jikoo
 *
 */
public class PlayerInteractListener implements Listener {

	/**
	 * The event handler for PlayerInteractEvents.
	 * 
	 * @param event the PlayerInteractEvent
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (SblockUser.getUser(event.getPlayer().getName()).isServer()) {
			event.setCancelled(true);
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();

			Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(b);
			if (m != null) {
				event.setCancelled(m.handleInteract(event));
				return;
			}

			if (b.getType().equals(Material.BED_BLOCK)) {
				if (SblockUser.getUser(event.getPlayer().getName()).isGodTier()) {
					// future feature
					return;
				}
				Bed bed = (Bed) b.getState().getData();
				Location head;
				if (bed.isHeadOfBed()) {
					head = b.getLocation();
				} else {
					head = b.getRelative(bed.getFacing()).getLocation();
					// getFace does not seem to work in most cases - adam test and fix
				}
				switch (Region.uValueOf(head.getWorld().getName())) {
				case EARTH:
				case MEDIUM:
				case INNERCIRCLE:
				case OUTERCIRCLE:
					SblockEvents.getEvents().fakeSleepDream(event.getPlayer(), head);
					event.setCancelled(true);
					return;
				default:
					return;
				}
			}
		}
		else if (event.getAction() == Action.RIGHT_CLICK_AIR) { //SblockEffects Active Effect
			SblockUser user = SblockUser.getUser(event.getPlayer().getName());
			HashMap<ActiveEffect, Integer> effects = EffectManager.activeScan(event.getPlayer());
			if (effects.isEmpty()) return;
			for (ActiveEffect aE : effects.keySet()) {
				if (aE.getActiveEffectType() == ActiveEffectType.RIGHT_CLICK) {
					ActiveEffect.applyRightClickEffect(event.getPlayer(), aE, effects.get(aE));
				}
			}
		}
	}
}
