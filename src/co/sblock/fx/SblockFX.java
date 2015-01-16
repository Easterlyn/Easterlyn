package co.sblock.fx;

import java.util.HashSet;

import org.bukkit.event.Event;

import co.sblock.Sblock;
import co.sblock.users.OnlineUser;

public abstract class SblockFX {

	// The canonical name of the effect
	private String canonicalName;
	// Other names that may be used ingame
	private HashSet<String> commonNames;
	// The event that can trigger this effect to occur
	private Class<? extends Event>[] eventTrigger;
	// How much to multiply the strength of an effect from its base value
	private Integer multiplier;
	// How much this effect costs
	private Integer cost;
	// Is this effect "passive" (continuous background, like a potion effect)
	private boolean isPassive;
	// The system time in milliseconds when the effect can be used next
	private long nextUsage;
	// The time in milliseconds to wait before the effect can be triggered again
	private long cooldown;

	@SuppressWarnings("unchecked")
	public SblockFX(String name, boolean isPassive, Integer cost, long cooldown, Class<? extends Event>... eventTrigger) {
		this.canonicalName = name;
		this.eventTrigger = eventTrigger;
		this.multiplier = 1;
		this.cost = cost;
		this.isPassive = isPassive;
		this.nextUsage = 0;
		this.cooldown = cooldown;
		this.commonNames = new HashSet<>();
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
	public HashSet<String> getCommonNames() {
		return commonNames;
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
	 * Checks to see if this Effect can be triggered from the event that called it. If so, makes
	 * sure the cooldown has expired. Note: e is null if a passive effect is re-applied by FXManager.
	 * 
	 * @param user The user who owns this Effect
	 * @param event The event that triggered this Effect
	 */
	public final void applyEffect(OnlineUser user, Event event) {
		boolean execute = false;
		if (event != null) {
			for (Class<? extends Event> eventClazz : eventTrigger) {
				// Because of Bukkit's class loading, our stored class does not match the class of events fired often.
				if (eventClazz.isAssignableFrom(event.getClass())
						|| eventClazz.getName().equals(event.getClass().getName())) {
					execute = true;
					break;
				}
			}
		}
		execute = execute || event == null;
		if (execute && System.currentTimeMillis() > nextUsage) {
			Sblock.getLog().info("Time: " + System.currentTimeMillis() + ", " + nextUsage + ", " + cooldown);
			nextUsage = System.currentTimeMillis() + cooldown;
			getEffect(user, event);
		}
	}

	/**
	 * The actual description and application of the effect. This should only be called from
	 * applyEffect()!
	 * 
	 * @param u The user who owns this Effect
	 */
	protected abstract void getEffect(OnlineUser user, Event event);

	/**
	 * For "passive" effects, will remove the effect from the user
	 * 
	 * @param u The user who owns this effect
	 */
	public abstract void removeEffect(OnlineUser user);

	public String toString() {
		String output = "\nFXName: " + canonicalName + " Trigger: " + eventTrigger[0].getName() + 
				"\nMultiplier: " + multiplier + " Cost: " + cost + " Passive?: " + isPassive + 
				"\nNext Usage: " + nextUsage + " Cooldown: " + cooldown;
		return output;
	}
}
