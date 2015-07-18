package co.sblock.effects.effect;

import org.bukkit.entity.Player;

/**
 * Interface declaring methods required for a passive effect.
 * 
 * @author Jikoo
 */
public interface EffectBehaviorPassive {

	/**
	 * Applies the Effect to the given OnlineUser.
	 * 
	 * @param user the OnlineUser
	 */
	public abstract void applyEffect(Player player, int level);

}
