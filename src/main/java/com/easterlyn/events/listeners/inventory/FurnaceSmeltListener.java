package com.easterlyn.events.listeners.inventory;

import com.easterlyn.Easterlyn;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.utilities.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Furnace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.inventory.BlastingRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
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
		BlastingRecipe furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_axe"),
				new ItemStack(Material.COAL), Material.DIAMOND_AXE, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_boots"),
				new ItemStack(Material.COAL), Material.DIAMOND_BOOTS, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_chestplate"),
				new ItemStack(Material.COAL), Material.DIAMOND_CHESTPLATE, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_helmet"),
				new ItemStack(Material.COAL), Material.DIAMOND_HELMET, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_hoe"),
				new ItemStack(Material.COAL), Material.DIAMOND_HOE, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_leggings"),
				new ItemStack(Material.COAL), Material.DIAMOND_LEGGINGS, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_pickaxe"),
				new ItemStack(Material.COAL), Material.DIAMOND_PICKAXE, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_shovel"),
				new ItemStack(Material.COAL), Material.DIAMOND_SHOVEL, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_diamond_sword"),
				new ItemStack(Material.COAL), Material.DIAMOND_SWORD, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
		furnace = new BlastingRecipe(new NamespacedKey(plugin, "smelt_down_shears"),
				new ItemStack(Material.COAL), Material.SHEARS, 0.0F, 200);
		plugin.getServer().addRecipe(furnace);
	}

	/**
	 * EventHandler for FurnaceSmeltEvents.
	 *
	 * @param event the FurnaceSmeltEvent
	 */
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onFurnaceSmelt(FurnaceSmeltEvent event) {

		ItemStack itemStack = event.getSource();
		if (!canSalvage(itemStack.getType())) {
			return;
		}

		final ItemStack result = getMainItem(itemStack.getType());
		double damage = 0;
		if (itemStack.hasItemMeta()) {
			ItemMeta meta = itemStack.getItemMeta();
			if (meta instanceof Damageable) {
				damage = ((Damageable) meta).getDamage();
			}
		}
		int amount = (int) (result.getAmount()
				* (itemStack.getType().getMaxDurability() - damage) / itemStack.getType().getMaxDurability());

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
