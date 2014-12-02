package co.sblock.fx;

import org.bukkit.event.Event;

import co.sblock.users.User;

public abstract class SblockFX {
	
	//The name of the effect as it will appear ingame
	protected static String name;
	//The event that can trigger this effect to occur
	protected Class<? extends Event>[] eventTrigger;
	//How much to multiply the strength of an effect from its base value
	protected Integer multiplier;
	//Is this effect "passive" (continuous background, like a potion effect)
	protected boolean isPassive;
	
	//The system time in milliseconds when the effect can be used next
	protected long nextUsage;
	//The time in milliseconds to wait before the effect can be triggered again
	protected long cooldown;
	
	@SuppressWarnings("unchecked")
	public SblockFX(boolean isPassive, long cooldown, Class<? extends Event>... eventTrigger) {
		this.eventTrigger = eventTrigger;
		this.multiplier = 1;
		this.isPassive = isPassive;
		this.nextUsage = 0;
		this.cooldown = cooldown;
	}
	
	/**
	 * Gets the name of the Effect
	 * 
	 * @return The name of the Effect as it will appear ingame
	 */
	public static String getEffectName() {
		return name;
	}
	/**
	 * Sets the strength multiplier for the Effect
	 * 
	 * @param mult The new multiplier
	 */
	public void setMultiplier(Integer mult) {
		multiplier = mult;
	}
	/**
	 * Gets the value of the multiplier
	 * 
	 * @return The multiplier
	 */
	public Integer getMultiplier() {
		return multiplier;
	}
	/**
	 * Gets whether the effect is "passive"
	 * 
	 * @return True if the effect is "passive"
	 */
	public boolean isPassive() {
		return isPassive;
	}
	/**
	 * The time when the Effect was last triggered
	 * 
	 * @return The time in millis
	 */
	public long getNextUsage() {
		return nextUsage;
	}
	/**
	 * The time that must be waited before the Effect can be triggered again
	 * 
	 * @return The cooldown time in millis
	 */
	public long getCooldown() {
		return cooldown;
	}
	
	/**
	 * Checks to see if this Effect can be triggered from the event that called it.
	 * If so, makes sure the cooldown has expired.
	 * Note: e is null if applied by FXManager
	 * 
	 * @param u The user who owns this Effect
	 * @param e The event that triggered this Effect
	 */
	public void applyEffect(User u, Class<? extends Event> e) {
		boolean execute = false;
		if(e != null) {
			for(Class<? extends Event> event : eventTrigger) {
				if(e.getClass().isAssignableFrom(event)) {
					execute = true;
					break;
				}	
			}
		}
		execute = (e == null);
		if(execute && System.currentTimeMillis() > nextUsage) {
			nextUsage = System.currentTimeMillis() + cooldown;
			getEffect(u);
		}
	}
	
	/**
	 * The actual description and application of the effect.
	 * This should only be called from applyEffect() !
	 * 
	 * @param u The user who owns this Effect
	 */
	protected abstract void getEffect(User u);
	
	/**
	 * For "passive" effects, will remove the effect from the user
	 * 
	 * @param u The user who owns this effect
	 */
	public abstract void removeEffect(User u);

}
