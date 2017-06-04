package com.easterlyn.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.fun.CrotchRocketCommand;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.BehaviorCooldown;
import com.easterlyn.effects.effect.Effect;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * Per request, the ever-popular CrotchRocket.
 * 
 * @author Jikoo
 */
public class EffectRocketJump extends Effect implements BehaviorActive, BehaviorCooldown {

	public EffectRocketJump(Easterlyn plugin) {
		super(plugin, 300, 1, 1, "Crotchrocket");
	}

	@Override
	public String getCooldownName() {
		return "RocketJump";
	}

	@Override
	public long getCooldownDuration() {
		return 1000;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Collections.singletonList(PlayerInteractEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		CrotchRocketCommand.launch(this.getPlugin(), entity);
	}

}
