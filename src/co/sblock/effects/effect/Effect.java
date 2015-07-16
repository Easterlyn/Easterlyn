package co.sblock.effects.effect;

import java.util.Arrays;
import java.util.List;

import co.sblock.users.OnlineUser;

/**
 * 
 * 
 * @author Jikoo
 */
public abstract class Effect {

	// Lore which will trigger the same Effect
	private final List<String> names;
	// Cost of the Effect during alchemy
	private final int cost;

	public Effect(int cost, String... names) {
		this.cost = cost;
		this.names = Arrays.asList(names);
	}

	/**
	 * Gets the List of names which this Effect goes by.
	 * 
	 * @return the names
	 */
	public List<String> getNames() {
		return this.names;
	}

	/**
	 * Gets the cost of the Effect for use in alchemy.
	 * 
	 * @return the cost
	 */
	public int getCost() {
		return this.cost;
	}

	/**
	 * Applies the Effect to the given OnlineUser.
	 * 
	 * @param user the OnlineUser
	 */
	public abstract void applyEffect(OnlineUser user);

}
