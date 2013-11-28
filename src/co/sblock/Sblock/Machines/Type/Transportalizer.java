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
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.Shape.Direction;
import co.sblock.Sblock.Utilities.Sblogger;

/**
 * @author Jikoo
 *
 */
public class Transportalizer extends Machine {

	/**
	 * @param l
	 * @param data
	 */
	public Transportalizer(Location l, String data, Direction d) {
		super(l, data, d);
		ItemStack is = new ItemStack(Material.QUARTZ_BLOCK);
		shape.addBlock(new Location(this.l.getWorld(), -1, 0, 0), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 0, 0), is);
		shape.addBlock(new Location(this.l.getWorld(), -1, 0, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 0, 0, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 0, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), -1, 2, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 0, 2, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 2, 1), is);
		is = new ItemStack(Material.WOOD_BUTTON);
		shape.addBlock(new Location(this.l.getWorld(), -1, 2, 0), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 2, 0), is);
		is = new ItemStack(Material.STEP);
		is.setDurability((short) 7);
		shape.addBlock(new Location(this.l.getWorld(), -1, 0, -1), is);
		shape.addBlock(new Location(l.getWorld(), 0, 0, -1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 0, -1), is);
		is.setDurability((short) 2);
		shape.addBlock(new Location(this.l.getWorld(), 0, 1, 1), is);
		is = new ItemStack(Material.NETHER_FENCE);
		shape.addBlock(new Location(this.l.getWorld(), -1, 1, 1), is);
		shape.addBlock(new Location(this.l.getWorld(), 1, 1, 1), is);
		is.setType(Material.CARPET);
		is.setDurability((short) 5);
		shape.addBlock(new Location(this.l.getWorld(), -1, 1, 0), is);
		is.setDurability((short) 14);
		shape.addBlock(new Location(this.l.getWorld(), 1, 1, 0), is);
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
		blocks = shape.getBuildLocations(getFacingDirection());
		for (Location l : blocks.keySet()) {
			if (!l.getBlock().isEmpty()) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "There isn't enough space to build this Machine here.");
				MachineModule.getInstance().getManager().removeMachineListing(l);
				return;
			}
		}
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			Block b = e.getKey().getBlock();
			b.setType(e.getValue().getType());
			b.setData(e.getValue().getData().getData(), false);
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
			Location signLocation = establishSignLocation(event.getClickedBlock());
			if (signLocation == null) {
				Sblogger.warning("SblockMachines",
						"Unable to establish sign location for registered Transportalizer at "
								+ event.getClickedBlock().getLocation());
				return false;
			}
			Block signBlock = signLocation.getBlock();
			if (!signBlock.getType().equals(Material.SIGN)) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "Please place a sign on your transportalizer between the buttons to use it."
						+ "\nThe third row should contain your desired coordinates in x,y,x format."
						+ "\nAll the other rows can contain whatever you like.");
				return false;
			}
			Sign sign = (Sign) signBlock.getState().getData();
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
					Integer.parseInt(locString[0]), y, Integer.parseInt(locString[2]));

			// CHECK FUEL
			Chest chest = (Chest) l.getBlock().getState().getData();
			if (!chest.getBlockInventory().contains(Material.COAL)) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "The transportalizer makes a sputtering noise, but nothing happens."
						+ "\nIt occurs to you that perhaps you should check the fuel level."
						+ "\nYou give yourself a pat on the back for being a troubleshooting genius.");
				return false;
			}
			// adam sound based on chest fill? maybe just raw usable ItemStacks rather than quantity for speed
			l.getWorld().playSound(l, Sound.NOTE_PIANO, 5f, 5f);

			// TELEPORT
			// Messy, but avoids deprecation for now.
			if (event.getClickedBlock().getRelative(BlockFace.DOWN)
					.getState().getData().toItemStack().getDurability() == (short) 5) {
				chest.getBlockInventory().remove(new ItemStack(Material.COAL));
				event.getPlayer().teleport(remote);
			} else {
				for (Entity e : l.getWorld().getEntities()) {
					if (e.getLocation().getBlock().equals(remote.getBlock())) {
						chest.getBlockInventory().remove(new ItemStack(Material.COAL));
						e.teleport(new Location(l.getWorld(), l.getX(), l.getY() + 1, l.getZ()));
						break;
					}
				}
			}
			return false;
		}
	}

	private Location establishSignLocation(Block b) {
		for (Entry<Location, ItemStack> e : blocks.entrySet()) {
			if (e.getValue().getType().equals(Material.WOOD_BUTTON)) {
				if (!e.getKey().equals(b.getLocation())) {
					if (e.getKey().getBlockX() > b.getX()) {
						return new Location(b.getWorld(),
								b.getX() + 1, b.getY(), b.getZ());
					}
					if (e.getKey().getBlockX() < b.getX()) {
						return new Location(b.getWorld(),
								b.getX() - 1, b.getY(), b.getZ());
					}
					if(e.getKey().getBlockZ() > b.getZ()) {
						return new Location(b.getWorld(),
								b.getX(), b.getY(), b.getZ() + 1);
					}
					if(e.getKey().getBlockZ() < b.getZ()) {
						return new Location(b.getWorld(),
								b.getX(), b.getY(), b.getZ() - 1);
					}
				}
			}
		}
		return null;
	}
}
