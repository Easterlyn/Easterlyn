package co.sblock.effects;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import org.reflections.Reflections;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import co.sblock.Sblock;
import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.effects.effect.EffectBehaviorCooldown;
import co.sblock.effects.effect.EffectBehaviorPassive;
import co.sblock.effects.effect.EffectBehaviorReactive;
import co.sblock.module.Module;
import co.sblock.users.OfflineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Users;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.general.Cooldowns;
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
	private final Multimap<Class<? extends Event>, Effect> reactive = HashMultimap.create();
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
				if (instance instanceof EffectBehaviorReactive) {
					for (Class<? extends Event> clazz : ((EffectBehaviorReactive) instance).getApplicableEvents()) {
						reactive.put(clazz, instance);
					}
				} else if (instance instanceof EffectBehaviorActive) {
					for (Class<? extends Event> clazz : ((EffectBehaviorActive) instance).getApplicableEvents()) {
						active.put(clazz, instance);
					}
				}
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException
					| InstantiationException e) {
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
		}.runTaskTimer(Sblock.getInstance(), 40, 40);
	}

	/**
	 * Gets an effect by name.
	 * 
	 * @param effect the name of the Effect
	 * @return the Effect
	 */
	public Effect getEffect(String effect) {
		return effects.get(effect);
	}

	/**
	 * Applies all effects to the given Player.
	 * 
	 * @param player the Player
	 */
	public void applyAllEffects(Player player) {
		for (Map.Entry<Effect, Integer> entry : getAllEffects(player).entrySet()) {
			if (!(entry.getKey() instanceof EffectBehaviorPassive)) {
				continue;
			}
			EffectBehaviorCooldown cool = null;
			if (entry.getKey() instanceof EffectBehaviorCooldown) {
				cool = (EffectBehaviorCooldown) entry.getKey();
				if (Cooldowns.getInstance().getRemainder(player.getUniqueId(), cool.getCooldownName()) > 0) {
					continue;
				}
			}
			((EffectBehaviorPassive) entry.getKey()).applyEffect(player, entry.getValue());
			if (cool != null) {
				Cooldowns.getInstance().addCooldown(player.getUniqueId(), cool.getCooldownName(), cool.getCooldownDuration());
			}
		}
	}

	/**
	 * Scans the Player for effects and gets them.
	 * 
	 * @param player the Player
	 */
	public Map<Effect, Integer> getAllEffects(Player player) {
		HashMap<Effect, Integer> applicableEffects = new HashMap<>();

		for (ItemStack item: player.getInventory().getArmorContents()) {
			for (Map.Entry<Effect, Integer> entry : getEffects(item).entrySet()) {
				if (applicableEffects.containsKey(entry.getKey())) {
					applicableEffects.put(entry.getKey(), applicableEffects.get(entry.getKey()) + entry.getValue());
				} else {
					applicableEffects.put(entry.getKey(), entry.getValue());
				}
			}
		}

		for (Map.Entry<Effect, Integer> entry : getEffects(player.getItemInHand()).entrySet()) {
			if (applicableEffects.containsKey(entry.getKey())) {
				applicableEffects.put(entry.getKey(), applicableEffects.get(entry.getKey()) + entry.getValue());
			} else {
				applicableEffects.put(entry.getKey(), entry.getValue());
			}
		}

		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		if (user.getProgression().ordinal() >= ProgressionState.GODTIER.ordinal()) {
			for (String effectType : new String[] {"::ACTIVE", "::PASSIVE", "::REACTIVE"}) {
				Effect effect = getEffect(user.getUserAspect().name() + effectType);
				if (effect != null) {
					if (applicableEffects.containsKey(effect)) {
						applicableEffects.put(effect, applicableEffects.get(effect) + 1);
					} else {
						applicableEffects.put(effect, 1);
					}
				}
			}
		}

		return applicableEffects;
	}

	/**
	 * Handles an event for an active or reactive Effect.
	 * 
	 * @param event the Event
	 * @param player the Player
	 * @param reactive true if the Effect is reactive
	 */
	public void handleEvent(Event event, Player player, boolean reactive) {
		Map<Effect, Integer> effects;
		if (reactive) {
			effects = getEffects(player.getInventory().getArmorContents());
		} else {
			effects = getEffects(player.getItemInHand());
		}
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		if (user.getProgression().ordinal() >= ProgressionState.GODTIER.ordinal()) {
			Effect effect = Effects.getInstance().getEffect(user.getUserAspect().name() + (reactive ? "::REACTIVE" : "::ACTIVE"));
			if (effect != null) {
				if (effects.containsKey(effect)) {
					effects.put(effect, effects.get(effect) + 1);
				} else {
					effects.put(effect, 1);
				}
			}
		}
		if (effects.isEmpty()) {
			return;
		}
		for (Class<? extends Event> clazz : reactive ? this.reactive.keySet() : active.keySet()) {
			if (!clazz.isAssignableFrom(event.getClass())) {
				continue;
			}
			for (Effect effect : active.get(clazz)) {
				if (!effects.containsKey(effect)) {
					continue;
				}
				if (effect instanceof EffectBehaviorCooldown && Cooldowns.getInstance().getRemainder(player.getUniqueId(), ((EffectBehaviorCooldown) effect).getCooldownName()) > 0) {
					continue;
				}
				((EffectBehaviorActive) effect).handleEvent(event, player, effects.get(effect));
				if (effect instanceof EffectBehaviorCooldown) {
					EffectBehaviorCooldown cool = (EffectBehaviorCooldown) effect;
					Cooldowns.getInstance().addCooldown(player.getUniqueId(), cool.getCooldownName(), cool.getCooldownDuration());
				}
			}
		}
	}

	/**
	 * Gets all Effects and corresponding levels on the provided ItemStack.
	 * 
	 * @param item the ItemStack
	 * @return the Effects and corresponding levels
	 */
	public Map<Effect, Integer> getEffects(ItemStack... items) {
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
		}
		return applicableEffects;
	}

	/**
	 * Checks if the Effect provided is on cooldown for the given UUID.
	 * 
	 * @param uuid the UUID
	 * @param effect the effect
	 * @return true if the Effect is on cooldown.
	 */
	public boolean isOnCooldown(UUID uuid, Effect effect) {
		if (!(effect instanceof EffectBehaviorCooldown)) {
			return false;
		}
		return Cooldowns.getInstance().getRemainder(uuid, ((EffectBehaviorCooldown) effect).getCooldownName()) > 0;
	}

	/**
	 * Starts the cooldown for the given UUID and Effect.
	 * 
	 * @param uuid the UUID
	 * @param effect the Effect
	 */
	public void startCooldown(UUID uuid, Effect effect) {
		if (!(effect instanceof EffectBehaviorCooldown)) {
			return;
		}
		EffectBehaviorCooldown cool = (EffectBehaviorCooldown) effect;
		Cooldowns.getInstance().addCooldown(uuid, cool.getCooldownName(), cool.getCooldownDuration());
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
