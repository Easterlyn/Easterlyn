package co.sblock.events.listeners.player;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.Bed;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.chat.Color;
import co.sblock.effects.Effects;
import co.sblock.events.listeners.SblockListener;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.micromodules.AwayFromKeyboard;
import co.sblock.micromodules.Cooldowns;
import co.sblock.micromodules.DreamTeleport;
import co.sblock.micromodules.SleepVote;
import co.sblock.utilities.Experience;
import co.sblock.utilities.InventoryUtils;

/**
 * Listener for PlayerInteractEvents.
 * 
 * @author Jikoo
 */
public class InteractListener extends SblockListener {

	private final AwayFromKeyboard afk;
	private final Captcha captcha;
	private final Cooldowns cooldowns;
	private final DreamTeleport dream;
	private final Effects effects;
	private final Machines machines;
	private final SleepVote sleep;
	private final Set<Material> bypassable;

	public InteractListener(Sblock plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.captcha = plugin.getModule(Captcha.class);
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.dream = plugin.getModule(DreamTeleport.class);
		this.effects = plugin.getModule(Effects.class);
		this.machines = plugin.getModule(Machines.class);
		this.sleep = plugin.getModule(SleepVote.class);

		bypassable = new HashSet<>();
		for (Material material : Material.values()) {
			if (!material.isOccluding() && material != Material.WATER && material != Material.STATIONARY_WATER) {
				bypassable.add(material);
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerInteractMonitor(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.isCancelled()) {
			// Right clicking air is cancelled by default as there is no result.
			return;
		}

		// EFFECTS: Active application - right click only for now, change if needed.
		effects.handleEvent(event, event.getPlayer(), false);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onPlayerInteractLow(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.PHYSICAL) {
			return;
		}

		// Machines
		Pair<Machine, ConfigurationSection> pair = machines.getMachineByBlock(event.getClickedBlock());
		if (pair != null) {
			event.setCancelled(pair.getLeft().handleInteract(event, pair.getRight()));
		}
	}

	/**
	 * The event handler for PlayerInteractEvents.
	 * 
	 * @param event the PlayerInteractEvent
	 */
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		afk.extendActivity(event.getPlayer());

		if (event.getAction() != Action.RIGHT_CLICK_AIR && event.isCancelled()) {
			// Right clicking air is cancelled by default as there is no result.
			return;
		}

		if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
			return;
		}

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = event.getClickedBlock();

			if (b.getType().equals(Material.BED_BLOCK)) {
				if (b.getWorld().getEnvironment() == Environment.NETHER || b.getWorld().getEnvironment() == Environment.THE_END) {
					// Vanilla bed explosions!
					return;
				}
				// Sleep voting
				if (sleep.isEnabled() && event.getPlayer().isSneaking()) {
					if (b.getWorld().getTime() > 12000 || b.getWorld().hasStorm()) {
						sleep.sleepVote(b.getWorld(), event.getPlayer());
						event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation());
					} else {
						event.getPlayer().sendMessage(Color.BAD + "It's not dark or raining!");
						event.getPlayer().setBedSpawnLocation(event.getPlayer().getLocation());
					}
					event.setCancelled(true);
					return;
				}

				// Sleep teleport
				if (dream.handleBedInteract(event.getPlayer(), b, (Bed) b.getState().getData())) {
					event.setCancelled(true);
					return;
				}
			}

			if (hasRightClickFunction(event.getClickedBlock())
					&& !event.getPlayer().isSneaking()) {
				// Other inventory/action. Do not proceed to captcha.
				return;
			}
		}

		PlayerInventory inv = event.getPlayer().getInventory();
		boolean mainHand = InventoryUtils.isMainHand(event);
		ItemStack held = InventoryUtils.getHeldItem(inv, mainHand);

		// Nothing in current hand, bail
		if (held == null) {
			return;
		}

		// TODO TODO this is hairy
		// Check item in main hand, if it has a right click function and isn't captcha/exp bugger off
		// Check item in off hand normally after if we did nothing

		if (held.getType() == Material.GLASS_BOTTLE
				&& cooldowns.getRemainder(event.getPlayer(), "ExpBottle") == 0) {
			for (Block block : event.getPlayer().getLineOfSight((java.util.Set<Material>) null, 4)) {
				if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
					return;
				}
			}
			// Bottle experience by right clicking
			int exp = Experience.getExp(event.getPlayer());
			if (exp >= 11) {
				Experience.changeExp(event.getPlayer(), -11);
				InventoryUtils.setHeldItem(inv, mainHand, InventoryUtils.decrement(held, 1));
				event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),
						new ItemStack(Material.EXP_BOTTLE, 1)).setPickupDelay(0);
				return;
			}
		}

		// Uncaptcha
		if (Captcha.isUsedCaptcha(held)) {
			ItemStack captchaStack = captcha.captchaToItem(held);
			if (held.getAmount() > 1) {
				held.setAmount(held.getAmount() - 1);
				if (event.getPlayer().getInventory().firstEmpty() != -1) {
					event.getPlayer().getInventory().addItem(captchaStack);
				} else {
					event.getPlayer().getWorld().dropItem(event.getPlayer().getEyeLocation(), captchaStack)
							.setVelocity(event.getPlayer().getLocation().getDirection().multiply(0.4));
				}
			} else {
				InventoryUtils.setHeldItem(inv, mainHand, captchaStack);
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
		case IRON_DOOR_BLOCK:
		case IRON_TRAPDOOR:
			return Bukkit.getPluginManager().isPluginEnabled("LWC");
		case ACACIA_DOOR:
		case ACACIA_FENCE_GATE:
		case ANVIL:
		case BEACON:
		case BED_BLOCK:
		case BIRCH_DOOR:
		case BIRCH_FENCE_GATE:
		case BREWING_STAND:
		case BURNING_FURNACE:
		case CAULDRON:
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
		case JUKEBOX:
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
