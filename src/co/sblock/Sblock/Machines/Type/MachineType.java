package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * @author Jikoo
 */
public enum MachineType {

	ALCHEMITER("alc"), COMPUTER("cpu"), CRUXTRUDER("crx"),
	INTELLIBEAM_LASERSTATION("il"), PERFECT_BUILDING_OBJECT("pbo"),
	PERFECTLY_GENERIC_OBJECT("pgo"), PUNCH_DESIGNIX("pd"),
	TOTEM_LATHE("tl"), TRANSMATERIALIZER("tm"),
	TRANSPORTALIZER("tp"), ANY("NO.");

	/** The shortened name of the <code>MachineType</code>. */
	String type;

	/**
	 * Constructor for MachineType.
	 * 
	 * @param s
	 *            the shortened name
	 */
	private MachineType(String s) {
		type = s;
	}

	/**
	 * Gets a shorter name for the <code>MachineType</code>.
	 * 
	 * @return the shortened name for the <code>MachineType</code>
	 */
	public String getAbbreviation() {
		return type;
	}

	/**
	 * Gets a MachineType by abbreviation or full name.
	 * 
	 * @param type
	 *            the <code>String</code> to match
	 * @return the <code>MachineType</code> or <code>null</code> if invalid
	 */
	public static MachineType getType(String type) {
		for (MachineType m : MachineType.values()) {
			if (m.getAbbreviation().equals(type) || m.name().equals(type.toUpperCase())) {
				return m;
			}
		}
		return null;
	}

	/**
	 * Gets <code>Machine</code> data based on type from a
	 * <code>BlockPlaceEvent</code>.
	 * 
	 * @param e
	 *            the <code>BlockPlaceEvent</code>
	 * @return the <code>Machine</code> data
	 */
	@SuppressWarnings("deprecation")
	public String getData(BlockPlaceEvent e) {
		if (this != MachineType.PERFECTLY_GENERIC_OBJECT) {
			return e.getPlayer().getName();
		}
		return e.getBlockAgainst().getTypeId() + ":" + e.getBlockAgainst().getData();
	}

	/**
	 * Gets the <code>ItemStack</code> that represents this
	 * <code>MachineType</code>.
	 * 
	 * @return the <code>ItemStack</code>
	 */
	public ItemStack getUniqueDrop() {
		ItemStack is = null;
		ItemMeta im;
		switch (this) {
		case ALCHEMITER:
			is = new ItemStack(Material.QUARTZ_BLOCK);
			is.setDurability((short) 2);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Alchemiter");
			is.setItemMeta(im);
			break;
		case COMPUTER:
			is = new ItemStack(Material.JUKEBOX);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Computer");
			is.setItemMeta(im);
			break;
		case CRUXTRUDER:
			is = new ItemStack(Material.BEACON);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Cruxtender");
			is.setItemMeta(im);
			break;
		case PERFECT_BUILDING_OBJECT:
			is = new ItemStack(Material.DIAMOND_BLOCK);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Perfect Building Object");
			is.setItemMeta(im);
			break;
		case PERFECTLY_GENERIC_OBJECT:
			is = new ItemStack(Material.DIRT);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Perfectly generic object");
			is.setItemMeta(im);
			break;
		case PUNCH_DESIGNIX:
			is = new ItemStack(Material.NOTE_BLOCK);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Punch Designix");
			is.setItemMeta(im);
			break;
		case TOTEM_LATHE:
			is = new ItemStack(Material.ANVIL);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Totem Lathe");
			is.setItemMeta(im);
			break;
		case TRANSMATERIALIZER:
			is = new ItemStack(Material.CHEST);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Transmaterializer");
			is.setItemMeta(im);
			break;
		case TRANSPORTALIZER:
			is = new ItemStack(Material.CHEST);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Transportalizer");
			is.setItemMeta(im);
			break;
		case ANY:
			return values()[(int) Math.random() * values().length].getUniqueDrop();
		default:
			is = new ItemStack(Material.BEDROCK);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Don't place this.");
			ArrayList<String> lore = new ArrayList<String>();
			lore.add(ChatColor.DARK_RED + "It'd make my life a lot easier.");
			im.setLore(lore);
			is.setItemMeta(im);
			break;
		}
		return is;
	}
	// Future stuff:
	// Holopad: Largely useless unless we redo captchas NOW to use books.
	// Intellibeam Laserstation: captcha books/Captchacards?
	// Punch Card Shunt: May not require, because screw it.
	// Jumper Block Extension: So many things to take into account @.@ Not yet.
	// Cloning Pad: Too op
	// Ectobiology Apparatus: borked cloning pad. Free slime!
}
