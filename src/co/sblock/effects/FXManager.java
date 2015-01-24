package co.sblock.effects;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.reflections.Reflections;

import co.sblock.Sblock;
import co.sblock.effects.fx.SblockFX;
import co.sblock.module.Module;
import co.sblock.users.OnlineUser;
import co.sblock.utilities.captcha.Captcha;

/**
 * Module for managing all SblockEffects, relevant cooldowns, etc.
 * 
 * @author Dublek, Jikoo
 */
public class FXManager extends Module {

	private static FXManager instance;
	private Map<String, Class<? extends SblockFX>> validEffects; 
	private InvisibilityManager invisibilityManager;

	@Override
	protected void onEnable() {
		instance = this;
		validEffects = new HashMap<String, Class<? extends SblockFX>>();
		Reflections reflections = new Reflections("co.sblock.effects.fx");
		Set<Class<? extends SblockFX>> effects = reflections.getSubTypesOf(SblockFX.class);
		for (Class<? extends SblockFX> effect : effects) {
			if (Modifier.isAbstract(effect.getModifiers())) {
				continue;
			}
			try {
				SblockFX instance = effect.newInstance();
				validEffects.put(instance.getCanonicalName(), effect);
				for (String name : instance.getCommonNames()) {
					validEffects.put(name, effect);
				}
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException
					| InstantiationException e) {
				Sblock.getLog().severe("Keiko you fuck fix your FXManager");
				e.printStackTrace();
			}
		}
		invisibilityManager = new InvisibilityManager();
	}

	@Override
	protected void onDisable() {
		validEffects = null;
		instance = null;
	}

	public Map<String, Class<? extends SblockFX>> getValidEffects() {
		return validEffects;
	}

	/**
	 * Scans the entire inventory of a User for effects
	 * 
	 * @param u the User to scan
	 */
	public void fullEffectsScan(OnlineUser u) {
		Player p = u.getPlayer();
		PlayerInventory pInv = p.getInventory();
		ItemStack[] iS = pInv.getContents();
		ItemStack[] iSA = pInv.getArmorContents();
		ArrayList<String> playerLore = new ArrayList<String>();
		HashMap<String, SblockFX> output = new HashMap<String, SblockFX>();

		u.removeAllEffects();

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
		for (String line : playerLore) { // Removes all invalid lore
			boolean valid = false;
			for (String validName : validEffects.keySet()) {
				if (line.equalsIgnoreCase(validName)) {
					line = validName;
					valid = true;
					break;
				}
			}
			if (!valid) {
				continue;
			}
			try {
				SblockFX newEffect = validEffects.get(line).newInstance();
				if (output.containsKey(newEffect.getCanonicalName())) {
					output.get(newEffect.getCanonicalName()).setMultiplier(
							output.get(newEffect.getCanonicalName()).getMultiplier() + 1);
				} else {
					output.put(newEffect.getCanonicalName(), newEffect);
				}
			} catch (InstantiationException | IllegalAccessException e) {
				Sblock.getLog().severe("Keiko you fuck fix your fullEffectsScan");
				e.printStackTrace();
			}
		}
		u.setAllEffects(output);
	}

	/**
	 * Scans a specific ItemStack for all valid Effects
	 * 
	 * @param iS the ItemStack to scan
	 */
	public HashMap<String, SblockFX> itemScan(ItemStack iS) {
		ArrayList<String> playerLore = new ArrayList<String>();
		HashMap<String, SblockFX> output = new HashMap<String, SblockFX>();

		if (iS != null) {
			if (iS.hasItemMeta() && iS.getItemMeta().hasLore() && !(Captcha.isCard(iS))) {
				playerLore.addAll(iS.getItemMeta().getLore());
			}
		}
		for (String line : playerLore) {
			boolean valid = false;
			for (String validName : validEffects.keySet()) {
				if (line.equalsIgnoreCase(validName)) {
					line = validName;
					valid = true;
					break;
				}
			}
			if (!valid) {
				continue;
			}
			try {
				SblockFX newEffect = validEffects.get(line).newInstance();
					if (output.containsKey(newEffect.getCanonicalName())) {
						output.get(newEffect.getCanonicalName()).setMultiplier(
								output.get(newEffect.getCanonicalName()).getMultiplier() + 1);
					}
					else {
						output.put(newEffect.getCanonicalName(), newEffect);
					}
			} catch (InstantiationException | IllegalAccessException e) {
				Sblock.getLog().severe("Keiko you fuck fix your itemScan");
				e.printStackTrace();
			}
		}
		return output;
	}

	public InvisibilityManager getInvisibilityManager() {
		return invisibilityManager;
	}

	@Override
	protected String getModuleName() {
		return "Sblock FX";
	}

	public static FXManager getInstance() {
		return instance;
	}
}
