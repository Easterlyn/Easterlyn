package co.sblock.Sblock.Machines.Type;

import java.util.Map.Entry;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Machines.Type.Shape.Direction;

/**
 * @author Jikoo
 */
public class Transportalizer extends Machine {

	/**
	 * @param l
	 * @param data
	 */
	public Transportalizer(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.QUARTZ_BLOCK);
		shape.addBlock(new Vector(-1, 0, 0), is);
		shape.addBlock(new Vector(1, 0, 0), is);
		shape.addBlock(new Vector(-1, 0, 1), is);
		shape.addBlock(new Vector(0, 0, 1), is);
		shape.addBlock(new Vector(1, 0, 1), is);
		shape.addBlock(new Vector(-1, 2, 1), is);
		shape.addBlock(new Vector(0, 2, 1), is);
		shape.addBlock(new Vector(1, 2, 1), is);
		is = new ItemStack(Material.QUARTZ_BLOCK);
		is.setDurability((short) 2);
		shape.addBlock(new Vector(0, 1, 1), is);
		is = new ItemStack(Material.WOOD_BUTTON);
		is.setDurability(d.getButtonByte());
		shape.addBlock(new Vector(-1, 2, 0), is);
		shape.addBlock(new Vector(1, 2, 0), is);
		is = new ItemStack(Material.STEP);
		is.setDurability((short) 7);
		shape.addBlock(new Vector(-1, 0, -1), is);
		shape.addBlock(new Vector(0, 0, -1), is);
		shape.addBlock(new Vector(1, 0, -1), is);
		is = new ItemStack(Material.NETHER_FENCE);
		shape.addBlock(new Vector(-1, 1, 1), is);
		shape.addBlock(new Vector(1, 1, 1), is);
		is = new ItemStack(Material.CARPET);
		is.setDurability((short) 14);
		shape.addBlock(new Vector(-1, 1, 0), is);
		is = new ItemStack(Material.CARPET);
		is.setDurability((short) 5);
		shape.addBlock(new Vector(1, 1, 0), is);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(org.bukkit.event.block.BlockBreakEvent)
	 */
	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return getData().equals(event.getPlayer().getName());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble(org.bukkit.event.block.BlockPlaceEvent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void assemble(BlockPlaceEvent event) {
		for (Location l : blocks.keySet()) {
			if (!l.getBlock().isEmpty()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "There isn't enough space to build this Machine here.");
				this.assemblyFailed();
				return;
			}
		}
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Block b = e.getKey().getBlock();
			b.setType(e.getValue().getType());
			b.setData(e.getValue().getData().getData());
		}
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.TRANSPORTALIZER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(org.bukkit.event.player.PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (!event.getClickedBlock().getType().equals(Material.WOOD_BUTTON)) {
			return false;
		} else {
			// ESTABLISH REMOTE LOCATION
			Block signBlock = this.l.clone().add(new Vector(0, 2, 0)).getBlock();
			if (!signBlock.getType().equals(Material.WALL_SIGN)) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "Please place a sign on your transportalizer between the buttons to use it."
						+ "\nThe third row should contain your desired coordinates in x,y,x format."
						+ "\nAll the other rows can contain whatever you like.");
				return false;
			}
			Sign sign = (Sign) signBlock.getState();
			// Sign lines are 0-3
			String checkLoc = sign.getLine(2);
			if (!checkLoc.replaceAll("\\-?[0-9]+,[0-9]+,\\-?[0-9]+",
					"Too long a string!").equals("Too long a string!")) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "The third line of your transportalizer sign must contain "
						+ "your desired destination in x,y,z format. Ex: 0,64,0");
				return false;
			}
			String[] locString = sign.getLine(2).split(",");
			int y = Integer.parseInt(locString[1]);
			y = y > 0 ? y < 256 ? y : 255 : 1;
			Location remote = new Location(event.getClickedBlock().getWorld(),
					Double.parseDouble(locString[0]) + .5, y, Double.parseDouble(locString[2]) + .5);

			// CHECK FUEL
			Chest chest = (Chest) l.getBlock().getState();
			Inventory chestInv = chest.getBlockInventory();
			if (!chestInv.contains(Material.REDSTONE)) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "The transportalizer makes a sputtering noise, but nothing happens."
						+ "\nIt occurs to you that perhaps you should check the fuel level."
						+ "\nYou give yourself a pat on the back for being a troubleshooting genius.");
				return false;
			}
			// adam sound based on chest fill? maybe just raw usable ItemStacks rather than quantity for speed
			l.getWorld().playSound(l, Sound.NOTE_PIANO, 5f, 5f);

			// Adam fix under-fueled still working
			consumeFuel(chest, remote);

			// TELEPORT
			// Messy, but avoids deprecation for now.
			Block pad = event.getClickedBlock().getRelative(BlockFace.DOWN);
			if (pad.getState().getData().toItemStack().getDurability() == (short) 5) {
				for (Entity e : l.getWorld().getEntities()) {
					if (e.getLocation().getBlock().equals(pad)) {
						remote.setPitch(e.getLocation().getPitch());
						remote.setYaw(e.getLocation().getYaw());
						e.teleport(remote);
						break;
					}
				}
			} else {
				for (Entity e : l.getWorld().getEntities()) {
					if (e.getLocation().getBlock().equals(remote.getBlock())) {
						remote.setPitch(e.getLocation().getPitch());
						remote.setYaw(e.getLocation().getYaw());
						e.teleport(new Location(pad.getWorld(), pad.getX() + .5, pad.getY() + 1, pad.getZ() + .5));
						break;
					}
				}
			}
			return false;
		}
	}

	private void consumeFuel(Chest chest, Location destination) {
		Inventory inv = chest.getBlockInventory();
		int cost = (int) (l.distance(destination) / 75 + .5);
		for (int i = 0; i < inv.getSize(); i++) {
			if (cost <= 0) {
				break;
			}
			ItemStack is = inv.getItem(i);
			int rate = 0;
			switch (is.getType()) {
			case SULPHUR:
				rate = 1;
				break;
			case REDSTONE:
				rate = 2;
				break;
			case BLAZE_POWDER:
				rate = 3;
				break;
			case GLOWSTONE_DUST:
				rate = 4;
				break;
			case BLAZE_ROD:
				rate = 6;
				break;
			default:
				break;
			}
			if (rate > 0) {
				int quantity = (int) (is.getAmount() - Math.ceil(cost / rate));
				if (is.getAmount() <= quantity) {
					cost -= is.getAmount() * rate;
					is = null;
				} else {
					is.setAmount(quantity);
					cost = 0;
				}
				inv.setItem(i, is);
			}
		}
	}
}
