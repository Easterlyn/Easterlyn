package com.easterlyn.util.inventory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftMerchantRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;
import org.jetbrains.annotations.NotNull;

public class InventoryUtil {

  public static void updateWindowSlot(Player player, int slot) {
    if (!(player instanceof CraftPlayer)) {
      return;
    }
    ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
    nmsPlayer.connection.send(
        new ClientboundContainerSetSlotPacket(
            nmsPlayer.containerMenu.containerId,
            nmsPlayer.containerMenu.incrementStateId(),
            slot,
            nmsPlayer.containerMenu.getSlot(slot).getItem()));
  }

  public static void changeWindowName(Player player, String name) {
    CraftPlayer craftPlayer = (CraftPlayer) player;
    ServerPlayer entityPlayer = craftPlayer.getHandle();

    if (entityPlayer.connection == null) {
      return;
    }

    entityPlayer.connection.send(
        new ClientboundOpenScreenPacket(
            entityPlayer.containerMenu.containerId,
            entityPlayer.containerMenu.getType(),
            Component.literal(name)));
    entityPlayer.initMenu(entityPlayer.containerMenu);
  }

  public static void updateVillagerTrades(Player player, MerchantRecipe... recipes) {
    if (recipes == null || recipes.length == 0) {
      return;
    }
    updateVillagerTrades(player, Arrays.asList(recipes));
  }

  public static void updateVillagerTrades(
      @NotNull Player player, @NotNull Collection<MerchantRecipe> recipes) {
    if (recipes.size() == 0) {
      // Setting result in a villager inventory with recipes doesn't play nice clientside.
      // To make life easier, if there are no recipes, don't send the trade recipe packet.
      return;
    }

    ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

    if (nmsPlayer.containerMenu.getBukkitView().getType() != InventoryType.MERCHANT) {
      return;
    }

    MerchantOffers list = new MerchantOffers();
    for (org.bukkit.inventory.MerchantRecipe recipe : recipes) {
      // The client can handle having empty results for recipes but will crash when clicking result.
      if (recipe.getResult().getType() == Material.AIR) {
        continue;
      }
      List<ItemStack> ingredients = recipe.getIngredients();
      if (ingredients.size() < 1) {
        continue;
      }
      list.add(CraftMerchantRecipe.fromBukkit(recipe).toMinecraft());
    }

    nmsPlayer.connection.send(
        new ClientboundMerchantOffersPacket(
            nmsPlayer.containerMenu.containerId, list, 5, 0, false, false));
    player.updateInventory();
  }
}
