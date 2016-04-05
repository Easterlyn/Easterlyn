package co.sblock.machines.type;

import java.util.Random;
import java.util.function.Function;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.utilities.InventoryUtils;

/**
 * Combine and create.
 * 
 * @author Jikoo
 */
public class CompoundingUnionizor extends Machine {

	private final ItemStack drop;

	public CompoundingUnionizor(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Compounding Unionizer");
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.DROPPER, Direction.NORTH, "chest");
		shape.setVectorData(new Vector(0, 0, 0), m);
		m = shape.new MaterialDataValue(Material.WORKBENCH);
		shape.setVectorData(new Vector(0, 1, 0), m);

		drop = null; // future
	}

	public boolean setRecipe(Recipe recipe) {
		if (!(recipe instanceof ShapelessRecipe || recipe instanceof ShapedRecipe)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean handleOpen(InventoryOpenEvent event, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				if (!(event.getPlayer() instanceof Player)) {
					return;
				}
				Player player = (Player) event.getPlayer();
				if (!player.isOnline()) {
					return;
				}
				String name;
				if (event.getInventory().getType() == InventoryType.WORKBENCH) {
					name = "Recipe Select - Craft to set!";
				} else {
					name = getName();
				}
				InventoryUtils.changeWindowName(player, name);
			}
		}.runTask(getPlugin());
		return false;
	}

	

	private void ejectAllInvalidItems(Function<ItemStack, Boolean> func, Inventory inventory, ConfigurationSection storage) {
		for (ItemStack item : inventory.getContents()) {
			if (item == null || func.apply(item)) {
				continue;
			}
			// This is safe, no CME because we're iterating over a copied array
			inventory.removeItem(item);
			this.ejectItem(this.getKey(storage), item, storage);
		}
	}

	private void ejectItem(Location key, ItemStack item, ConfigurationSection storage) {
		Direction facing = this.getDirection(storage).getRelativeDirection(Direction.SOUTH);

		BlockState blockState = key.clone()
				.add(Shape.getRelativeVector(facing, new Vector(0, 0, 1))).getBlock().getState();
		if (blockState instanceof InventoryHolder) {
			// MACHINES InventoryMoveItemEvent
			if (((InventoryHolder) blockState).getInventory().addItem(item).size() == 0) {
				return;
			}
		}

		// Center block location
		key.add(Shape.getRelativeVector(facing, new Vector(0.5D, 0.5D, 1.5D)));
		BlockFace face = facing.toBlockFace();

		// See net.minecraft.server.DispenseBehaviorItem
		Random random = getPlugin().getRandom();
		double motionRandom = random.nextDouble() * 0.1D + 0.2D;
		// 0.007499999832361937D * 6
		double motX = face.getModX() * motionRandom + random.nextGaussian() * 0.044999998994171622D;
		double motY = 0.2000000029802322D + random.nextGaussian() * 0.044999998994171622D;
		double motZ = face.getModZ() * motionRandom + random.nextGaussian() * 0.044999998994171622D;

		// MACHINES BlockDispenseEvent
		// TODO play click + smoke
		key.getWorld().dropItem(key, item).setVelocity(new Vector(motX, motY, motZ));
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
