package co.sblock.events.listeners;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.utilities.inventory.InventoryUtils;

/**
 * Listener for FurnaceSmeltEvents.
 * 
 * @author Jikoo
 */
public class FurnaceSmeltListener implements Listener {

	/**
	 * EventHandler for FurnaceSmeltEvents.
	 * 
	 * @param event the FurnaceSmeltEvent
	 */
	@EventHandler(priority = org.bukkit.event.EventPriority.HIGH)
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {

		Machine m = Machines.getInstance().getMachineByBlock(event.getBlock());
		if (m != null) {
			event.setCancelled(m.handleFurnaceSmelt(event));
			return;
		}

		if (!canSalvage(event.getSource().getType())) {
			return;
		}

		ItemStack result = getMainItem(event.getSource().getType());

		int amount = (int) (result.getAmount()
				// This cast is actually necessary, prevents mid-calculation rounding.
				* ((double) (event.getSource().getType().getMaxDurability() - event.getSource()
						.getDurability())) / event.getSource().getType().getMaxDurability());

		if (amount < 1) {
			// Default coal recipe will fire
			return;
		}

		result.setAmount(amount);

		event.setCancelled(true);
		Furnace furnace = (Furnace) event.getBlock().getState();
		if (furnace.getInventory().getResult() == null) {
			furnace.getInventory().setResult(result);
		} else {
			furnace.getWorld().dropItemNaturally(furnace.getLocation(), result);
		}
		furnace.getInventory().setSmelting(InventoryUtils.decrement(furnace.getInventory().getSmelting(), 1));
		furnace.update(true);
	}

	private ItemStack getMainItem(Material material) {
		switch (material) {
		case DIAMOND_SPADE:
			return new ItemStack(Material.DIAMOND, 1);
		case DIAMOND_HOE:
		case DIAMOND_SWORD:
			return new ItemStack(Material.DIAMOND, 2);
		case DIAMOND_AXE:
		case DIAMOND_PICKAXE:
			return new ItemStack(Material.DIAMOND, 3);
		case DIAMOND_BOOTS:
			return new ItemStack(Material.DIAMOND, 4);
		case DIAMOND_HELMET:
			return new ItemStack(Material.DIAMOND, 5);
		case DIAMOND_LEGGINGS:
			return new ItemStack(Material.DIAMOND, 7);
		case DIAMOND_CHESTPLATE:
			return new ItemStack(Material.DIAMOND, 8);
		case GOLD_SPADE:
			return new ItemStack(Material.GOLD_INGOT, 1);
		case GOLD_HOE:
		case GOLD_SWORD:
			return new ItemStack(Material.GOLD_INGOT, 2);
		case GOLD_AXE:
		case GOLD_PICKAXE:
			return new ItemStack(Material.GOLD_INGOT, 3);
		case GOLD_BOOTS:
			return new ItemStack(Material.GOLD_INGOT, 4);
		case GOLD_HELMET:
			return new ItemStack(Material.GOLD_INGOT, 5);
		case GOLD_LEGGINGS:
			return new ItemStack(Material.GOLD_INGOT, 7);
		case GOLD_CHESTPLATE:
			return new ItemStack(Material.GOLD_INGOT, 8);
		case IRON_SPADE:
			return new ItemStack(Material.IRON_INGOT, 1);
		case IRON_HOE:
		case IRON_SWORD:
		case SHEARS:
			return new ItemStack(Material.IRON_INGOT, 2);
		case IRON_AXE:
		case IRON_PICKAXE:
			return new ItemStack(Material.IRON_INGOT, 3);
		case IRON_BOOTS:
			return new ItemStack(Material.IRON_INGOT, 4);
		case IRON_HELMET:
			return new ItemStack(Material.IRON_INGOT, 5);
		case IRON_LEGGINGS:
			return new ItemStack(Material.IRON_INGOT, 7);
		case IRON_CHESTPLATE:
			return new ItemStack(Material.IRON_INGOT, 8);
		default:
			return new ItemStack(Material.COAL);
		}
	}

	private boolean canSalvage(Material material) {
		switch (material) {
		case DIAMOND_AXE:
		case DIAMOND_HOE:
		case DIAMOND_PICKAXE:
		case DIAMOND_SPADE:
		case DIAMOND_SWORD:
		case DIAMOND_BOOTS:
		case DIAMOND_CHESTPLATE:
		case DIAMOND_HELMET:
		case DIAMOND_LEGGINGS:
		case GOLD_AXE:
		case GOLD_HOE:
		case GOLD_PICKAXE:
		case GOLD_SPADE:
		case GOLD_SWORD:
		case GOLD_BOOTS:
		case GOLD_CHESTPLATE:
		case GOLD_HELMET:
		case GOLD_LEGGINGS:
		case IRON_AXE:
		case IRON_HOE:
		case IRON_SPADE:
		case IRON_SWORD:
		case IRON_PICKAXE:
		case IRON_BOOTS:
		case IRON_CHESTPLATE:
		case IRON_HELMET:
		case IRON_LEGGINGS:
		case SHEARS:
			return true;
		default:
			return false;
		}
	}
}
