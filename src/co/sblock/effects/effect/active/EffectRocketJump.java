package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import co.sblock.Sblock;
import co.sblock.commands.entry.CrotchRocketCommand;
import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorActive;
import co.sblock.effects.effect.EffectBehaviorCooldown;

/**
 * Per request, the ever-popular CrotchRocket.
 * 
 * @author Jikoo
 */
public class EffectRocketJump extends Effect implements EffectBehaviorActive, EffectBehaviorCooldown {

	public EffectRocketJump() {
		super(300, 1, 1, "Crotchrocket", "Rocket Jump", "Rocket Rider");
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
		return Arrays.asList(PlayerInteractEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		((CrotchRocketCommand) Sblock.getInstance().getCommandMap().getCommand("crotchrocket")).launch(entity);
	}

}
