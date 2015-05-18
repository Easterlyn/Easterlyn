package co.sblock.machines;

import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.regex.RegexUtils;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.Container;
import net.minecraft.server.v1_8_R3.ContainerMerchant;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MerchantRecipe;
import net.minecraft.server.v1_8_R3.MerchantRecipeList;
import net.minecraft.server.v1_8_R3.PacketDataSerializer;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_8_R3.World;

/**
 * brb going insane because of NMS
 * 
 * @author Jikoo
 */
public class MachineInventoryTracker {

	private static MachineInventoryTracker instance;

	private final Map<Player, Machine> openMachines;

	public MachineInventoryTracker() {
		openMachines = new HashMap<>();
	}

	public boolean hasMachineOpen(Player p) {
		return openMachines.containsKey(p);
	}

	public Machine getOpenMachine(Player p) {
		return openMachines.get(p);
	}

	public void closeMachine(InventoryCloseEvent event) {
		Machine m = openMachines.remove(event.getPlayer());
		if (m == null) {
			return;
		}

		// Do not drop exp bottle placed in second slot
		if (m.getType() == MachineType.ALCHEMITER) {
			event.getInventory().setItem(1, null);
		}
	}

	public void openVillagerInventory(Player player, Machine m, org.bukkit.inventory.ItemStack... items) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

		int containerCounter = nmsPlayer.nextContainerCounter();
		Container container = new MerchantContainer(nmsPlayer);
		nmsPlayer.activeContainer = container;
		container.windowId = containerCounter;
		container.addSlotListener(nmsPlayer);
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, "minecraft:villager",
				new ChatComponentText(RegexUtils.getFriendlyName(m.getType().name())), 3));

		this.openMachines.put(player, m);

		MerchantRecipeList list = new MerchantRecipeList();
		for (int i = 0; i < items.length; i++) {
			if (i % 3 == 0 && items.length - i > 2) {
				list.add(new MerchantRecipe(CraftItemStack.asNMSCopy(items[i]),
						CraftItemStack.asNMSCopy(items[i+1]),
						CraftItemStack.asNMSCopy(items[i+2])));
			}
		}

		if (list.isEmpty()) {
			// Setting result in a villager inventory with recipes doesn't play nice clientside.
			// To make life easier, if there are no recipes, don't send the trade recipe packet.
			return;
		}

		PacketDataSerializer out = new PacketDataSerializer(Unpooled.buffer());
		out.writeInt(containerCounter);
		list.a(out);
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|TrList", out));

	}

	public class MerchantContainer extends ContainerMerchant {
		public MerchantContainer(EntityPlayer player) {
			super(player.inventory, new FakeNMSVillager(player, player.world), player.world);
			this.checkReachable = false;
		}
	}

	public class FakeNMSVillager extends EntityVillager {
		public FakeNMSVillager(EntityPlayer player, World world) {
			super(world);
			a_(player);
		}

		@Override
		public MerchantRecipeList getOffers(EntityHuman paramEntityHuman) {
			return new MerchantRecipeList();
		}

		@Override
		public void a(MerchantRecipe paramMerchantRecipe) {
			// adds a trade
		}

		@Override
		public void a_(ItemStack paramItemStack) {
			// reduces remaining trades and makes yes/no noises
		}

		@Override
		public IChatBaseComponent getScoreboardDisplayName() {
			return new ChatComponentText("Machine");
		}
	}

	public static MachineInventoryTracker getTracker() {
		if (instance == null) {
			instance = new MachineInventoryTracker();
		}
		return instance;
	}
}
