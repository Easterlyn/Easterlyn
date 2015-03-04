package co.sblock.events.listeners;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Bed;

import co.sblock.effects.FXManager;
import co.sblock.effects.fx.SblockFX;
import co.sblock.events.Events;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Computer;
import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.MachineType;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Users;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.experience.Experience;
import co.sblock.utilities.inventory.InventoryUtils;
import co.sblock.utilities.progression.Entry;
import co.sblock.utilities.progression.ServerMode;
import co.sblock.utilities.vote.SleepVote;

/**
 * 
 * 
 * @author Jikoo
 */
public class PlayerInteractListener implements Listener {

	/**
	 * The event handler for PlayerInteractEvents.
	 * 
	 * @param event the PlayerInteractEvent
	 */
	@SuppressWarnings("deprecation")
	public void onPlayerInteract(PlayerInteractEvent event) {
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			// No interaction with any blocks while out of range.
			if (event.getAction().name().contains("BLOCK") && !ServerMode.getInstance().isWithinRange(
					user, event.getClickedBlock())) {
				event.getPlayer().sendMessage(ChatColor.RED + "Block out of range!");
				event.setCancelled(true);
				return;
			}
			// Breaking and placing blocks is acceptable, instabreak blocks in approved list.
			if (event.getAction() == Action.LEFT_CLICK_BLOCK
					&& ServerMode.getInstance().isApproved(event.getClickedBlock().getType())
					&& !Machines.getInstance().isMachine(event.getClickedBlock())) {
				event.getClickedBlock().setType(Material.AIR);
			} else if (event.getAction() == Action.RIGHT_CLICK_AIR && event.getItem() != null) {
				if (ServerMode.getInstance().isApproved(event.getMaterial())) {
					// Right click air: Cycle to next approved material
					ServerMode.getInstance().cycleData(event.getItem());
				} else if (event.getItem().equals(MachineType.COMPUTER.getUniqueDrop())) {
					// Right click air: Open computer
					event.getPlayer().openInventory(new Computer(event.getPlayer().getLocation(),
							event.getPlayer().getUniqueId().toString(), true).getInventory(user));
				}
			}
			return;
		}

		//Entry Trigger Items
		if (event.getItem() != null) {
			for (Material m : Entry.getEntry().getMaterialList()) {
				if (event.getItem().getType() == m && event.getItem().getItemMeta().hasDisplayName() 
						&& event.getItem().getItemMeta().getDisplayName().startsWith(ChatColor.AQUA + "Cruxite ")) {
					if (Entry.getEntry().isEntering(user)) {
						if (m == Entry.getEntry().getData().get(user).getCruxtype()) {
							Entry.getEntry().succeed(user);
						}
					}
					event.getPlayer().setItemInHand(null);
					return;
				}
			}
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			return;
		}

		// Effect application
		HashMap<String, SblockFX> effects = FXManager.getInstance().itemScan(event.getItem());
		for (SblockFX fx : effects.values()) {
			fx.applyEffect(Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser(), event);
		}
		if(Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser().getProgression() == ProgressionState.GODTIER &&
				event.getPlayer().isSneaking() && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getOnlineUser().applyGodtierActiveEffect();
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();

			// Machines
			Machine m = Machines.getInstance().getMachineByBlock(b);
			if (m != null) {
				event.setCancelled(m.handleInteract(event));
				return;
			}

			if (b.getType().equals(Material.BED_BLOCK)) {
				if (b.getWorld().getEnvironment() == Environment.NETHER || b.getWorld().getEnvironment() == Environment.THE_END) {
					// Vanilla bed explosions!
					return;
				}
				// Sleep voting
				if (event.getPlayer().isSneaking()) {
					if (b.getWorld().getTime() > 12000 || b.getWorld().hasStorm()) {
						SleepVote.getInstance().sleepVote(b.getWorld(), event.getPlayer());
						event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation());
					} else {
						event.getPlayer().sendMessage(ChatColor.YELLOW + "It's not dark or raining!");
						event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation());
					}
					event.setCancelled(true);
					return;
				}

				// Sleep teleport
				Bed bed = (Bed) b.getState().getData();
				Location head;
				if (bed.isHeadOfBed()) {
					head = b.getLocation();
				} else {
					// bed.getFacing does not return correctly in most cases.
					BlockFace relative;
					switch (bed.getData()) {
					case 0:
						relative = BlockFace.SOUTH;
						break;
					case 1:
						relative = BlockFace.WEST;
						break;
					case 2:
						relative = BlockFace.EAST;
						break;
					case 3:
						relative = BlockFace.NORTH;
						break;
					default:
						relative = BlockFace.SELF;
						break;
					}
					head = b.getRelative(relative).getLocation();
				}

				switch (Users.getGuaranteedUser(event.getPlayer().getUniqueId()).getCurrentRegion()) {
				case EARTH:
				case PROSPIT:
				case LOFAF:
				case LOHAC:
				case LOLAR:
				case LOWAS:
				case DERSE:
					Events.getInstance().fakeSleepDream(event.getPlayer(), head);
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
		} else if (event.getPlayer().getItemInHand() != null
				&& event.getPlayer().getItemInHand().getType() == Material.GLASS_BOTTLE) {
			// Bottle experience by right clicking air
			int exp = Experience.getExp(event.getPlayer());
			if (exp >= 11) {
				Experience.changeExp(event.getPlayer(), -11);
				event.getPlayer().setItemInHand(InventoryUtils.decrement(event.getPlayer().getItemInHand(), 1));
				event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),
						new ItemStack(Material.EXP_BOTTLE, 1)).setPickupDelay(0);
				return;
			}
		}

		// Uncaptcha
		if (Captcha.isUsedCaptcha(event.getItem())) {
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
		case ACACIA_DOOR:
		case ACACIA_FENCE_GATE:
		case ANVIL:
		case BEACON:
		case BED_BLOCK:
		case BIRCH_DOOR:
		case BIRCH_FENCE_GATE:
		case BREWING_STAND:
		case BURNING_FURNACE:
		case CHEST:
		case COMMAND:
		case DARK_OAK_DOOR:
		case DARK_OAK_FENCE_GATE:
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
		case JUNGLE_DOOR:
		case JUNGLE_FENCE_GATE:
		case LEVER:
		case NOTE_BLOCK:
		case REDSTONE_COMPARATOR:
		case REDSTONE_COMPARATOR_OFF:
		case REDSTONE_COMPARATOR_ON:
		case SPRUCE_DOOR:
		case SPRUCE_FENCE_GATE:
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
