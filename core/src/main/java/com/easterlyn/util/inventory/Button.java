package com.easterlyn.util.inventory;

import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class Button {

  private final ItemStack item;
  private final Consumer<InventoryClickEvent> consumer;

  public Button(ItemStack item, Consumer<InventoryClickEvent> consumer) {
    this.item = item;
    this.consumer = consumer;
  }

  public ItemStack getItem() {
    return item;
  }

  public Consumer<InventoryClickEvent> getConsumer() {
    return consumer;
  }
}
