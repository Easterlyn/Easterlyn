package com.easterlyn.events.listeners.player;

import java.util.HashSet;
import java.util.Set;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.effects.Effects;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.Cooldowns;
import com.easterlyn.utilities.Experience;
import com.easterlyn.utilities.InventoryUtils;

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

/**
 * Listener for PlayerInteractEvents.
 *
 * @author Jikoo
 */
public class InteractListener extends EasterlynListener {

	private final AwayFromKeyboard afk;
	private final Captcha captcha;
	private final Cooldowns cooldowns;
	private final Effects effects;
	private final Machines machines;
	private final Set<Material> bypassable;

	public InteractListener(Easterlyn plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.captcha = plugin.getModule(Captcha.class);
		this.cooldowns = plugin.getModule(Cooldowns.class);
		this.effects = plugin.getModule(Effects.class);
		this.machines = plugin.getModule(Machines.class);

		this.bypassable = new HashSet<>();
		for (Material material : Material.values()) {
			if (!material.isOccluding() && material != Material.WATER) {
				this.bypassable.add(material);
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
	public void onPlayerInteractHigh(PlayerInteractEvent event) {
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

		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& hasRightClickFunction(event.getClickedBlock(), event.getItem())
				&& !event.getPlayer().isSneaking()) {
			// Other inventory/action. Do not proceed to captcha.
			return;
		}

		PlayerInventory inv = event.getPlayer().getInventory();
		boolean mainHand = InventoryUtils.isMainHand(event);
		ItemStack held = event.getItem();

		// Nothing in current hand, bail
		if (held == null || held.getType() == Material.AIR) {
			return;
		}

		if (held.getType() == Material.GLASS_BOTTLE
				&& cooldowns.getRemainder(event.getPlayer(), "ExpBottle") == 0) {
			for (Block block : event.getPlayer().getLineOfSight(bypassable, 4)) {
				if (block.getType() == Material.WATER) {
					return;
				}
			}
			// Bottle experience by right clicking
			int exp = Experience.getExp(event.getPlayer());
			if (exp >= 11) {
				Experience.changeExp(event.getPlayer(), -11);
				InventoryUtils.setHeldItem(inv, mainHand, InventoryUtils.decrement(held, 1));
				event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(),
						new ItemStack(Material.EXPERIENCE_BOTTLE, 1)).setPickupDelay(0);
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
	 * @param block the Block to check
	 *
	 * @return true if right clicking the block without sneaking will cause
	 */
	private boolean hasRightClickFunction(Block block, ItemStack hand) {
		switch (block.getType()) {
		case BED:
		case BOOKSHELF:
			// RIP BookShelf, you will be missed.
			return Bukkit.getPluginManager().isPluginEnabled("BookShelf");
		case IRON_DOOR_BLOCK:
		case IRON_TRAPDOOR:
			return Bukkit.getPluginManager().isPluginEnabled("LWC");
		case ENDER_STONE:
			// Special case: player is probably attempting to bottle dragon's breath
			return block.getWorld().getEnvironment() == Environment.THE_END
					&& hand != null && hand.getType() == Material.GLASS_BOTTLE;
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
		case WHITE_SHULKER_BOX:
		case ORANGE_SHULKER_BOX:
		case MAGENTA_SHULKER_BOX:
		case LIGHT_BLUE_SHULKER_BOX:
		case YELLOW_SHULKER_BOX:
		case LIME_SHULKER_BOX:
		case PINK_SHULKER_BOX:
		case GRAY_SHULKER_BOX:
		case LIGHT_GRAY_SHULKER_BOX:
		case CYAN_SHULKER_BOX:
		case PURPLE_SHULKER_BOX:
		case BLUE_SHULKER_BOX:
		case BROWN_SHULKER_BOX:
		case GREEN_SHULKER_BOX:
		case RED_SHULKER_BOX:
		case BLACK_SHULKER_BOX:
			return true;
		default:
			return false;
		}
	}

}
