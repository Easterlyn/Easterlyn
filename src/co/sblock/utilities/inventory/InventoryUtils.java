package co.sblock.utilities.inventory;

import java.util.HashSet;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.captcha.CruxiteDowel;

/**
 * A set of useful methods for inventory functions.
 * 
 * @author Jikoo
 */
public class InventoryUtils {

	private static HashSet<ItemStack> uniques;

	public static HashSet<ItemStack> getUniqueItems() {
		if (uniques == null) {
			uniques = new HashSet<>();
			for (MachineType mt : MachineType.values()) {
				uniques.add(mt.getUniqueDrop());
			}
		}
		return uniques;
	}

	public static boolean isUniqueItem(ItemStack toCheck) {
		if (Captcha.isCaptcha(toCheck) || CruxiteDowel.isDowel(toCheck)) {
			return true;
		}

		for (ItemStack is : getUniqueItems()) {
			if (is.isSimilar(toCheck)) {
				return true;
			}
		}

		return false;
	}

	public static int getAddFailures(Map<Integer, ItemStack> failures) {
		int count = 0;
		for (ItemStack is : failures.values()) {
			count += is.getAmount();
		}
		return count;
	}

	/**
	 * Reduces an ItemStack by the given quantity. If the ItemStack would have a quantity of 0,
	 * returns null.
	 * 
	 * @param is the ItemStack to reduce
	 * @param amount the amount to reduce the ItemStack by
	 * 
	 * @return the reduced ItemStack
	 */
	public static ItemStack decrement(ItemStack is, int amount) {
		if (is == null) {
			return null;
		}
		if (is.getAmount() > amount) {
			is.setAmount(is.getAmount() - amount);
		} else {
			is = null;
		}
		return is;
	}
}
