package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.utilities.InventoryUtils;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for FurnaceSmeltEvents.
 * 
 * @author Jikoo
 */
public class FurnaceSmeltListener extends EasterlynListener {

	public FurnaceSmeltListener(Easterlyn plugin) {
		super(plugin);
	}

	/**
	 * EventHandler for FurnaceSmeltEvents.
	 * 
	 * @param event the FurnaceSmeltEvent
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {

		if (!canSalvage(event.getSource().getType())) {
			return;
		}

		ItemStack result = getMainItem(event.getSource().getType());

		int amount = (int) (result.getAmount()
				// This cast is actually necessary, prevents mid-calculation rounding.
				* ((double) (event.getSource().getType().getMaxDurability() - event.getSource()
						.getDurability())) / event.getSource().getType().getMaxDurability());

		if (amount < 1) {
			amount = 1;
			result = new ItemStack(Material.COAL, 1);
		}

		result.setAmount(amount);

		event.setCancelled(true);
		Furnace furnace = (Furnace) event.getBlock().getState();
		ItemStack furnaceResult = furnace.getInventory().getResult();
		if (furnaceResult == null || furnaceResult.isSimilar(result)) {
			if (furnaceResult != null) {
				amount = furnaceResult.getAmount() + result.getAmount();
				if (amount > result.getMaxStackSize()) {
					amount -= result.getMaxStackSize();
					result.setAmount(result.getMaxStackSize());
					furnace.getWorld().dropItem(furnace.getLocation(), new ItemStack(result.getType(), amount));
				} else {
					result.setAmount(amount);
				}
			}
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
		case SHEARS:
			return new ItemStack(Material.IRON_INGOT, 2);
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
		case SHEARS:
			return true;
		default:
			return false;
		}
	}

}
