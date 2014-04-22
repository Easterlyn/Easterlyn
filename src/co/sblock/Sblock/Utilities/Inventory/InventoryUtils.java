package co.sblock.Sblock.Utilities.Inventory;

import java.util.HashSet;
import java.util.Map;

import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.Type.MachineType;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.CruxiteDowel;

/**
 * 
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

	public static boolean isUniqueItem(ItemStack is) {
		ItemStack toCheck = is.clone();
		toCheck.setAmount(1);
		return Captcha.isCaptcha(toCheck) || CruxiteDowel.isDowel(toCheck)
				|| getUniqueItems().contains(toCheck);
	}

	public static int getAddFailures(Map<Integer, ItemStack> failures) {
		int count = 0;
		for (ItemStack is : failures.values()) {
			count += is.getAmount();
		}
		return count;
	}

	/**
	 * Reduces an ItemStack by the given quantity. If the ItemStack would have a
	 * quantity of 0, returns null.
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

	public static boolean equalsIgnoreAmount(ItemStack is1, ItemStack is2) {
		ItemStack temp = is2.clone();
		temp.setAmount(is1.getAmount());
		return is1.equals(temp);
	}
}
