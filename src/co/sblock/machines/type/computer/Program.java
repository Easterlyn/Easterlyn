package co.sblock.machines.type.computer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Interface defining basic Program behavior.
 * 
 * @author Jikoo
 */
public abstract class Program {

	public void openInventory(Player player, ItemStack clicked) {
		openInventory(player, clicked, false);
	}

	protected abstract void openInventory(Player player, ItemStack clicked, boolean verified);

	public abstract ItemStack getIcon();

	public abstract ItemStack getInstaller();

	public String getName() {
		return getClass().getSimpleName();
	}

}
