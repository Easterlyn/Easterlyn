/**
 * 
 */
package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Jikoo
 *
 */
public enum MachineType {

	ALCHEMITER("alc"), APPEARIFIER("app"), COMPUTER("cpu"), CRUXTRUDER("crx"), INTELLIBEAM_LASERSTATION("il"),
	PERFECTLY_GENERIC_OBJECT("pgo"), PUNCH_DESIGNIX("pd"), SENDIFICATOR("snd"), TOTEM_LATHE("tl"), ANY("NO.");

	String type;

	private MachineType(String s) {
		type = s;
	}

	public String getAbbreviation() {
		return type;
	}

	public static MachineType getType(String type) {
		for (MachineType m : MachineType.values()) {
			if (m.getAbbreviation().equals(type)) {
				return m;
			}
		}
		try {
			return MachineType.valueOf(type);
		} catch (IllegalArgumentException e) {
			Sblogger.warning("Machines", "Invalid machine type " + type);
			return null;
		}
	}

	public String getData(BlockPlaceEvent e) {
		if (this != MachineType.PERFECTLY_GENERIC_OBJECT) {
			return e.getPlayer().getName();
		}
		return e.getBlockAgainst().getTypeId() + ":" + e.getBlockAgainst().getData();
	}

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
