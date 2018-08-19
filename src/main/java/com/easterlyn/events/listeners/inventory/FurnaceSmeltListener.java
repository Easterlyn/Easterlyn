package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.utilities.InventoryUtils;

import org.bukkit.Material;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Listener for FurnaceSmeltEvents.
 *
 * @author Jikoo
 */
public class FurnaceSmeltListener extends EasterlynListener {

	public FurnaceSmeltListener(Easterlyn plugin) {
		super(plugin);

		// Smelting: Revert armor to crafting material, 1 coal if durability% too low
		// Deprecated constructor required to ignore item durability
		FurnaceRecipe furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_BOOTS, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_CHESTPLATE, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_HELMET, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_HOE, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_LEGGINGS, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_PICKAXE, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_SHOVEL, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.DIAMOND_SWORD, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
		furnace = new FurnaceRecipe(new ItemStack(Material.COAL), Material.DIAMOND_AXE, Short.MAX_VALUE);
		furnace.setInput(Material.SHEARS, Short.MAX_VALUE);
		plugin.getServer().addRecipe(furnace);
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

		final ItemStack result = getMainItem(event.getSource().getType());
		int amount = (int) (result.getAmount()
				// This cast is actually necessary, prevents mid-calculation rounding.
				* ((double) (event.getSource().getType().getMaxDurability() - event.getSource()
						.getDurability())) / event.getSource().getType().getMaxDurability());

		if (amount < 1) {
			// Allow Minecraft to handle recipe normally
			return;
		}

		result.setAmount(amount);

		event.setCancelled(true);

		new BukkitRunnable() {
			@Override
			public void run() {
				Furnace furnace = (Furnace) event.getBlock().getState();
				ItemStack furnaceResult = furnace.getInventory().getResult();
				if (furnaceResult != null && furnaceResult.getType() != Material.AIR && !furnaceResult.isSimilar(result)) {
					return;
				}

				if (furnaceResult != null && furnaceResult.getType() != Material.AIR) {
					result.setAmount(furnaceResult.getAmount() + result.getAmount());
					if (result.getAmount() > result.getMaxStackSize()) {
						result.setAmount(result.getMaxStackSize());
					}
				}

				furnace.getInventory().setResult(result);
				furnace.getInventory().setSmelting(InventoryUtils.decrement(furnace.getInventory().getSmelting(), 1));
				furnace.update(true);
			}
		}.runTask(this.getPlugin());
	}

	private ItemStack getMainItem(Material material) {
		switch (material) {
			case DIAMOND_SHOVEL:
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
			case DIAMOND_SHOVEL:
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
