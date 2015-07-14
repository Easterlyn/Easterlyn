package co.sblock.machines.utilities;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.utilities.messages.RegexUtils;

/**
 * Enum defining all types of Machines.
 * 
 * @author Jikoo
 */
public enum MachineType {

	ALCHEMITER,
	COMPILATION_AMALGAMATOR,
	COMPOUNDING_UNIONIZER,
	COMPUTER,
	CRUXTRUDER,
	PERFECT_BUILDING_OBJECT,
	PERFECTLY_GENERIC_OBJECT,
	PUNCH_DESIGNIX,
	TOTEM_LATHE,
	TRANSPORTALIZER,
	ANY;

	/**
	 * Gets a MachineType by abbreviation or full name.
	 * 
	 * @param type the String to match
	 * 
	 * @return the MachineType or null if invalid
	 */
	public static MachineType getType(String type) {
		try {
			return valueOf(type.toUpperCase());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Gets Machine data based on type from a BlockPlaceEvent.
	 * 
	 * @param e the BlockPlaceEvent
	 * 
	 * @return the Machine data
	 */
	public String getData(BlockPlaceEvent e) {
		if (this == MachineType.PERFECTLY_GENERIC_OBJECT) {
			return e.getBlockAgainst().getState().getData().toString();
		}
		return null;
	}

	/**
	 * Gets the ItemStack that represents this MachineType.
	 * 
	 * @return the ItemStack
	 */
	public ItemStack getUniqueDrop() {
		ItemStack is = new ItemStack(Material.BEDROCK);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(ChatColor.WHITE + RegexUtils.getFriendlyName(name()));
		switch (this) {
		case ALCHEMITER:
			is.setType(Material.QUARTZ_BLOCK);
			is.setDurability((short) 2);
			break;
		case TRANSPORTALIZER:
			is.setType(Material.CHEST);
			break;
		case COMPUTER:
			is.setType(Material.JUKEBOX);
			break;
		case CRUXTRUDER:
			is.setType(Material.BEACON);
			break;
		case PERFECT_BUILDING_OBJECT:
			is.setType(Material.DIAMOND_BLOCK);
			break;
		case PERFECTLY_GENERIC_OBJECT:
			is.setType(Material.DIRT);
			break;
		case PUNCH_DESIGNIX:
			is.setType(Material.NOTE_BLOCK);
			break;
		case TOTEM_LATHE:
			is.setType(Material.ANVIL);
			break;
		case ANY:
			return values()[(int) Math.random() * values().length].getUniqueDrop();
		default:
			im.setDisplayName(ChatColor.WHITE + "Don't place this.");
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.DARK_RED + "It'd make my life a lot easier.");
			im.setLore(lore);
			break;
		}
		is.setItemMeta(im);
		return is;
	}

	public boolean isFree() {
		switch (this) {
		case ALCHEMITER:
		case COMPUTER:
		case CRUXTRUDER:
		case PUNCH_DESIGNIX:
		case TOTEM_LATHE:
			return true;
		default:
			return false;
		}
	}

	public int getCost() {
		switch (this) {
		case TRANSPORTALIZER:
			return Integer.MAX_VALUE;
		default:
			return -1;
		}
	}

	// Future stuff:
	// Holopad: Largely useless unless we redo captchas NOW to use books.
	// Intellibeam Laserstation: captcha books/Captchacards?
	// Punch Card Shunt: May not require, because screw it.
	// Jumper Block Extension: So many things to take into account @.@ Not yet.
	// Cloning Pad: Too op
	// Ectobiology Apparatus: borked cloning pad. Free slime!
}
