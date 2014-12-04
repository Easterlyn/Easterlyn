package co.sblock.fx;

import java.util.ArrayList;

import org.bukkit.event.Event;

import co.sblock.users.OnlineUser;

public abstract class SblockFX {

	//The canonical name of the effect
	protected String canonicalName;
	//Other names that may be used ingame
	protected ArrayList<String> commonNames;
	//The event that can trigger this effect to occur
	protected Class<? extends Event>[] eventTrigger;
	//How much to multiply the strength of an effect from its base value
	protected Integer multiplier;
	//How much this effect costs
	protected Integer cost;
	//Is this effect "passive" (continuous background, like a potion effect)
	protected boolean isPassive;

	//The system time in milliseconds when the effect can be used next
	protected long nextUsage;
	//The time in milliseconds to wait before the effect can be triggered again
	protected long cooldown;

	@SuppressWarnings("unchecked")
	public SblockFX(String name, boolean isPassive, Integer cost, long cooldown, Class<? extends Event>... eventTrigger) {
		this.canonicalName = name;
		this.eventTrigger = eventTrigger;
		this.multiplier = 1;
		this.cost = cost;
		this.isPassive = isPassive;
		this.nextUsage = 0;
		this.cooldown = cooldown;
	}

	/**
	 * Gets the canonical name of the Effect
	 * 
	 * @return The canonical name of the Effect
	 */
	public String getCanonicalName() {
		return canonicalName;
	}

	/**
	 * Adds a new name to the ArrayList of allowed names
	 * 
	 * @param newName The name to add
	 */
	public void addCommonName(String newName) {
		commonNames.add(newName);
	}

	/**
	 * Gets all common names of the Effect
	 * 
	 * @return The ArrayList of all common names
	 */
	public ArrayList<String> getCommonNames() {
		return commonNames;
	}

	/**
	 * Returns if the tested name should be allowed to apply/trigger the Effect
	 * 
	 * @param testName The name to compare against known names
	 * @return True if name is valid for this Effect
	 */
	public boolean isValidName(String testName) {
		if(testName.equalsIgnoreCase(canonicalName)) return true;
		for(String name : commonNames) {
			if(testName.equalsIgnoreCase(name)) {
				return true;
			}
		}
		return false;
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
	 * Gets the cost of the effect for alchemy purposes
	 * 
	 * @return The cost of the effect
	 */
	public Integer getCost() {
		return cost;
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
	public void applyEffect(OnlineUser u, Class<? extends Event> e) {
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
			getEffect(u, e);
		}
	}

	/**
	 * The actual description and application of the effect.
	 * This should only be called from applyEffect() !
	 * 
	 * @param u The user who owns this Effect
	 */
	protected abstract void getEffect(OnlineUser u, Class<? extends Event> e);
	
	/**
	 * For "passive" effects, will remove the effect from the user
	 * 
	 * @param u The user who owns this effect
	 */
	public abstract void removeEffect(OnlineUser u);

	public String toString() {
		String output = "\nFXName: " + canonicalName + " Trigger: " + eventTrigger[0].getName() + 
				"\nMultiplier: " + multiplier + " Cost: " + cost + " Passive?: " + isPassive + 
				"\nNext Usage: " + nextUsage + " Cooldown: " + cooldown;
		return output;
	}
}
