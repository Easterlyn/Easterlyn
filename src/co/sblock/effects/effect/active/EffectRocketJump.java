package co.sblock.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

import co.sblock.Sblock;
import co.sblock.commands.entry.CrotchRocketCommand;
import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorCooldown;
import co.sblock.effects.effect.Effect;

/**
 * Per request, the ever-popular CrotchRocket.
 * 
 * @author Jikoo
 */
public class EffectRocketJump extends Effect implements BehaviorActive, BehaviorCooldown {

	public EffectRocketJump() {
		super(300, 1, 1, "Crotchrocket");
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
