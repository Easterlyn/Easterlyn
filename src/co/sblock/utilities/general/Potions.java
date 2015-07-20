package co.sblock.utilities.general;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/**
 * 
 * 
 * @author Jikoo
 */
public class Potions {

	public static void applyIfBetter(Player player, PotionEffect effect) {
		if (!player.hasPotionEffect(effect.getType())) {
			player.addPotionEffect(effect);
			return;
		}
		for (PotionEffect current : player.getActivePotionEffects()) {
			if (!current.getType().equals(effect.getType())) {
				continue;
			}
			if (current.getAmplifier() > effect.getAmplifier()) {
				return;
			}
			if (current.getDuration() > effect.getDuration() && current.getAmplifier() == effect.getAmplifier()) {
				return;
			}
			player.addPotionEffect(effect, true);
		}
	}
}
