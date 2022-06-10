package com.easterlyn.kitchensink.listener;

import com.github.jikoo.planarwrappers.util.Generics;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.jetbrains.annotations.NotNull;

public class WitherFacts implements Listener {

  private final ItemStack facts;

  public WitherFacts() {
    this.facts = new ItemStack(Material.WRITTEN_BOOK);
    Generics.consumeAs(
        BookMeta.class,
        facts.getItemMeta(),
        bookMeta -> {
          bookMeta.setTitle("Wither Facts");
          bookMeta.setAuthor(ChatColor.BLUE + "Pete");
          bookMeta
              .spigot()
              .addPage(new TextComponent[] {new TextComponent("Withers are awesome.")});
          facts.setItemMeta(bookMeta);
        });
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    Player player = event.getEntity();
    EntityDamageEvent lastDamage = player.getLastDamageCause();
    if (lastDamage != null
        && (lastDamage.getCause() == EntityDamageEvent.DamageCause.WITHER
            || (lastDamage instanceof EntityDamageByEntityEvent
                && ((EntityDamageByEntityEvent) lastDamage).getDamager().getType()
                    == EntityType.WITHER))) {
      event.getDrops().add(facts);
    }
  }
}
