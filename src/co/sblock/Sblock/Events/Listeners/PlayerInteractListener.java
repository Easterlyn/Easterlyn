/**
 * 
 */
package co.sblock.Sblock.Events.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;

import co.sblock.Sblock.Events.SblockEvents;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Machines.Type.Machine;
import co.sblock.Sblock.UserData.Region;
import co.sblock.Sblock.UserData.SblockUser;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.Captchadex;

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
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (SblockUser.getUser(event.getPlayer().getName()).isServer()) {
			event.setCancelled(true);
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();

			// Machines
			Machine m = SblockMachines.getMachines().getManager().getMachineByBlock(b);
			if (m != null) {
				event.setCancelled(m.handleInteract(event));
				return;
			}

			// Sleep teleport
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

			if (hasRightClickFunction(event.getClickedBlock())
					&& !event.getPlayer().isSneaking()) {
				// Other inventory/action. Do not proceed to captcha.
				return;
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			return; // Captcha and Captchadex right clicks only.
		}

		// Captchadex
		if (Captchadex.isCaptchadex(event.getPlayer())) {
			event.getPlayer().openInventory(Captchadex.loadCaptchadex(event.getItem()));
			return;
		}

		// Uncaptcha
		if (Captcha.isCaptchaCard(event.getItem())) {
			ItemStack captcha = Captcha.captchaToItem(event.getItem());
			if (event.getItem().getAmount() > 1) {
				event.getItem().setAmount(event.getItem().getAmount() - 1);
				if (event.getPlayer().getInventory().firstEmpty() != -1) {
					event.getPlayer().getInventory().addItem(captcha);
				} else {
					event.getPlayer().getWorld().dropItem(event.getPlayer().getEyeLocation(), captcha)
							.setVelocity(event.getPlayer().getLocation().getDirection().multiply(0.4));
				}
			} else {
				event.getPlayer().setItemInHand(captcha);
			}
			event.getPlayer().updateInventory();
		}
	}

	/**
	 * Check if a Block has a right click action.
	 * 
	 * @param b the Block to check
	 * 
	 * @return true if right clicking the block without sneaking will cause 
	 */
	private boolean hasRightClickFunction(Block b) {
		switch (b.getType()) {
		case BOOKSHELF:
			// Awww yiss BookShelf <3
			return Bukkit.getPluginManager().isPluginEnabled("BookShelf");
		case CAULDRON:
			return Bukkit.getPluginManager().isPluginEnabled("BookSuite");
		case ANVIL:
		case BEACON:
		case BED_BLOCK:
		case BREWING_STAND:
		case BURNING_FURNACE:
		case CHEST:
		case COMMAND:
		case DAYLIGHT_DETECTOR:
		case DIODE_BLOCK_OFF:
		case DIODE_BLOCK_ON:
		case DISPENSER:
		case DRAGON_EGG:
		case DROPPER:
		case ENCHANTMENT_TABLE:
		case ENDER_CHEST:
		case FENCE_GATE:
		case HOPPER:
		case ITEM_FRAME:
		case LEVER:
		case NOTE_BLOCK:
		case REDSTONE_COMPARATOR:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case STONE_BUTTON:
		case TRAPPED_CHEST:
		case TRAP_DOOR:
		case TRIPWIRE_HOOK:
		case WOODEN_DOOR:
		case WOOD_BUTTON:
		case WOOD_DOOR:
		case WORKBENCH:
			return true;
		default:
			return false;
		}
	}
}
