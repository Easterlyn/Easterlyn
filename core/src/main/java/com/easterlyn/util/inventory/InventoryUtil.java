package com.easterlyn.util.inventory;

import com.easterlyn.util.tuple.Triple;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.server.v1_15_R1.ChatMessage;
import net.minecraft.server.v1_15_R1.EntityPlayer;
import net.minecraft.server.v1_15_R1.MerchantRecipe;
import net.minecraft.server.v1_15_R1.MerchantRecipeList;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_15_R1.PacketPlayOutOpenWindowMerchant;
import net.minecraft.server.v1_15_R1.PacketPlayOutSetSlot;
import net.minecraft.server.v1_15_R1.PacketPlayOutWindowData;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_15_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

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

	public static void setAnvilExpCost(InventoryView view, int cost) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		try {
			Method method = view.getClass().getMethod("getHandle");
			Object nmsInventory = method.invoke(view);
			Field field = nmsInventory.getClass().getDeclaredField("a");
			field.set(nmsInventory, cost);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateAnvilExpCost(InventoryView view) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		EntityPlayer entityPlayer = ((CraftPlayer) view.getPlayer()).getHandle();
		if (entityPlayer.playerConnection == null) {
			return;
		}
		entityPlayer.playerConnection.sendPacket(new PacketPlayOutWindowData(entityPlayer.activeContainer.windowId, 0,
				((AnvilInventory) view.getTopInventory()).getRepairCost()));
	}

	@SafeVarargs
	public static void updateVillagerTrades(Player player, Triple<ItemStack, ItemStack, ItemStack>... recipes) {
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
		for (Triple<ItemStack, ItemStack, ItemStack> recipe : recipes) {
			// The client can handle having empty results for recipes, but will crash upon removing the result.
			if (recipe.getRight().getType() == Material.AIR) {
				continue;
			}
			list.add(new MerchantRecipe(CraftItemStack.asNMSCopy(recipe.getLeft()), CraftItemStack.asNMSCopy(recipe.getMiddle()),
					CraftItemStack.asNMSCopy(recipe.getRight()), 0, Integer.MAX_VALUE, 0, 0));
		}

		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindowMerchant(nmsPlayer.activeContainer.windowId, list, 5, 0, false, false));
		player.updateInventory();
	}

}
