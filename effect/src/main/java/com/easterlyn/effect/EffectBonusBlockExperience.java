package com.easterlyn.effect;

import com.easterlyn.EasterlynEffects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockExpEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Gives bonus experience from blocks.
 *
 * @author Jikoo
 */
public class EffectBonusBlockExperience extends Effect {

	public EffectBonusBlockExperience(EasterlynEffects plugin) {
		super(plugin, "Inquisitor", EquipmentSlots.HEAD, 200, 5, 5);
	}

	@Override
	public void applyEffect(@NotNull LivingEntity entity, int level, Event event) {
		if (event instanceof BlockExpEvent) {
			BlockExpEvent blockExpEvent = (BlockExpEvent) event;

			blockExpEvent.setExpToDrop((int) (blockExpEvent.getExpToDrop() * (level * .25 + 1)));
		}
	}

}
