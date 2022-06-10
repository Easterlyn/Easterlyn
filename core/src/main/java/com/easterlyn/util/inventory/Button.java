package com.easterlyn.util.inventory;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public record Button(ItemStack item, Consumer<InventoryClickEvent> consumer) {}
