package co.sblock.fx;

import org.bukkit.event.Event;

import co.sblock.users.User;

public abstract class SblockFX {
	
	//The name of the effect as it will appear ingame
	private String name;
	//The event that can trigger this effect to occur
	private Class<? extends Event> eventTrigger;
	//How much to multiply the strength of an effect from its base value
	private Integer multiplier;
	
	//The system time in milliseconds when the effect was last triggered
	private long lastTriggered;
	//The time in milliseconds to wait before the effect can be triggered again
	private long cooldown;
	
	//The FXManager. Not sure if this is even necessary tbh
	private FXManager manager;
	
	/**
	 * Gets the name of the Effect
	 * 
	 * @return The name of the Effect as it will appear ingame
	 */
	public String getName() {
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
	 * The time when the Effect was last triggered
	 * 
	 * @return The time in millis
	 */
	public long getLastTriggered() {
		return lastTriggered;
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
	 * 
	 * @param u The user who owns this Effect
	 * @param e The event that triggered this Effect
	 */
	public void applyEffect(User u, Event e) {
		if(!(e.getClass().isAssignableFrom(eventTrigger))) {
			return;
		}
		if(System.currentTimeMillis() - lastTriggered > cooldown) {
			lastTriggered = System.currentTimeMillis();
			getEffect(u, e);
		}
	}
	
	/**
	 * The actual description and application of the effect.
	 * This should only be called from applyEffect() !
	 * 
	 * @param u The user who owns this Effect
	 * @param e The event that triggered this Effect
	 */
	protected abstract void getEffect(User u, Event e);

}
