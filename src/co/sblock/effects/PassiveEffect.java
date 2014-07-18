package co.sblock.effects;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public enum PassiveEffect {
	
	COMPUTER("Computer", 0),
	PSHOOOES("PSHOOOES", 800),
	JUMP("Boing", 500),
	SPEED("Speed", 500),
	FLOWERS("Flowers", 1000);
	
	private String loreText;

	private int cost;

	private PassiveEffect(String s, int cost) {
		loreText = s;
		this.cost = cost;
	}
	
	public String getLoreText() {
		return this.loreText;
	}

	public int getCost() {
		return this.cost;
	}
	
	/**
	 * Gets if a String is a valid PassiveEffect
	 * 
	 * @param s the String to test
	 * 
	 * @return true if the String is valid
	 */
	public static boolean isValidEffect(String s) {
		for (PassiveEffect p : PassiveEffect.values()) {
			if (p.getLoreText().equalsIgnoreCase(s)) {
				return true;
			}
		}
		try {
			PassiveEffect.valueOf(s);
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the specified effect
	 * 
	 * @param s the String to return the PassiveEffect for
	 * 
	 * @return the PassiveEffect. Null if Effect does not exist
	 */
	public static PassiveEffect getEffect(String s) {
		PassiveEffect pE = null;
		for (PassiveEffect p : PassiveEffect.values()) {
			if (p.getLoreText().equalsIgnoreCase(s)) {
				return p;
			}
		}
		try {
			pE = PassiveEffect.valueOf(s);
		} catch (IllegalArgumentException e) {}
		return pE;
	}
	
	/**
	 * Applies a PassiveEffect to a Player
	 * 
	 * @param p the Player to apply the PassiveEffect to
	 * @param pE the PassiveEffect to be applied
	 * @param strength the modifier on the PassiveEffect
	 */
	public static void applyEffect(Player p, PassiveEffect pE, Integer strength) {
		PotionEffect potEffect;
		switch (pE) {

		case JUMP:
			potEffect = new PotionEffect(PotionEffectType.JUMP, Integer.MAX_VALUE, strength);
			p.addPotionEffect(potEffect, true);
			break;
		case SPEED:
			potEffect = new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, strength);
			p.addPotionEffect(potEffect, true);
			break;
		default:
			break;
		}
	}
	
	/**
	 * Removes a PassiveEffect from a Player
	 * 
	 * @param p the Player to remove the PassiveEffect from
	 * @param pE the PassiveEffect to be removed
	 */
	public static void removeEffect(Player p, PassiveEffect pE) {
		switch (pE) {

		case JUMP:
			p.removePotionEffect(PotionEffectType.JUMP);
			break;
		case SPEED:
			p.removePotionEffect(PotionEffectType.SPEED);
			break;
		default:
			break;
		}
	}
	
/*
Jump boost
Sanic Lightspeed
Armor Particles (Particle Armor?)
Snow Trail (Snow Golem-style) (Freezes water underneath you)
Item Vacuum (Long-distance Pickup)
SGA (enchantment text) Particle Aura
Mob Disguise (also able to emit relevant sfx)
Dirt Accumulator (occasionally adds blocks of dirt to inventory)
Love Aura (Emit heart particles; animals inclined to breed when you are near)
Footprints
Steady Hand (Decreases mining speed, silk touch for all blocks)
Batman (attract bats to you)
Stay Away from the AIDS Bakesale (automatically eats nearby cake when low on hunger)
Something with beacon beams.
Fill buckets with milk without needing a cow (Aurthor is your spirit animal)
Pogo (jumps automatically on contact with the ground)
Clonevision (other players appear to be wearing your head)
Sodavison (other players appear to be wearing Sodapop's head)
LEADERSHIP SKILLS: All chat messages are allcaps
Grimdark: All chat messages are scrambled and black. give off nether portal particles.
Wither
Saladturtle's Blessing: Guaranteed chicken spawn from thrown eggs
Johnny Appleseed: 25% chance to get apples from oak leaves. apples restore a little more hunger.
Fred flintstone: always get flint from gravel. Flint and Steel is not damaged when used.
Ninja: Nametag can't be seen through walls, even when not crouching.
HeartStone: either short crappy regen every effect tick or just add 1/2 heart (1 hp)
 */
}
