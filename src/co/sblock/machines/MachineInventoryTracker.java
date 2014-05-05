package co.sblock.machines;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_7_R3.Container;
import net.minecraft.server.v1_7_R3.ContainerAnvil;
import net.minecraft.server.v1_7_R3.ContainerMerchant;
import net.minecraft.server.v1_7_R3.EntityHuman;
import net.minecraft.server.v1_7_R3.EntityPlayer;
import net.minecraft.server.v1_7_R3.IMerchant;
import net.minecraft.server.v1_7_R3.ItemStack;
import net.minecraft.server.v1_7_R3.MerchantRecipe;
import net.minecraft.server.v1_7_R3.MerchantRecipeList;
import net.minecraft.server.v1_7_R3.PacketDataSerializer;
import net.minecraft.util.io.netty.buffer.Unpooled;

import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_7_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_7_R3.inventory.CraftItemStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;

import co.sblock.events.packets.WrapperPlayServerCustomPayload;
import co.sblock.events.packets.WrapperPlayServerOpenWindow;
import co.sblock.machines.type.Machine;
import co.sblock.machines.type.MachineType;
import co.sblock.utilities.regex.RegexUtils;

import com.comphenix.protocol.ProtocolLibrary;

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

	public void openMachineInventory(Player player, Machine m, InventoryType it, org.bukkit.inventory.ItemStack... items) {
		// Opens a real anvil window for the Player in question
		WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow();
		packet.setInventoryType(it);
		packet.setWindowTitle(RegexUtils.getFriendlyName(m.getType().name()));
		packet.setTitleExact(true);

		EntityPlayer p = ((CraftPlayer) player).getHandle();

		// tick container counter - otherwise server will be confused by slot numbers
		packet.setWindowId((byte) p.nextContainerCounter());

		Container container;
		switch (it) {
		case ANVIL:
			container = new AnvilContainer(p, m.getKey());
			break;
		case MERCHANT:
			container = new MerchantContainer(p);
			break;
		default:
			return;
		}

		p.activeContainer = container;
		p.activeContainer.windowId = packet.getWindowId();
		container.addSlotListener(p);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			return;
		}

		this.openMachines.put(player, m);

		if (!(container instanceof MerchantContainer)) {
			return;
		}
		MerchantRecipeList list = new MerchantRecipeList();
		for (int i = 0; i < items.length; i++) {
			if (i % 3 == 0 && items.length - i > 2) {
				list.a(new MerchantRecipe(CraftItemStack.asNMSCopy(items[i]),
						CraftItemStack.asNMSCopy(items[i+1]),
						CraftItemStack.asNMSCopy(items[i+2])));
			}
		}

		if (list.isEmpty()) {
			// Setting result in a villager inventory with recipes doesn't play nice clientside.
			// To make life easier, if there are no recipes, don't send the trade recipe packet.
			return;
		}

		try {

			PacketDataSerializer out = new PacketDataSerializer(Unpooled.buffer());
			out.writeInt(packet.getWindowId());
			list.a(out);

			WrapperPlayServerCustomPayload trades = new WrapperPlayServerCustomPayload();
			trades.setChannel("MC|TrList");
			trades.setData(out.array());
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, trades.getHandle());

			

		} catch (IllegalArgumentException | SecurityException | InvocationTargetException e) {
			e.printStackTrace();
		}
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
		public EntityHuman b() {
			return customer;
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
	}

	public class AnvilContainer extends ContainerAnvil {

		public AnvilContainer(EntityHuman entity, Location l) {
			super(entity.inventory, ((CraftWorld) l.getWorld()).getHandle(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), entity);
		}

		@Override
		public boolean a(EntityHuman entityhuman) {
			return true;
		}
	}

	public static MachineInventoryTracker getTracker() {
		if (instance == null) {
			instance = new MachineInventoryTracker();
		}
		return instance;
	}
}
