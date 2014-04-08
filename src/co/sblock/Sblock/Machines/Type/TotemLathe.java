package co.sblock.Sblock.Machines.Type;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.CruxiteDowel;

/**
 * 
 * @author Dublek, Jikoo
 */
public class TotemLathe extends Machine implements InventoryHolder	{

	/** The Furnace Block */
	private Block furnaceBlock;

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public TotemLathe(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 2);
		shape.addBlock(new Vector(0, 0, 0), m);
		shape.addBlock(new Vector(0, 1, 0), m);
		shape.addBlock(new Vector(0, 2, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.WEST).getUpperStairByte());
		shape.addBlock(new Vector(-1, 0, 0), m);
		m = new MaterialData(Material.STEP, (byte) 7);
		shape.addBlock(new Vector(0, 3, 0), m);
		shape.addBlock(new Vector(-1, 3, 0), m);
		shape.addBlock(new Vector(-2, 3, 0), m);
		shape.addBlock(new Vector(-3, 3, 0), m);
		m = new MaterialData(Material.STEP, (byte) 15);
		shape.addBlock(new Vector(-2, 0, 0), m);
		shape.addBlock(new Vector(-3, 0, 0), m);
		m = new MaterialData(Material.FURNACE,
				d.getRelativeDirection(Direction.EAST).getChestByte());
		shape.addBlock(new Vector(-1, 1, 0), m);
		m = new MaterialData(Material.DAYLIGHT_DETECTOR);
		shape.addBlock(new Vector(-2, 1, 0), m);
		m = new MaterialData(Material.ANVIL, (byte) (d.getDirByte() % 2 == 0 ? 3 : 2));
		shape.addBlock(new Vector(-3, 1, 0), m);
		m = new MaterialData(Material.HOPPER);
		shape.addBlock(new Vector(-3, 2, 0), m);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.TOTEM_LATHE;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		event.getPlayer().openInventory(getInventory());
		return true;
	}

	@Override
	public boolean handleFurnaceSmelt(FurnaceSmeltEvent event) {
		Furnace furnace = (Furnace) event.getBlock().getState();
		FurnaceInventory fi = furnace.getInventory();
		if (!CruxiteDowel.isBlankDowel(fi.getSmelting()) || !Captcha.isPunch(fi.getFuel())) {
			// Objects are invalid.
			return true;
		}
		fi.setResult(CruxiteDowel.carve(fi.getFuel()));
		ItemStack decrease = fi.getSmelting();
		if (decrease.getAmount() > 1) {
			decrease.setAmount(decrease.getAmount() - 1);
		} else {
			decrease = null;
		}
		fi.setSmelting(decrease);
		decrease = fi.getFuel();
		if (decrease.getAmount() > 1) {
			decrease.setAmount(decrease.getAmount() - 1);
		} else {
			decrease = null;
		}
		fi.setFuel(decrease);
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	public boolean handleClick(InventoryClickEvent event) {
		updateFurnaceInventory();
		return false;
	}

	/**
	 * Trigger a check that will cause the dowel to start smelting.
	 */
	private void updateFurnaceInventory() {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				Furnace f = (Furnace) furnaceBlock.getState();
				if (f == null) {
					// TileEntity is not loaded.
					return;
				}

				if (CruxiteDowel.isBlankDowel(f.getInventory().getSmelting())
						&& Captcha.isPunch(f.getInventory().getFuel())) {
					short cookRemaining = (short) (200 - f.getCookTime());
					if (f.getBurnTime() < cookRemaining) {
						f.setBurnTime(cookRemaining);
					}
				} else {
					f.setBurnTime((short) 0);
				}
				f.update(true);
			}
		});
	}

	/**
	 * @see org.bukkit.inventory.InventoryHolder#getInventory()
	 */
	@Override
	public Inventory getInventory() {
		if (furnaceBlock != null) {
			return ((Furnace) furnaceBlock.getState()).getInventory();
		}
		for (Entry<Location, MaterialData> e : blocks.entrySet()) {
			if (e.getValue().getItemType() == Material.FURNACE) {
				furnaceBlock = e.getKey().getBlock();
				return ((Furnace) furnaceBlock.getState()).getInventory();
			}
		}
		Inventory i = Bukkit.createInventory(this, InventoryType.FURNACE);
		return i;
	}

}
