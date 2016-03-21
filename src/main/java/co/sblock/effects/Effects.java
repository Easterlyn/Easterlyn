package co.sblock.effects;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import org.reflections.Reflections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.BehaviorReactive;
import co.sblock.effects.effect.Effect;
import co.sblock.micromodules.Cooldowns;
import co.sblock.module.Module;
import co.sblock.users.UserAspect;
import co.sblock.utilities.InventoryUtils;
import co.sblock.utilities.NumberUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Module for Effects management.
 * 
 * @author Jikoo
 */
public class Effects extends Module {

	private final Map<String, Effect> effects = new HashMap<>();
	private final Multimap<Class<? extends Event>, Effect> active = HashMultimap.create();
	private final Multimap<Class<? extends Event>, Effect> reactive = HashMultimap.create();
	private final Pattern effectPattern = Pattern.compile("^\\" + ChatColor.COLOR_CHAR + "7(.*) ([IVXLCDM]+)$");

	private Cooldowns cooldowns;
	private boolean lock = false;

	public Effects(Sblock plugin) {
		super(plugin);
	}

	@Override
	protected void onEnable() {

		if (lock) {
			return;
		}

		lock = true;

		this.cooldowns = getPlugin().getModule(Cooldowns.class);
		Reflections reflections = new Reflections("co.sblock.effects.effect");
		Set<Class<? extends Effect>> allEffects = reflections.getSubTypesOf(Effect.class);
		for (Class<? extends Effect> effect : allEffects) {
			if (Modifier.isAbstract(effect.getModifiers())) {
				continue;
			}
			try {
				Constructor<? extends Effect> constructor = effect.getConstructor(getPlugin().getClass());
				Effect instance = constructor.newInstance(getPlugin());
				effects.put(instance.getName(), instance);
				if (instance instanceof BehaviorReactive) {
					for (Class<? extends Event> clazz : ((BehaviorReactive) instance).getApplicableEvents()) {
						reactive.put(clazz, instance);
					}
				} else if (instance instanceof BehaviorActive) {
					for (Class<? extends Event> clazz : ((BehaviorActive) instance).getApplicableEvents()) {
						active.put(clazz, instance);
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException
					| InstantiationException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		// Schedule effect scanning and application
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Player player : Bukkit.getOnlinePlayers()) {
					applyAllEffects(player);
				}
			}
		}.runTaskTimer(getPlugin(), 40, 40);
	}

	/**
	 * Gets a collection of all Effect names.
	 * 
	 * @return the Effect names
	 */
	public Collection<String> getAllEffectNames() {
		return effects.keySet();
	}

	/**
	 * Gets an effect by name.
	 * 
	 * @param effect the name of the Effect
	 * @return the Effect
	 */
	public Effect getEffect(String effect) {
		if (effects.containsKey(effect)) {
			return effects.get(effect);
		}
		return null;
	}

	/**
	 * Applies all effects to the given LivingEntity.
	 * 
	 * @param entity the LivingEntity
	 */
	public void applyAllEffects(LivingEntity entity) {
		if (!this.isEnabled()) {
			return;
		}
		for (Map.Entry<Effect, Integer> entry : getAllEffects(entity).entrySet()) {
			if (!(entry.getKey() instanceof BehaviorPassive)) {
				continue;
			}
			BehaviorCooldown cool = null;
			if (entry.getKey() instanceof BehaviorCooldown) {
				cool = (BehaviorCooldown) entry.getKey();
				if (cooldowns.getRemainder(entity, cool.getCooldownName()) > 0) {
					continue;
				}
			}
			((BehaviorPassive) entry.getKey()).applyEffect(entity, entry.getValue());
			if (cool != null) {
				cooldowns.addCooldown(entity, cool.getCooldownName(), cool.getCooldownDuration());
			}
		}
	}

	/**
	 * Scans the LivingEntity for effects and gets them.
	 * 
	 * @param entity the LivingEntity
	 */
	public Map<Effect, Integer> getAllEffects(LivingEntity entity) {
		boolean bypassMax = entity.hasPermission("sblock.effects.bypassmax");
		Map<Effect, Integer> applicableEffects = getEffects(bypassMax, entity.getEquipment().getArmorContents());

		getEffects(false, entity.getEquipment().getItemInMainHand(), entity.getEquipment().getItemInOffHand()).forEach(
				(key, value) -> applicableEffects.merge(key, value, (v1, v2) -> v1 + v2));

		if (!(entity instanceof Player)) {
			return applicableEffects;
		}

		// TODO Godtier effects
//		for (String effectType : user.getGodtierEffects()) {
//			Effect effect = getEffect(user.getUserAspect().name() + effectType);
//			if (effect == null) {
//				continue;
//			}
//			applicableEffects.merge(effect, 1, (v1, v2) -> v1 + v2);
//		}

		return applicableEffects;
	}

	/**
	 * Handles an event for an active or reactive Effect.
	 * 
	 * @param event the Event
	 * @param entity the LivingEntity
	 * @param reactive true if the Effect is reactive
	 */
	public void handleEvent(Event event, LivingEntity entity, boolean reactive) {
		if (!this.isEnabled()) {
			return;
		}
		boolean bypassMax = entity.hasPermission("sblock.effects.bypassmax");
		Map<Effect, Integer> effects;
		if (reactive) {
			effects = getEffects(bypassMax, entity.getEquipment().getArmorContents());
		} else if (event instanceof PlayerInteractEvent) {
			effects = getEffects(bypassMax, InventoryUtils.getHeldItem((PlayerInteractEvent) event));
		} else {
			effects = getEffects(bypassMax, entity.getEquipment().getItemInMainHand());
		}
		// TODO Godtier effects
//		if (entity instanceof Player) {
//			User user = users.getUser(entity.getUniqueId());
//				for (String effectType : user.getGodtierEffects()) {
//					Effect effect = getEffect(effectType);
//					if (effect == null || effect instanceof BehaviorPassive
//							|| !reactive && !(effect instanceof BehaviorReactive)) {
//						continue;
//					}
//					effects.merge(effect, 1, (v1, v2) -> v1 + v2);
//				}
//		}
		if (effects.isEmpty()) {
			return;
		}
		for (Class<? extends Event> clazz : reactive ? this.reactive.keySet() : active.keySet()) {
			if (!clazz.isAssignableFrom(event.getClass())) {
				continue;
			}
			for (Effect effect : reactive ? this.reactive.get(clazz) : active.get(clazz)) {
				if (!effects.containsKey(effect)) {
					continue;
				}
				if (effect instanceof BehaviorCooldown && cooldowns.getRemainder(entity, ((BehaviorCooldown) effect).getCooldownName()) > 0) {
					continue;
				}
				((BehaviorActive) effect).handleEvent(event, entity, effects.get(effect));
				if (effect instanceof BehaviorCooldown) {
					BehaviorCooldown cool = (BehaviorCooldown) effect;
					cooldowns.addCooldown(entity, cool.getCooldownName(), cool.getCooldownDuration());
				}
			}
		}
	}

	/**
	 * Gets all Effects and corresponding levels on the provided ItemStack.
	 * 
	 * @param bypassMax whether or not the maximum level for an effect can be bypassed
	 * @param items the item(s) to get effects from
	 * @return the Effects and corresponding levels
	 */
	public Map<Effect, Integer> getEffects(boolean bypassMax, ItemStack... items) {
		HashMap<Effect, Integer> applicableEffects = new HashMap<>();
		for (ItemStack item : items) {
			if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore() || Captcha.isCard(item)) {
				continue;
			}
			for (String lore : item.getItemMeta().getLore()) {
				Matcher match = effectPattern.matcher(lore);
				if (!match.find()) {
					continue;
				}
				String effectName = ChatColor.stripColor(match.group(1));
				if (!effects.containsKey(effectName)) {
					continue;
				}
				int level;
				try {
					level = NumberUtils.intFromRoman(match.group(2));
				} catch (NumberFormatException e) {
					continue;
				}
				Effect effect = effects.get(effectName);
				if (applicableEffects.containsKey(effect)) {
					level += applicableEffects.get(effect);
				}
				if (!bypassMax && level > effect.getMaxLevel()) {
					level = effect.getMaxTotalLevel();
				}
				applicableEffects.put(effect, level);
			}
		}
		return applicableEffects;
	}

