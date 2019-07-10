package com.easterlyn;

import com.easterlyn.effect.Effect;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.event.SimpleListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.player.PlayerEvent;
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
		EntityDamageByEntityEvent.getHandlerList().register(new SimpleListener<>(EntityDamageByEntityEvent.class, event -> {
			if (event.getEntity() instanceof LivingEntity) {
				applyEffects((LivingEntity) event.getDamager(), event);
			}
		}, this));
		PlayerInteractEvent.getHandlerList().register(new SimpleListener<>(PlayerInteractEvent.class,
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

	private void register(EasterlynCore plugin) {
		plugin.registerCommands(getClassLoader(), "com.easterlyn.effect.command");
		// TODO fx command

		EconomyUtil.addWorthModifier(itemStack -> getEffects(true, itemStack).keySet().stream()
				.mapToDouble(Effect::getCost).sum());
	}

	private void registerEvent(PlayerEvent playerEvent, HandlerList handlerList) {
		handlerList.register(new SimpleListener<>(playerEvent.getClass(),
				event -> applyEffects(event.getPlayer(), event), this));
	}

	/**
	 * Gets all Effects and corresponding levels on the provided ItemStack(s).
	 *
	 * @param bypass whether or not the maximum level for an effect can be bypassed
	 * @param items the item(s) to get effects from
	 * @return the Effects and corresponding levels
	 */
	private Map<Effect, Integer> getEffects(boolean bypass, ItemStack... items) {
		Map<Effect, Integer> applicableEffects = new HashMap<>();
		for (ItemStack item : items) {
			if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()
					|| item.getItemMeta().getLore() == null) {
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
					level = NumberUtil.intFromRoman(match.group(2));
				} catch (NumberFormatException e) {
					continue;
				}
				Effect effect = effects.get(effectName);
				if (applicableEffects.containsKey(effect)) {
					level += applicableEffects.get(effect);
				}
				if (!bypass && level > effect.getMaxLevel()) {
					level = effect.getMaxTotalLevel();
				}
				applicableEffects.put(effect, level);
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

}
