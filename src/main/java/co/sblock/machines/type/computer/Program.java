package co.sblock.machines.type.computer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.machines.Machines;

/**
 * Interface defining basic Program behavior.
 * 
 * @author Jikoo
 */
public abstract class Program {

	private final Machines machines;

	public Program(Machines machines) {
		this.machines = machines;
	}

	public void execute(Player player, ItemStack clicked) {
		execute(player, clicked, false);
	}

	protected abstract void execute(Player player, ItemStack clicked, boolean verified);

	public boolean isDefault() {
		return false;
	}

	public abstract ItemStack getIcon();

	public abstract ItemStack getInstaller();

	public Machines getMachines() {
		return this.machines;
	}

	public String getName() {
		return getClass().getSimpleName();
	}

}