	/**
	 * Organize and correct Effects in ItemStack lore.
	 * 
	 * @param lore the List of lore containing Effects
	 * @param ignoreCase whether lore matching should ignore case
	 * @param overwrite whether any duplicate Effects in toAdd should be ignored
	 * @param cap whether Effect levels should be capped to the maximum
	 * @param toAdd additional Strings to be merged with the lore
	 * 
	 * @return the organized lore
	 */
	public List<String> organizeEffectLore(List<String> lore, boolean ignoreCase,
			boolean overwrite, boolean cap, String... toAdd) {
		ArrayList<String> oldLore = new ArrayList<>();
		if (lore != null) {
			oldLore.addAll(lore);
		}
		HashMap<Effect, Integer> applicableEffects = new HashMap<>();
		Iterator<String> iterator = oldLore.iterator();
		while (iterator.hasNext()) {
			Pair<Effect, Integer> pair = getEffectFromLore(iterator.next(), false);
			if (pair == null) {
				continue;
			}
			iterator.remove();
			if (applicableEffects.containsKey(pair.getLeft())) {
				applicableEffects.put(pair.getLeft(), applicableEffects.get(pair.getLeft()) + pair.getRight());
				continue;
			}
			applicableEffects.put(pair.getLeft(), pair.getRight());
		}

		for (String string : toAdd) {
			Pair<Effect, Integer> pair = getEffectFromLore(string, ignoreCase);
			if (pair == null) {
				oldLore.add(string);
				continue;
			}
			if (!overwrite && applicableEffects.containsKey(pair.getLeft())) {
				applicableEffects.put(pair.getLeft(), applicableEffects.get(pair.getLeft()) + pair.getRight());
				continue;
			}
			applicableEffects.put(pair.getLeft(), pair.getRight());
		}

		ArrayList<String> newLore = new ArrayList<>();
		for (Map.Entry<Effect, Integer> entry : applicableEffects.entrySet()) {
			if (entry.getValue() < 1) {
				continue;
			}
			if (cap) {
				entry.setValue(Math.min(entry.getKey().getMaxLevel(), entry.getValue()));
			}
			newLore.add(new StringBuilder().append(ChatColor.GRAY)
					.append(entry.getKey().getName()).append(' ')
					.append(NumberUtils.romanFromInt(entry.getValue())).toString());
		}
		newLore.addAll(oldLore);

		return newLore;
	}

