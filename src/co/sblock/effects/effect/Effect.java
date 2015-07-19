package co.sblock.effects.effect;

import java.util.Arrays;
import java.util.List;

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
	// Maximum level per individual item
	private final int maximumLevel;
	// Maximum level with any effect on play
	private final int maximumCombinedLevel;

	public Effect(int cost, int maximumLevel, int maximumCombinedLevel, String... names) {
		if (names.length == 0) {
			throw new IllegalArgumentException("Effects must have a minimum of 1 identifying name to register!");
		}
		this.names = Arrays.asList(names);
		this.cost = cost;
		this.maximumLevel = maximumLevel;
		this.maximumCombinedLevel = maximumCombinedLevel;
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
	 * Gets the maximum level of an Effect per gear part.
	 * 
	 * @return the maximum level
	 */
	public int getMaxLevel() {
		return this.maximumLevel;
	}

	/**
	 * Gets the maximum level of an Effect for all gear parts combined.
	 * 
	 * @return the maximum level
	 */
	public int getMaxTotalLevel() {
		return this.maximumCombinedLevel;
	}

}
