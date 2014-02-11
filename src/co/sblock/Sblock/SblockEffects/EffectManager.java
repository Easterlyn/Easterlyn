package co.sblock.Sblock.SblockEffects;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.UserData.SblockUser;

public class EffectManager {

	private final JavaPlugin plugin;
	
	public EffectManager() {
		plugin = Sblock.getInstance();
	}

	/**
	 * Scans the entire inventory of a Player for effects
	 * 
	 * @param p the Player to scan
	 * @return the HashMap of PassiveEffects the Player has
	 */
	public static HashMap<PassiveEffect, Integer> passiveScan(Player p) {
		HashMap<PassiveEffect, Integer> effects = new HashMap<PassiveEffect, Integer>();
		PlayerInventory pInv = p.getInventory();
		ItemStack[] iS = pInv.getContents();
		ItemStack[] iSA = pInv.getArmorContents();
		ArrayList<String> playerLore = new ArrayList<String>();
		ArrayList<String> tempLore = new ArrayList<String>();
		
		for (ItemStack i : iS) { // Inventory
			if (i != null) {
				// System.out.println(i.getType().toString());
				if (i.hasItemMeta() && i.getItemMeta().hasLore()) {
					tempLore.addAll(i.getItemMeta().getLore());
					playerLore.addAll(i.getItemMeta().getLore());
				}
			}
		}
		for (ItemStack i : iSA) { // Armor
			// System.out.println(i.getType().toString());
			if (i != null && i.hasItemMeta() && i.getItemMeta().hasLore()) {
				tempLore.addAll(i.getItemMeta().getLore());
				playerLore.addAll(i.getItemMeta().getLore());
			}
		}
		for (String s : tempLore) {
			if (!PassiveEffect.isValidEffect(s)) {
				playerLore.remove(playerLore.indexOf(s));
			}
		}
		for (String s : playerLore) {
			PassiveEffect pE = PassiveEffect.getEffect(s);
			if(!effects.containsKey(pE)) {
				effects.put(pE, 0);
			}
			else {
				effects.put(pE, effects.get(pE) + 1);
			}
		}		
		return effects;
	}
	
	/**
	 * Scans a specific ItemStack for all valid PassiveEffects
	 * 
	 * @param iS the ItemStack to scan
	 * @return a HashMap of all PassiveEffect on the ItemStack
	 */
	public static HashMap<PassiveEffect, Integer> itemScan(Item i) {
		ItemStack iS = i.getItemStack();
		HashMap<PassiveEffect, Integer> effects = new HashMap<PassiveEffect, Integer>();
		ArrayList<String> playerLore = new ArrayList<String>();
		ArrayList<String> tempLore = new ArrayList<String>();
		
		if (iS.hasItemMeta() && iS.getItemMeta().hasLore()) {
			tempLore.addAll(iS.getItemMeta().getLore());
			playerLore.addAll(iS.getItemMeta().getLore());
		}
		for (String s : tempLore) {
			if (!PassiveEffect.isValidEffect(s)) {
				playerLore.remove(playerLore.indexOf(s));
			}
		}
		for (String s : playerLore) {
			PassiveEffect pE = PassiveEffect.getEffect(s);
			if(!effects.containsKey(pE)) {
				effects.put(pE, 0);
			}
			else {
				effects.put(pE, effects.get(pE) + 1);
			}
		}
		
		return effects;
	}
	
	public ArrayList<String> scan(Player p) {
		PlayerInventory pInv = p.getInventory();
		ItemStack[] iS = pInv.getContents();
		ItemStack[] iSA = pInv.getArmorContents();
		ArrayList<String> playerLore = new ArrayList<String>();
		ArrayList<String> tempLore = new ArrayList<String>();
		for (ItemStack i : iS) { // Inventory
			if (i != null) {
				// System.out.println(i.getType().toString());
				if (i.hasItemMeta() && i.getItemMeta().hasLore()) {
					tempLore.addAll(i.getItemMeta().getLore());
					playerLore.addAll(i.getItemMeta().getLore());
				}
			}
		}
		for (ItemStack i : iSA) { // Armor
			// System.out.println(i.getType().toString());
			if (i != null && i.hasItemMeta() && i.getItemMeta().hasLore()) {
				tempLore.addAll(i.getItemMeta().getLore());
				playerLore.addAll(i.getItemMeta().getLore());
			}
		}
		for (String s : tempLore) {
			if (!PassiveEffect.isValidEffect(s)) {
				playerLore.remove(playerLore.indexOf(s));
			}
		}
		if (SblockEffects.verbose && playerLore.size() > 0) {
			plugin.getLogger().info(p.getName() + playerLore);
		}
		return playerLore;
	}
	
	
	public ArrayList<String> activeScan(Player p) {
		ArrayList<String> lore = new ArrayList<String>();
		ArrayList<String> tempLore = new ArrayList<String>();
		ItemStack iS = p.getItemInHand();
		if (iS != null && iS.hasItemMeta() && iS.getItemMeta().hasLore()) {
			lore.addAll(iS.getItemMeta().getLore());
			tempLore.addAll(iS.getItemMeta().getLore());
		}
		for (String s : tempLore) {
			if (!ActiveEffect.isValidEffect(s)) {
				lore.remove(lore.indexOf(s));
			}
		}
		return lore;
	}

	public void applyActiveRightClickEffects(ArrayList<String> effects, Player p) {
		ActiveEffect aE;
		for (String lore : effects) {
			if (ActiveEffect.isValidEffect(lore)) {
				try {
					aE = ActiveEffect.valueOf(lore);
					aE.getRightClickEffect(p);
				} catch (IllegalArgumentException e) {
					for (ActiveEffect act : ActiveEffect.values()) {
						if (act.getLoreText().equalsIgnoreCase(lore)) {
							act.getRightClickEffect(p);
						}
					}
				}
			}
		}
	}

	public void applyActiveDamageEffects(ArrayList<String> effects, Player p, Player target) {
		ActiveEffect aE;
		for (String lore : effects) {
			if (ActiveEffect.isValidEffect(lore)) {
				try {
					aE = ActiveEffect.valueOf(lore);
					aE.getDamageEffect(p, target);
				} catch (IllegalArgumentException e) {
					for (ActiveEffect act : ActiveEffect.values()) {
						if (act.getLoreText().equalsIgnoreCase(lore)) {
							act.getDamageEffect(p, target);
						}
					}
				}
			}
		}
	}

	/**
	 * Applies all PassiveEffects to a Player based on
	 * the HashMap stored in their SblockUser
	 * 
	 * @param user the SblockUSer to apply PassiveEffects to
	 */
	public static void applyPassiveEffects(SblockUser user) {
		HashMap<PassiveEffect, Integer> effects = user.getPassiveEffects();
		for (PassiveEffect pE : effects.keySet()) {
			PassiveEffect.applyEffect(user.getPlayer(), pE, effects.get(pE));
		}
	}
}
