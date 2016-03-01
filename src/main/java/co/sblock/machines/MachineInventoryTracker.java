package co.sblock.machines;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import co.sblock.machines.type.Alchemiter;
import co.sblock.machines.type.Machine;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;

import net.minecraft.server.v1_9_R1.ChatComponentText;
import net.minecraft.server.v1_9_R1.Container;
import net.minecraft.server.v1_9_R1.ContainerMerchant;
import net.minecraft.server.v1_9_R1.EntityHuman;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.EntityVillager;
import net.minecraft.server.v1_9_R1.IChatBaseComponent;
import net.minecraft.server.v1_9_R1.ItemStack;
import net.minecraft.server.v1_9_R1.MerchantRecipe;
import net.minecraft.server.v1_9_R1.MerchantRecipeList;
import net.minecraft.server.v1_9_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_9_R1.World;

/**
 * brb going insane because of NMS
 * 
 * @author Jikoo
 */
public class MachineInventoryTracker {

	private final Map<UUID, Pair<Machine, Location>> openMachines;
	private final Machines machines;

	public MachineInventoryTracker(Machines machines) {
		this.openMachines = new HashMap<>();
		this.machines = machines;
	}

	public boolean hasMachineOpen(Player p) {
		return openMachines.containsKey(p.getUniqueId());
	}

	public Pair<Machine, ConfigurationSection> getOpenMachine(Player p) {
		if (!openMachines.containsKey(p.getUniqueId())) {
			return null;
		}
		return machines.getMachineByLocation(openMachines.get(p.getUniqueId()).getRight());
	}

	public void closeMachine(InventoryCloseEvent event) {
		Pair<Machine, Location> pair = openMachines.remove(event.getPlayer().getUniqueId());
		if (pair == null) {
			return;
		}

		// Do not drop exp bottle placed in second slot
		if (pair.getLeft() instanceof Alchemiter) {
			event.getInventory().setItem(1, null);
		}
	}

	public void openVillagerInventory(Player player, Machine m, Location key) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

		int containerCounter = nmsPlayer.nextContainerCounter();
		Container container = new MerchantContainer(nmsPlayer);
		nmsPlayer.activeContainer = container;
		container.windowId = containerCounter;
		container.addSlotListener(nmsPlayer);
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, "minecraft:villager",
				new ChatComponentText(m.getClass().getSimpleName()), 3));

		this.openMachines.put(player.getUniqueId(), new ImmutablePair<Machine, Location>(m, key));
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
			setTradingPlayer(player);
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
}
