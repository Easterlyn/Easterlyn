package co.sblock.Sblock.Machines.Type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Captcha.Captcha;

/**
 * Simulate a Sburb Punch Designix in Minecraft.
 * 
 * @author Dublek
 */
public class PunchDesignix extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public PunchDesignix(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.EAST).getUpperStairByte());
		shape.addBlock(new Vector(0, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.WEST).getUpperStairByte());
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.NORTH).getStairByte());
		shape.addBlock(new Vector(0, 1, 0), m);
		shape.addBlock(new Vector(1, 1, 0), m);
		m = new MaterialData(Material.STEP, (byte) 15);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(1, 0, -1), m);
		m = new MaterialData(Material.CARPET, (byte) 8);
		shape.addBlock(new Vector(0, 1, -1), m);
		shape.addBlock(new Vector(1, 1, -1), m);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.PUNCH_DESIGNIX;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		openInventory(event.getPlayer());
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	@SuppressWarnings("deprecation")
	public boolean handleClick(InventoryClickEvent event) {
		final MerchantInventory merchant = (MerchantInventory) event.getInventory();
		if (event.getSlot() == 2 && event.getRawSlot() == event.getView().convertSlot(event.getRawSlot())) {
			if (event.getCurrentItem() != null && event.getClick() == ClickType.LEFT) {
				if (merchant.getItem(0) != null && merchant.getItem(0).getAmount() > 1) {
					merchant.getItem(0).setAmount(merchant.getItem(0).getAmount() - 1);
				} else {
					merchant.setItem(0, null);
				}
				event.setCursor(event.getCurrentItem());
				((Player) event.getWhoClicked()).updateInventory();
			}
			return true;
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			public void run() {
				ItemStack output;
				if (merchant.getItem(0) == null || !Captcha.isCard(merchant.getItem(0))) {
					output = null;
				} else {
					output = Captcha.createPunchCard(merchant.getItem(0), merchant.getItem(1));
				}
				merchant.setItem(2, output);
				for (HumanEntity viewer : merchant.getViewers()) {
					((Player) viewer).updateInventory();
				}
			}
		});
		return false;
	}

	public void openInventory(Player player) {
		MachineInventoryTracker.getTracker().openMachineInventory(player, this, InventoryType.MERCHANT);
	}
}
