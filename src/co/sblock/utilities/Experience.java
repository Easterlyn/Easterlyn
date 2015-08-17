package co.sblock.utilities;

import org.bukkit.entity.Player;

/**
 * A utility for managing Player experience properly.
 * 
 * @author Jikoo
 */
public class Experience {

	/**
	 * Calculates a player's total exp based on level and progress to next.
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * @param player the Player
	 * 
	 * @return the amount of exp the Player has
	 */
	public static int getExp(Player player) {
		return getLevelExp(player.getLevel())
				+ Math.round(getExpToNext(player.getLevel()) * player.getExp());
	}

	/**
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * "One can determine how much experience has been collected to reach a level using the equations:
	 * 
	 *  Total Experience = [Level]2 + 6[Level] (at levels 0-15)
	 *                     2.5[Level]2 - 40.5[Level] + 360 (at levels 16-30)
	 *                     4.5[Level]2 - 162.5[Level] + 2220 (at level 31+)"
	 */
	private static int getLevelExp(int level) {
		if (level > 30) {
			return (int) (4.5 * level * level - 162.5 * level + 2220);
		}
		if (level > 15) {
			return (int) (2.5 * level * level - 40.5 * level + 360);
		}
		return level * level + 6 * level;
	}

	/**
	 * @see http://minecraft.gamepedia.com/Experience#Leveling_up
	 * 
	 * "The formulas for figuring out how many experience orbs you need to get to the next level are as follows:
	 *  Experience Required = 2[Current Level] + 7 (at levels 0-15)
	 *                        5[Current Level] - 38 (at levels 16-30)
	 *                        9[Current Level] - 158 (at level 31+)"
	 */
	private static int getExpToNext(int level) {
		if (level > 30) {
			return 9 * level - 158;
		}
		if (level > 15) {
			return 5 * level - 38;
		}
		return 2 * level + 7;
	}

	/**
	 * Change a Player's exp.
	 * <p>
	 * This method should be used in place of {@link Player#giveExp(int)}, which does not properly
	 * account for different levels requiring different amounts of experience.
	 * 
	 * @param player the Player affected
	 * @param exp the amount of experience to add or remove
	 */
	public static void changeExp(Player player, int exp) {
		exp += getExp(player);

		if (exp < 0) {
			exp = 0;
		}

		int level = 0;
		float expRemaining = 0;
		while (exp > 0) {
			int expTillNext = getExpToNext(level);
			if (exp >= expTillNext) {
				level++;
			} else {
				expRemaining = exp / (float) expTillNext;
			}
			exp -= expTillNext;
		}

		player.setLevel(level);
		player.setExp(expRemaining);
	}
}
