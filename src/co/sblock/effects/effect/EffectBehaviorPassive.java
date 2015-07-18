package co.sblock.effects.effect;

import co.sblock.users.OnlineUser;

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
	public abstract void applyEffect(OnlineUser user);

	/**
	 * Removes the Effect from the given OnlineUser.
	 * 
	 * @param user the OnlineUser
	 */
	public void removeEffect(OnlineUser user);

}
