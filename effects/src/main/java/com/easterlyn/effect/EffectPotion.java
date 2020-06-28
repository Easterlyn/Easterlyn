package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import com.easterlyn.util.StringUtil;
import java.util.function.Function;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EffectPotion extends Effect {

	private final PotionEffectType potionEffect;
	private final int duration;

	EffectPotion(@NotNull EasterlynEffects plugin, @NotNull PotionEffectType effect) {
		this(plugin, effect, 200, EquipmentSlots.ALL, 500, 2, 10);
	}

	EffectPotion(@NotNull EasterlynEffects plugin, @NotNull PotionEffectType effect, int duration,
			@NotNull Function<EquipmentSlot, Boolean> target, double cost, int maximumLevel, int maximumCombinedLevel) {
		super(plugin, StringUtil.getFriendlyName(effect.getName()), target, cost, maximumLevel, maximumCombinedLevel);
		this.potionEffect = effect;
		this.duration = duration;
	}

	@Override
	public void applyEffect(@NotNull LivingEntity entity, int level, @Nullable Event event) {
		if (event != null) {
			return;
		}

		if (level < 1) {
			level = 1;
		}
		int entityDuration = entity instanceof Player ? duration : Integer.MAX_VALUE;
		entity.addPotionEffect(new PotionEffect(potionEffect, entityDuration, level - 1));
	}

}
