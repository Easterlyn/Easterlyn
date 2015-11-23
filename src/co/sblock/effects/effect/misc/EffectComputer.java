package co.sblock.effects.effect.misc;

import org.bukkit.entity.LivingEntity;

import co.sblock.Sblock;
import co.sblock.effects.effect.BehaviorPassive;
import co.sblock.effects.effect.Effect;

/**
 * Effect for indicating that the user has alchemized a computer into an object.
 * 
 * @author Jikoo
 */
public class EffectComputer extends Effect implements BehaviorPassive {

	public EffectComputer(Sblock plugin) {
		super(plugin, 10, 1, 1, "Computer");
	}

	@Override
	public void applyEffect(LivingEntity entity, int level) {}

}
