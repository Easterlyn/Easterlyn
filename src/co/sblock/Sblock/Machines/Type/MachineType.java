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

	ALCHEMITER("alc"), APPEARIFIER("app"), COMPUTER("cpu"),
	CRUXTRUDER("crx"), INTELLIBEAM_LASERSTATION("il"),
	PERFECTLY_GENERIC_OBJECT("pgo"), PUNCH_DESIGNIX("pd"),
	SENDIFICATOR("snd"), TOTEM_LATHE("tl"),
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
		case COMPUTER:
			is = new ItemStack(Material.JUKEBOX);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Computer");
			// I'm not setting lore (yet) because carrying around a desktop doesn't seem like good global access.
			is.setItemMeta(im);
			break;
		case PERFECTLY_GENERIC_OBJECT:
			is = new ItemStack(Material.DIRT);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Perfectly generic object");
			is.setItemMeta(im);
			break;
		case TRANSPORTALIZER:
			is = new ItemStack(Material.CHEST);
			im = is.getItemMeta();
			im.setDisplayName(ChatColor.WHITE + "Transportalizer");
			is.setItemMeta(im);
		case ANY:
			@SuppressWarnings("static-access")
			MachineType[] types = this.values();
			return types[(int) Math.random() * types.length].getUniqueDrop();
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
