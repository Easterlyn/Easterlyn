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
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.CruxiteDowel;

/**
 * 
 * @author Dublek, Jikoo
 */
public class TotemLathe extends Machine implements InventoryHolder	{

	private Block furnaceBlock;

	private int task;

	public TotemLathe(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.QUARTZ_BLOCK);
		is.setDurability((short) 2);
		shape.addBlock(new Vector(0, 1, 0), is);
		shape.addBlock(new Vector(0, 2, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(d.getRelativeDirection(Direction.WEST).getUpperStairByte());
		shape.addBlock(new Vector(-1, 0, 0), is);
		is = new ItemStack(Material.STEP);
		is.setDurability((short) 7);
		shape.addBlock(new Vector(0, 3, 0), is);
		shape.addBlock(new Vector(-1, 3, 0), is);
		shape.addBlock(new Vector(-2, 3, 0), is);
		shape.addBlock(new Vector(-3, 3, 0), is);
		is = new ItemStack(Material.STEP);
		is.setDurability((short) 15);
		shape.addBlock(new Vector(-2, 0, 0), is);
		shape.addBlock(new Vector(-3, 0, 0), is);
		is = new ItemStack(Material.FURNACE);
		is.setDurability(d.getRelativeDirection(Direction.WEST).getChestByte());
		shape.addBlock(new Vector(-1, 1, 0), is);
		is = new ItemStack(Material.DAYLIGHT_DETECTOR);
		shape.addBlock(new Vector(-2, 1, 0), is);
		is = new ItemStack(Material.ANVIL);
		is.setDurability((short) (d.getDirByte() % 2 == 0 ? 3 : 2));
		shape.addBlock(new Vector(-3, 1, 0), is);
		is = new ItemStack(Material.HOPPER);
		shape.addBlock(new Vector(-3, 2, 0), is);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	@Override
	public MachineType getType() {
		return MachineType.TOTEM_LATHE;
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		event.getPlayer().openInventory(getInventory());
		return true;
	}
	
	public boolean handleClick(InventoryClickEvent event)	{
		if (event.getCurrentItem() == null) {
			event.setResult(Result.DENY);
			return true;
		}
		updateFurnaceInventory();
		return false;
	}

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

	@SuppressWarnings("deprecation")
	@Override
	protected void postAssemble() {
		this.l.getBlock().setType(Material.QUARTZ_BLOCK);
		this.l.getBlock().setData((byte) 2, false);		
	}

	@Override
	public Inventory getInventory() {
		if (furnaceBlock != null) {
			return ((Furnace) furnaceBlock.getState()).getInventory();
		}
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			if (e.getValue().getType() == Material.FURNACE) {
				furnaceBlock = e.getKey().getBlock();
				return ((Furnace) furnaceBlock.getState()).getInventory();
			}
		}
		Inventory i = Bukkit.createInventory(this, InventoryType.FURNACE);
		return i;
	}

}
