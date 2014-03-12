package co.sblock.Sblock.Machines.Type;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.CruxiteDowel;

/**
 * 
 * @author Dublek, Jikoo
 */
public class TotemLathe extends Machine implements InventoryHolder	{

	/** The Furnace Block */
	private Block furnaceBlock;

	/** The ID of the furnace updating task */
	private int task;

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

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	public boolean handleClick(InventoryClickEvent event) {
		SblockMachines.getMachines().getLogger().debug("TotemLathe handleClick");
		if (event.getCurrentItem() == null) {
			event.setResult(Result.DENY);
			return true;
		}
		updateFurnaceInventory();
		return false;
	}

	/**
	 * Trigger a synchronous repeating update of this Machine's Inventory.
	 * <p>
	 * This simulates the furnace smelting process for objects without a recipe.
	 */
	private void updateFurnaceInventory() {
		task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				Furnace f = (Furnace) furnaceBlock.getState();
				if (f == null) {
					// TileEntity is not loaded.
					return;
				}
				FurnaceInventory fi = f.getInventory();

				boolean fuelIsDowel = CruxiteDowel.isBlankDowel(fi.getFuel());
				if ((fuelIsDowel || CruxiteDowel.isBlankDowel(fi.getSmelting()))
						&& (Captcha.isPunchCard(fi.getSmelting()) || Captcha.isPunchCard(fi.getFuel()))) {
					if (f.getCookTime() > 190) {
						fi.setResult(CruxiteDowel.carve(fuelIsDowel ? fi.getSmelting() : fi.getFuel()));
						if (fuelIsDowel) {
							fi.setFuel(null);
						} else {
							fi.setSmelting(null);
						}
						f.setBurnTime((short) 0);
						f.setCookTime((short) 0);
						f.update(true);
						Bukkit.getScheduler().cancelTask(task);
						return;
					}
					if (f.getBurnTime() > 200 - f.getCookTime()) {
						f.setBurnTime((short) 200);
					}
					// 200 ticks standard cook time
					f.setCookTime((short) (f.getCookTime() + 5));
				} else {
					f.setBurnTime((short) 0);
					f.setCookTime((short) 0);
					Bukkit.getScheduler().cancelTask(task);
				}
				f.update(true);
			}
		}, 0, 5);
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
