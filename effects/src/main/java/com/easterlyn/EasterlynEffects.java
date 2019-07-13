package com.easterlyn;

import com.easterlyn.effect.Effect;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.tuple.Pair;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reflections.Reflections;

public class EasterlynEffects extends JavaPlugin {

	private Map<String, Effect> effects;
	private final Pattern effectPattern = Pattern.compile("^\\" + ChatColor.COLOR_CHAR + "7(.*) ([IVXLCDM]+)$");
	private final Map<EquipmentSlot, Function<EntityEquipment, ItemStack>> equipmentSlotMap = new HashMap<>();

	@Override
	public void onEnable() {
		effects = new HashMap<>();

		equipmentSlotMap.put(EquipmentSlot.CHEST, EntityEquipment::getChestplate);
		equipmentSlotMap.put(EquipmentSlot.FEET, EntityEquipment::getBoots);
		equipmentSlotMap.put(EquipmentSlot.HAND, EntityEquipment::getItemInMainHand);
		equipmentSlotMap.put(EquipmentSlot.HEAD, EntityEquipment::getHelmet);
		equipmentSlotMap.put(EquipmentSlot.LEGS, EntityEquipment::getItemInOffHand);
		equipmentSlotMap.put(EquipmentSlot.OFF_HAND, EntityEquipment::getItemInOffHand);

		// TODO green thumb, autotorch, pshoooot
		Reflections reflections = new Reflections("com.easterlyn.effect", getClassLoader());
		Set<Class<? extends Effect>> allEffects = reflections.getSubTypesOf(Effect.class);
		for (Class<? extends Effect> effect : allEffects) {
			if (Modifier.isAbstract(effect.getModifiers())) {
				continue;
			}
			try {
				Constructor<? extends Effect> constructor = effect.getConstructor(this.getClass());
				Effect instance = constructor.newInstance(this);
				effects.put(instance.getName(), instance);
			} catch (IllegalAccessException | IllegalArgumentException | SecurityException
					| InstantiationException | InvocationTargetException | NoSuchMethodException e) {
				e.printStackTrace();
			}
		}

		// Register events for effects
		BlockBreakEvent.getHandlerList().register(new SimpleListener<>(BlockBreakEvent.class,
				event -> applyEffects(event.getPlayer(), event), this));
		FurnaceExtractEvent.getHandlerList().register(new SimpleListener<>(FurnaceExtractEvent.class,
				event -> applyEffects(event.getPlayer(), event), this));
		EntityDamageEvent.getHandlerList().register(new SimpleListener<>(EntityDamageEvent.class, event -> {
			if (event.getEntity() instanceof LivingEntity) {
				applyEffects((LivingEntity) event.getEntity(), event);
			}
			if (event instanceof EntityDamageByEntityEvent && ((EntityDamageByEntityEvent) event).getDamager() instanceof LivingEntity) {
				applyEffects((LivingEntity) ((EntityDamageByEntityEvent) event).getDamager(), event);
			}
		}, this));
		PlayerInteractEvent.getHandlerList().register(new SimpleListener<>(PlayerInteractEvent.class,
				event -> applyEffects(event.getPlayer(), event), this));
		PlayerChangedWorldEvent.getHandlerList().register(new SimpleListener<>(PlayerChangedWorldEvent.class,
				event -> applyEffects(event.getPlayer(), event), this));

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class, event -> {
			if (event.getPlugin() instanceof EasterlynCore) {
				register((EasterlynCore) event.getPlugin());
			}
		}, this));


	}

	@Override
	public void onDisable() {
		// Clear effects so worth modifier will be empty
		effects.clear();
	}

	private void register(@NotNull EasterlynCore plugin) {
		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.effect.command");

		EconomyUtil.addWorthModifier(itemStack -> getEffects(true, itemStack).keySet().stream()
				.mapToDouble(Effect::getCost).sum());
	}

	/**
	 * Gets all Effects and corresponding levels on the provided ItemStack(s).
	 *
	 * @param bypass whether or not the maximum level for an effect can be bypassed
	 * @param items the item(s) to get effects from
	 * @return the Effects and corresponding levels
	 */
	@NotNull
	private Map<Effect, Integer> getEffects(boolean bypass, ItemStack... items) {
		Map<Effect, Integer> applicableEffects = new HashMap<>();
		for (ItemStack item : items) {
			if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()
					|| item.getItemMeta().getLore() == null) {
				continue;
			}
			for (String lore : item.getItemMeta().getLore()) {
				Pair<Effect, Integer> pair = getEffectFromLore(lore, false);
				if (pair == null) {
					continue;
				}
				int level = pair.getRight();
				if (applicableEffects.containsKey(pair.getLeft())) {
					level += applicableEffects.get(pair.getLeft());
				}
				if (!bypass && level > pair.getLeft().getMaxLevel()) {
					level = pair.getLeft().getMaxTotalLevel();
				}
				applicableEffects.put(pair.getLeft(), level);
			}
		}
		return applicableEffects;
	}

	/**
	 * Applies all effects to the given LivingEntity.
	 *
	 * @param entity the LivingEntity
	 */
	private void applyEffects(@NotNull LivingEntity entity, @Nullable Event event) {
		EntityEquipment equipment = entity.getEquipment();
		if (equipment == null) {
			return;
		}
		Map<Effect, Integer> effects = new HashMap<>();
		equipmentSlotMap.forEach((equipmentSlot, equipmentSlotFunction) ->
				getEffects(false, equipmentSlotFunction.apply(equipment)).forEach((effect, level) -> {
					if (!effect.getTarget().apply(equipmentSlot)) {
						return;
					}
					effects.compute(effect, (value, current) -> {
						if (current == null) {
							current = level;
						} else {
							current += level;
						}
						return current > effect.getMaxTotalLevel() ? effect.getMaxTotalLevel() : current;
					});
				}));
		effects.forEach((effect, level) -> effect.applyEffect(entity, level, event));
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
	@NotNull
	public List<String> organizeEffectLore(@NotNull List<String> lore, boolean ignoreCase,
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
			newLore.add(ChatColor.GRAY + entry.getKey().getName() + ' ' + NumberUtil.romanFromInt(entry.getValue()));
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
	@Nullable
	public Pair<Effect, Integer> getEffectFromLore(@NotNull String lore, boolean ignoreCase) {
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
			return new Pair<>(effect, NumberUtil.intFromRoman(match.group(2)));
		} catch (NumberFormatException e) {
			return null;
		}
	}

}