	/**
	 * Gets the Effect and level represented by a String in an ItemStack's lore.
	 * 
	 * @param lore the String
	 * @param ignoreCase if case should be ignored when matching Effect
	 */
	public Pair<Effect, Integer> getEffectFromLore(String lore, boolean ignoreCase) {
		Matcher match = effectPattern.matcher(lore);
		if (!match.find()) {
			return null;
		}
		lore = ChatColor.stripColor(match.group(1));
		Effect effect = null;
		if (effects.containsKey(lore)) {
			effect = effects.get(lore);
		} else {
			if (!ignoreCase) {
				return null;
			}
			for (Map.Entry<String, Effect> entry : effects.entrySet()) {
				if (entry.getKey().equalsIgnoreCase(lore)) {
					effect = entry.getValue();
					break;
				}
			}
			if (effect == null) {
				return null;
			}
		}
		try {
			return new ImmutablePair<>(effect, NumberUtils.intFromRoman(match.group(2)));
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * Checks if the Effect provided is on cooldown for the given UUID.
	 * 
	 * @param uuid the UUID
	 * @param effect the effect
	 * @return true if the Effect is on cooldown.
	 */
	public boolean isOnCooldown(LivingEntity entity, Effect effect) {
		if (!(effect instanceof BehaviorCooldown)) {
			return false;
		}
		return cooldowns.getRemainder(entity, ((BehaviorCooldown) effect).getCooldownName()) > 0;
	}

	/**
	 * Starts the cooldown for the given UUID and Effect.
	 * 
	 * @param uuid the UUID
	 * @param effect the Effect
	 */
	public void startCooldown(LivingEntity entity, Effect effect) {
		if (!(effect instanceof BehaviorCooldown)) {
			return;
		}
		BehaviorCooldown cool = (BehaviorCooldown) effect;
		cooldowns.addCooldown(entity, cool.getCooldownName(), cool.getCooldownDuration());
	}

	public List<Effect> getGodtierEffects(UserAspect aspect) {
		ArrayList<Effect> applicableEffects = new ArrayList<>();
		for (Effect effect : effects.values()) {
			if (!(effect instanceof BehaviorGodtier)) {
				continue;
			}
			if (((BehaviorGodtier) effect).getAspects().contains(aspect)) {
				applicableEffects.add(effect);
			}
		}
		return applicableEffects;
	}

	@Override
	protected void onDisable() { }

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "Effects";
	}

}
