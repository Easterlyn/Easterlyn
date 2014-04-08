package co.sblock.Sblock.Machines.Type;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.v1_7_R2.Container;
import net.minecraft.server.v1_7_R2.ContainerAnvil;
import net.minecraft.server.v1_7_R2.EntityHuman;
import org.bukkit.craftbukkit.v1_7_R2.CraftWorld;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;

import co.sblock.Sblock.Events.Packets.WrapperPlayServerOpenWindow;

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

	public void closeMachine(Player p) {
		openMachines.remove(p);
	}

	public void openAnvil(Player player, Machine m) {
		this.openMachines.put(player, m);
		// Opens a real anvil window for the Player in question
		WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow();
		packet.setInventoryType(InventoryType.ANVIL);
		packet.setWindowTitle("Punch Designix"); // Does not display. Minecraft limitation.

		net.minecraft.server.v1_7_R2.EntityPlayer p = ((org.bukkit.craftbukkit.v1_7_R2.entity.CraftPlayer) player).getHandle();

		// tick container counter - otherwise server will be confused by slot numbers
		packet.setWindowId((byte) p.nextContainerCounter());

		Container container = new AnvilContainer(p, m.l);

		p.activeContainer = container;
		p.activeContainer.windowId = packet.getWindowId();
		container.addSlotListener(p);

		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public class AnvilContainer extends ContainerAnvil {

		public AnvilContainer(net.minecraft.server.v1_7_R2.EntityHuman entity, Location l) {
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
