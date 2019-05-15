package com.easterlyn.machines;

import com.easterlyn.machines.type.Dublexor;
import com.easterlyn.machines.type.Machine;
import com.easterlyn.utilities.tuple.Pair;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.ChatComponentText;
import net.minecraft.server.v1_14_R1.Container;
import net.minecraft.server.v1_14_R1.ContainerMerchant;
import net.minecraft.server.v1_14_R1.Containers;
import net.minecraft.server.v1_14_R1.EntityHuman;
import net.minecraft.server.v1_14_R1.EntityPlayer;
import net.minecraft.server.v1_14_R1.EntityTypes;
import net.minecraft.server.v1_14_R1.EntityVillager;
import net.minecraft.server.v1_14_R1.EnumHand;
import net.minecraft.server.v1_14_R1.IChatBaseComponent;
import net.minecraft.server.v1_14_R1.MerchantRecipe;
import net.minecraft.server.v1_14_R1.MerchantRecipeList;
import net.minecraft.server.v1_14_R1.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_14_R1.SoundEffect;
import net.minecraft.server.v1_14_R1.World;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.craftbukkit.v1_14_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * brb going insane because of NMS
 *
 * @author Jikoo
 */
public class MachineInventoryTracker {

	private final Map<UUID, Pair<Machine, Location>> openMachines;
	private final Machines machines;

	MachineInventoryTracker(Machines machines) {
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
		if (pair.getLeft() instanceof Dublexor) {
			event.getInventory().setItem(1, null);
		}
	}

	public void openVillagerInventory(Player player, Machine m, Location key) {
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

		int containerCounter = nmsPlayer.nextContainerCounter();
		Container container = new MerchantContainer(containerCounter, nmsPlayer, key);
		nmsPlayer.activeContainer = container;
		container.addSlotListener(nmsPlayer);
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(containerCounter, Containers.MERCHANT,
				new ChatComponentText(m.getName())));

		this.openMachines.put(player.getUniqueId(), new Pair<>(m, key));
	}

	public class MerchantContainer extends ContainerMerchant {
		MerchantContainer(int containerCounter, EntityPlayer player, Location location) {
			super(containerCounter, player.inventory, new FakeNMSVillager(player, player.world, location));
			this.checkReachable = false;
		}
	}

	public class FakeNMSVillager extends EntityVillager {
		FakeNMSVillager(EntityPlayer player, World world, Location location) {
			super(EntityTypes.VILLAGER, world);
			setTradingPlayer(player);
			// Set location so that logging plugins know where the transaction is taking place
			a(new BlockPosition(location.getX(), location.getY(), location.getZ()), -1);
		}

		@Override
		public MerchantRecipeList getOffers() {
			return new MerchantRecipeList();
		}

		@Override
		public void a(MerchantRecipe paramMerchantRecipe) {
			// Adds a trade
		}

		@Nullable
		@Override
		protected SoundEffect getSoundAmbient() {
			return null;
		}

		@Override
		public void ej() {
			// Random sound
		}

		@Override
		public boolean a(EntityHuman entityhuman, EnumHand enumhand) {
			// Player interaction should not be possible
			return true;
		}

		@Override
		public IChatBaseComponent getScoreboardDisplayName() {
			return new ChatComponentText("Machine");
		}
	}

}
