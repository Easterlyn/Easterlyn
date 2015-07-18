package co.sblock.effects.effect.misc;

import org.bukkit.entity.Player;

import co.sblock.effects.effect.Effect;
import co.sblock.effects.effect.EffectBehaviorPassive;

/**
 * Effect for indicating that the user has alchemized a computer into an object.
 * 
 * @author Jikoo
 */
public class EffectComputer extends Effect implements EffectBehaviorPassive {

	public EffectComputer() {
		super(10, 1, 1, "Computer");
	}

	@Override
	public void applyEffect(Player player, int level) {}

}
