package co.sblock.fx;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.reflections.Reflections;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.effects.PassiveEffect;
import co.sblock.users.User;
import co.sblock.utilities.captcha.Captcha;

public class FXManager {

	private static Map<String, Class<? extends SblockFX>> validEffects; 

	public FXManager() {
		validEffects = new HashMap<String, Class<? extends SblockFX>>();

		Reflections reflections = new Reflections("co.sblock.fx");
		Set<Class<? extends SblockFX>> effects = reflections.getSubTypesOf(SblockFX.class);
		for (Class<? extends SblockFX> effect : effects) {
			try {
				validEffects.put((String) effect.getMethod("getEffectName").invoke(effect), effect);
			} catch (IllegalAccessException | IllegalArgumentException | 
					InvocationTargetException | NoSuchMethodException | SecurityException e) {
				Sblock.getLog().severe("Keiko you fuck fix your FXManager");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Scans the entire inventory of a User for effects
	 * 
	 * @param u the User to scan
	 */
	public static void fullEffectsScan(User u) {
		Player p = u.getPlayer();
		HashMap<String, SblockFX> effects = new HashMap<String, SblockFX>();
		PlayerInventory pInv = p.getInventory();
		ItemStack[] iS = pInv.getContents();
		ItemStack[] iSA = pInv.getArmorContents();
		ArrayList<String> playerLore = new ArrayList<String>();

		for (ItemStack i : iS) { // Inventory
			if (i != null) {
				// System.out.println(i.getType().toString());
				if (i.hasItemMeta() && i.getItemMeta().hasLore() && !(Captcha.isCard(i))) {
					playerLore.addAll(i.getItemMeta().getLore());
				}
			}
		}
		for (ItemStack i : iSA) { // Armor
			// System.out.println(i.getType().toString());
			if (i != null && i.hasItemMeta() && i.getItemMeta().hasLore() && !(Captcha.isCard(i))) {
				playerLore.addAll(i.getItemMeta().getLore());
			}
		}
		for (String s : playerLore) { //Removes all invalid lore
			if (validEffects.containsKey(s)) {
				SblockFX newEffect;
				try {
					newEffect = validEffects.get(s).newInstance();
					u.addEffect(newEffect);
				} catch (InstantiationException | IllegalAccessException e) {
					Sblock.getLog().severe("Keiko you fuck fix your effectsScan");
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * Scans a specific ItemStack for all valid Effects
	 * 
	 * @param iS the ItemStack to scan
	 */
	public static HashMap<String, SblockFX> itemScan(ItemStack iS) {
		ArrayList<String> playerLore = new ArrayList<String>();
		HashMap<String, SblockFX> output = new HashMap<String, SblockFX>();

		if (iS != null) {
			if (iS.hasItemMeta() && iS.getItemMeta().hasLore() && !(Captcha.isCard(iS))) {
				playerLore.addAll(iS.getItemMeta().getLore());
			}
		}
		for (String s : playerLore) {
			if (validEffects.containsKey(s)) {
				SblockFX newEffect;
				try {
					newEffect = validEffects.get(s).newInstance();
					output.put(newEffect.getEffectName(), newEffect);
				} catch (InstantiationException | IllegalAccessException e) {
					Sblock.getLog().severe("Keiko you fuck fix your itemScan");
					e.printStackTrace();
				}
			}
		}
		return output;
	}	
}
