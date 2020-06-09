package com.easterlyn.util.inventory;

import java.util.List;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MerchantRecipeList;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindowMerchant;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftMerchantRecipe;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class InventoryUtil {

	public static void updateWindowSlot(Player player, int slot) {
		if (!(player instanceof CraftPlayer)) {
			return;
		}
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		nmsPlayer.playerConnection.sendPacket(
				new PacketPlayOutSetSlot(nmsPlayer.activeContainer.windowId, slot,
						nmsPlayer.activeContainer.getSlot(slot).getItem()));
	}

	public static void changeWindowName(Player player, String name) {
		CraftPlayer craftPlayer = (CraftPlayer) player;
		EntityPlayer entityPlayer = craftPlayer.getHandle();

		if (entityPlayer.playerConnection == null) {
			return;
		}

		entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId,
				entityPlayer.activeContainer.getType(), new ChatMessage(name)));
		entityPlayer.updateInventory(entityPlayer.activeContainer);

	}

	public static void updateVillagerTrades(Player player, MerchantRecipe... recipes) {
		if (recipes == null || recipes.length == 0) {
			// Setting result in a villager inventory with recipes doesn't play nice clientside.
			// To make life easier, if there are no recipes, don't send the trade recipe packet.
			return;
		}

		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

		if (nmsPlayer.activeContainer.getBukkitView().getType() != InventoryType.MERCHANT) {
			return;
		}

		MerchantRecipeList list = new MerchantRecipeList();
		for (org.bukkit.inventory.MerchantRecipe recipe : recipes) {
			// The client can handle having empty results for recipes, but will crash upon removing the result.
			if (recipe.getResult().getType() == Material.AIR) {
				continue;
			}
			List<ItemStack> ingredients = recipe.getIngredients();
			if (ingredients.size() < 1) {
				continue;
			}
			list.add(CraftMerchantRecipe.fromBukkit(recipe).toMinecraft());
		}

		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindowMerchant(nmsPlayer.activeContainer.windowId, list, 5, 0, false, false));
		player.updateInventory();
	}

}
