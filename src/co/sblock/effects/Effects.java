package co.sblock.effects;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;

import org.reflections.Reflections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.module.Module;
import co.sblock.utilities.general.Roman;

import net.md_5.bungee.api.ChatColor;

/**
 * Module for Effects management.
 * 
 * @author Jikoo
 */
public class Effects extends Module {

	private static Effects instance;
	private final Map<String, Effect> effects = new HashMap<>();
	private final Multimap<Class<? extends Event>, Effect> active = HashMultimap.create();
	private final Pattern effectPattern = Pattern.compile("^\\" + ChatColor.COLOR_CHAR + "[A-FK-ORa-fk-or0-9](.*) ([IVXLCDM]+)$");

	@Override
	protected void onEnable() {
		instance = this;
		Reflections reflections = new Reflections("co.sblock.effects.effects");
		Set<Class<? extends Effect>> allEffects = reflections.getSubTypesOf(Effect.class);
		for (Class<? extends Effect> effect : allEffects) {
			if (Modifier.isAbstract(effect.getModifiers())) {
				continue;
			}
			try {
				Effect instance = effect.newInstance();
				for (String name : instance.getNames()) {
					effects.put(name, instance);
				}
				if (instance instanceof EffectBehaviorActive) {
					for (Class<? extends Event> clazz : ((EffectBehaviorActive) instance).getApplicableEvents()) {
						active.put(clazz, instance);
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException
					| InstantiationException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Gets all Effects and corresponding levels on the provided ItemStack.
	 * 
	 * @param item the ItemStack
	 * @return the Effects and corresponding levels
	 */
	public Map<Effect, Integer> getEffects(ItemStack item) {
		HashMap<Effect, Integer> applicableEffects = new HashMap<>();
		if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore()) {
			return applicableEffects;
		}
		for (String lore : item.getItemMeta().getLore()) {
			Matcher match = effectPattern.matcher(lore);
			if (!match.find()) {
				continue;
			}
			String effect = ChatColor.stripColor(match.group(1));
			if (!effects.containsKey(effect)) {
				continue;
			}
			int level;
			try {
				level = Roman.fromString(match.group(2));
			} catch (NumberFormatException e) {
				continue;
			}
			applicableEffects.put(effects.get(effect), level);
		}
		return applicableEffects;
	}

	@Override
	protected void onDisable() {
		instance = null;
	}

	@Override
	protected String getModuleName() {
		return "Effects";
	}

	public static Effects getInstance() {
		return instance;
	}

}
