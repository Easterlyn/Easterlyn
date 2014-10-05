package co.sblock.events.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

import co.sblock.Sblock;

public class SaddleEnchantmentListener implements Listener {

	@EventHandler
	public void onClick(InventoryClickEvent event) {
		final Inventory toTest = event.getInventory();

		if (toTest.getType() != InventoryType.ANVIL
				|| !((Player) event.getWhoClicked()).hasPermission("sblock.blaze")) {
			return;
		}
		if (true) {
			Bukkit.getScheduler().runTaskLater(Sblock.getInstance(), new Runnable() {

				@Override
				public void run() {
					doCombine((AnvilInventory) toTest);
				}

			}, 1l);
		}

	}

	private void doCombine(AnvilInventory toTest) {
		ItemStack firstSlot = toTest.getItem(0);
		ItemStack secondSlot = toTest.getItem(1);

		if (firstSlot == null || secondSlot == null) {
			return;
		}

		ItemStack maybeSaddle = null;
		if (firstSlot.getType() == Material.ENCHANTED_BOOK
				&& secondSlot.getType() == Material.SADDLE) {
			maybeSaddle = tryCombineBookSaddle(firstSlot, secondSlot);
		}

		if (firstSlot.getType() == Material.SADDLE
				&& secondSlot.getType() == Material.ENCHANTED_BOOK) {
			maybeSaddle = tryCombineBookSaddle(secondSlot, firstSlot);
		}

		if (maybeSaddle != null) {
			toTest.setItem(2, maybeSaddle);
		}
	}

	private ItemStack tryCombineBookSaddle(ItemStack book, ItemStack saddle) {
		int fireAspectLevel = 0;

		if (book.getItemMeta() instanceof EnchantmentStorageMeta) {
			EnchantmentStorageMeta esm = (EnchantmentStorageMeta) book.getItemMeta();

			fireAspectLevel = esm.getStoredEnchantLevel(Enchantment.ARROW_FIRE);
		}

		if (fireAspectLevel > 0) {
			ItemStack blazingSaddle = new ItemStack(Material.SADDLE, 1);
			blazingSaddle.addUnsafeEnchantment(Enchantment.ARROW_FIRE, 1);
			return blazingSaddle;
		}

		return null;
	}
}
