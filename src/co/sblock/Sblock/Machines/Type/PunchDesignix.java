package co.sblock.Sblock.Machines.Type;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.comphenix.protocol.ProtocolLibrary;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Events.Packets.WrapperPlayServerOpenWindow;
import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.Captchadex;

/**
 * Simulate a Sburb Punch Designix in Minecraft.
 * 
 * @author Dublek
 */
public class PunchDesignix extends Machine implements InventoryHolder {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	public PunchDesignix(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(d.getRelativeDirection(Direction.WEST).getUpperStairByte());
		shape.addBlock(new Vector(1, 0, 0), is);
		is = new ItemStack(Material.QUARTZ_STAIRS);
		is.setDurability(d.getRelativeDirection(Direction.NORTH).getStairByte());
		shape.addBlock(new Vector(0, 1, 0), is);
		shape.addBlock(new Vector(1, 1, 0), is);
		is = new ItemStack(Material.STEP);
		is.setDurability((short) 15);
		shape.addBlock(new Vector(0, 0, -1), is);
		shape.addBlock(new Vector(1, 0, -1), is);
		is = new ItemStack(Material.CARPET);
		is.setDurability((short) 8);
		shape.addBlock(new Vector(0, 1, -1), is);
		shape.addBlock(new Vector(1, 1, -1), is);
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
	public boolean handleClick(InventoryClickEvent event) {
		SblockMachines.getMachines().getLogger().debug("Designinx handleClick");
		if (event.getCurrentItem() == null) {
			event.setResult(Result.DENY);
			return true;
		}
		final AnvilInventory ai = (AnvilInventory) event.getInventory();
		if (event.getSlot() == 2 && event.getClickedInventory().equals(event.getView().getTopInventory())) {
			Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
				public void run() {
					ItemStack is = ai.getItem(0);
					if (is.getAmount() == 1) {
						ai.setItem(0, null);
					} else {
						is.setAmount(is.getAmount() - 1);
						ai.setItem(0, is);
					}
				}
			});
			return false;
		} else {
			if (Captcha.isCaptchaCard(ai.getItem(0)) && ai.getItem(1) == null) {
				final ItemStack is = Captchadex.punchCard(ai.getItem(0));
				is.setAmount(1);

				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					public void run() {
						ai.setItem(2, is);
					}
				});
				return false;
			} else if (Captcha.isPunchCard(ai.getItem(0)) && Captcha.isPunchCard(ai.getItem(1))) {
				ItemStack input = Captchadex.punchCard(ai.getItem(1));
				final ItemStack output = Captchadex.punchCard(ai.getItem(0));
				output.setAmount(1);
				List<String> inputLore = new ArrayList<String>();
				List<String> outputLore = new ArrayList<String>();
				if (output.hasItemMeta() && output.getItemMeta().hasLore()) {
					outputLore = output.getItemMeta().getLore();
				}
				if (input.hasItemMeta() && input.getItemMeta().hasLore()) {
					inputLore = input.getItemMeta().getLore();
				}
				for (String s : inputLore) {
					if (s.startsWith(">")) {
						outputLore.add(s);
					}
				}
				ItemMeta im = output.getItemMeta();
				im.setLore(outputLore);
				output.setItemMeta(im);

				Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
					public void run() {
						ai.setItem(2, output);
					}
				});
				return false;
			}
		}
		event.setResult(Result.DENY);
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#postAssemble()
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void postAssemble() {
		this.l.getBlock().setType(Material.QUARTZ_STAIRS);
		this.l.getBlock().setData(d.getRelativeDirection(Direction.EAST).getUpperStairByte());
	}

	/**
	 * @see org.bukkit.inventory.InventoryHolder#getInventory()
	 */
	@Override
	public Inventory getInventory() {
		return null;
	}
	
	public void openInventory(Player player) {
		// Bear in mind - we will need a packet listener to handle all events related to this.
		// the window we are opening is fake and not recognized by the server.
		WrapperPlayServerOpenWindow packet = new WrapperPlayServerOpenWindow();
		packet.setInventoryType(InventoryType.ANVIL);
		packet.setWindowTitle("Punch Designix");
		try {
			ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet.getHandle());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}
	}
}
