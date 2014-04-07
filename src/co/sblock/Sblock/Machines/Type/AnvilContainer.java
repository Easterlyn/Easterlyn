package co.sblock.Sblock.Machines.Type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;

/**
 * 
 * 
 * @author Jikoo
 */
public class AnvilContainer extends net.minecraft.server.v1_7_R2.ContainerAnvil {

	public AnvilContainer(net.minecraft.server.v1_7_R2.EntityHuman entity, Location l) {
		super(entity.inventory, ((org.bukkit.craftbukkit.v1_7_R2.CraftWorld) l.getWorld()).getHandle(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), entity);
		try {
			Field f = net.minecraft.server.v1_7_R2.ContainerAnvil.class.getDeclaredField("h");
			f.setAccessible(true);
			f.set(this, new AnvilInventoryWrapper("Repair", true, 2, this));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean a(net.minecraft.server.v1_7_R2.EntityHuman entityhuman) {
		return true;
	}

	public class AnvilInventoryWrapper extends net.minecraft.server.v1_7_R2.InventorySubcontainer {

		final AnvilContainer a;
		public List<HumanEntity> transaction = new ArrayList<HumanEntity>();
		public InventoryHolder holder;
		@SuppressWarnings("unused")
		private int maxStack = 64;

		public AnvilInventoryWrapper(String s, boolean flag, int i, AnvilContainer a) {
			super(s, flag, i);
			this.a = a;
			setMaxStackSize(1);
		}
		public net.minecraft.server.v1_7_R2.ItemStack[] getContents() {
			return this.items;
		}

		public void onOpen(org.bukkit.craftbukkit.v1_7_R2.entity.CraftHumanEntity who) {
			this.transaction.add(who);
		}

		public void onClose(org.bukkit.craftbukkit.v1_7_R2.entity.CraftHumanEntity who) {
			this.transaction.remove(who);
		}

		public List<HumanEntity> getViewers() {
			return this.transaction;
		}

		public InventoryHolder getOwner() {
			return this.holder;
		}

		public void setMaxStackSize(int size) {
			this.maxStack = size;
		}

		public void update() {
			super.update();
			this.a.a(this);
		}
	}
}
