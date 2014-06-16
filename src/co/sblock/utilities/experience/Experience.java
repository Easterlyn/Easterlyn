package co.sblock.utilities.experience;

import org.bukkit.entity.Player;


/**
 * A utility for managing Player experience properly.
 * 
 * @author Jikoo
 */
public class Experience {

	/**
	 * Calculates a player's total exp based on level and progress to next.
	 * @see http://minecraft.gamepedia.com/Experience#Formulas_and_Tables
	 * 
	 * @param player the Player
	 * 
	 * @return the amount of exp the Player has
	 */
	public static int getExp(Player player) {
		return (int) (getLevelExp(player.getLevel())
				+ Math.round(getExpToNext(player.getLevel()) * player.getExp()));
	}

	private static int getLevelExp(int level) {
		int exp = 0;
		for (int i = 0; i < level; i++) {
			exp += getExpToNext(i);
		}
		return exp;
	}

	private static int getExpToNext(int level) {
		if (level > 29) {
			return 62 + (level - 30) * 7;
		}
		if (level > 15) {
			return 17 + (level - 15) * 3;
		}
		return 17;
	}

	public static void changeExp(Player player, int exp) {
		exp += getExp(player);

		if (exp < 0) {
			exp = 0;
		}

		player.setLevel(0);
		player.setExp(0);

		while (exp > 0) {
			int expTillNext = getExpToNext(player.getLevel());
			if (exp >= expTillNext) {
				player.giveExp(expTillNext);
			} else {
				player.giveExp(exp);
			}
			exp -= expTillNext;
		}
	}
}
