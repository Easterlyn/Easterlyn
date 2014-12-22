package co.sblock.machines;

import io.netty.buffer.Unpooled;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_8_R1.ChatComponentText;
import net.minecraft.server.v1_8_R1.Container;
import net.minecraft.server.v1_8_R1.ContainerMerchant;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityPlayer;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.IMerchant;
import net.minecraft.server.v1_8_R1.ItemStack;
import net.minecraft.server.v1_8_R1.MerchantRecipe;
import net.minecraft.server.v1_8_R1.MerchantRecipeList;
import net.minecraft.server.v1_8_R1.PacketDataSerializer;
import net.minecraft.server.v1_8_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_8_R1.PacketPlayOutOpenWindow;

import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import co.sblock.machines.type.Machine;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.regex.RegexUtils;

/**
 * brb going insane because of NBT
 * 
 * @author Jikoo
 */
public class MachineInventoryTracker {

	private static MachineInventoryTracker instance;

	private Map<Player, Machine> openMachines;

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
		Machine m = openMachines.remove((Player) event.getPlayer());
		if (m == null) {
			return;
		}

		// Do not drop exp bottle placed in second slot
		if (m.getType() == MachineType.ALCHEMITER) {
			event.getInventory().setItem(1, null);
		}
	}

	@SuppressWarnings("unchecked")
	public void openVillagerInventory(Player player, Machine m, org.bukkit.inventory.ItemStack... items) {
		EntityPlayer p = ((CraftPlayer) player).getHandle();

		int containerCounter = p.nextContainerCounter();
		Container container = new MerchantContainer(p);
		p.activeContainer = container;
		container.windowId = containerCounter;
		container.addSlotListener(p);
		p.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, "minecraft:villager",
				new ChatComponentText(RegexUtils.getFriendlyName(m.getType().name())), 3));

		this.openMachines.put(player, m);

		if (!(container instanceof MerchantContainer)) {
			return;
		}
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
		p.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|TrList", out));

	}

	public class MerchantContainer extends ContainerMerchant {
		public MerchantContainer(EntityPlayer player) {
			super(player.inventory, new FakeMerchant(player), player.world);
			this.checkReachable = false;
		}
	}

	public class FakeMerchant implements IMerchant {
		private EntityHuman customer;

		public FakeMerchant(EntityHuman customer) {
			this.customer = customer;
		}

		@Override
		public void a_(EntityHuman paramEntityHuman) {
			this.customer = paramEntityHuman;
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

		@Override
		public EntityHuman u_() {
			return customer;
		}
	}

	public static MachineInventoryTracker getTracker() {
		if (instance == null) {
			instance = new MachineInventoryTracker();
		}
		return instance;
	}
}
