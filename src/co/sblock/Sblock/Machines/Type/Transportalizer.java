package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * Machine for Entity teleportation.
 * <p>
 * Costs fuel based on distance: 1 unit of fuel per 75 blocks of direct line
 * travel rounded up. Gunpowder = 1 fuel, redstone = 2, blaze powder = 3,
 * glowstone = 4, blaze rod = 6
 * <p>
 * Does not store excess fuel, uses first valid fuel object(s) available.
 * 
 * @author Jikoo
 */
public class Transportalizer extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public Transportalizer(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.QUARTZ_BLOCK);
		shape.addBlock(new Vector(-1, 0, 0), m);
		shape.addBlock(new Vector(1, 0, 0), m);
		shape.addBlock(new Vector(-1, 0, 1), m);
		shape.addBlock(new Vector(0, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(-1, 2, 1), m);
		shape.addBlock(new Vector(0, 2, 1), m);
		shape.addBlock(new Vector(1, 2, 1), m);
		m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 2);
		shape.addBlock(new Vector(0, 1, 1), m);
		m = new MaterialData(Material.WOOD_BUTTON, d.getButtonByte());
		shape.addBlock(new Vector(-1, 2, 0), m);
		shape.addBlock(new Vector(1, 2, 0), m);
		m = new MaterialData(Material.STEP, (byte) 7);
		shape.addBlock(new Vector(-1, 0, -1), m);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(1, 0, -1), m);
		m = new MaterialData(Material.NETHER_FENCE);
		shape.addBlock(new Vector(-1, 1, 1), m);
		shape.addBlock(new Vector(1, 1, 1), m);
		m = new MaterialData(Material.CARPET, (byte) 14);
		shape.addBlock(new Vector(-1, 1, 0), m);
		m = new MaterialData(Material.CARPET, (byte) 5);
		shape.addBlock(new Vector(1, 1, 0), m);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.TRANSPORTALIZER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
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
			y = y > 0 ? y < 256 ? y : 255 : 63;
			Location remote = new Location(event.getClickedBlock().getWorld(),
					Double.parseDouble(locString[0]) + .5, y, Double.parseDouble(locString[2]) + .5);

			// CHECK FUEL
			Chest chest = (Chest) l.getBlock().getState();
			Inventory chestInv = chest.getInventory();
			if (!chestInv.contains(Material.SULPHUR) && !chestInv.contains(Material.REDSTONE)
					&& !chestInv.contains(Material.BLAZE_POWDER)
					&& !chestInv.contains(Material.GLOWSTONE_DUST)
					&& !chestInv.contains(Material.BLAZE_ROD)) {
				event.getPlayer().sendMessage(ChatColor.RED
						+ "The transportalizer makes a sputtering noise, but nothing happens."
						+ "\nIt occurs to you that perhaps you should check the fuel level."
						+ "\nYou give yourself a pat on the back for being a troubleshooting genius.");
				return false;
			}

			ArrayList<ItemStack> removedFuel = consumeFuel(chest, remote);
			if (removedFuel != null) {
				for (ItemStack is : removedFuel) {
					chest.getInventory().addItem(is);
				}
				event.getPlayer().sendMessage(ChatColor.RED
						+ "The Transportalizer begins humming through standard teleport procedure,"
						+ " when all of a sudden it chokes to a halt with an awful screeching noise."
						+ "\nPerhaps it requires more fuel?");
				l.getWorld().playSound(l, Sound.ENDERMAN_SCREAM, 10, 3);
				return false;
			}
			// adam sound based on chest fill? maybe just raw usable ItemStacks rather than quantity for speed
			l.getWorld().playSound(l, Sound.NOTE_PIANO, 5, 5);

			// TELEPORT
			// Messy, but avoids deprecation for now.
			Block pad = event.getClickedBlock().getRelative(BlockFace.DOWN);
			if (pad.getState().getData().toItemStack().getDurability() == (short) 5) {
				for (Entity e : l.getWorld().getEntities()) {
					if (e.getLocation().getBlock().equals(pad)) {
						remote.setPitch(e.getLocation().getPitch());
						remote.setYaw(e.getLocation().getYaw());
						e.teleport(remote);
						pad.getWorld().playEffect(pad.getLocation(), Effect.ENDER_SIGNAL, 4);
						pad.getWorld().playEffect(remote, Effect.ENDER_SIGNAL, 4);
						return false;
					}
				}
			} else {
				for (Entity e : l.getWorld().getEntities()) {
					if (e.getLocation().getBlock().equals(remote.getBlock())) {
						remote.setPitch(e.getLocation().getPitch());
						remote.setYaw(e.getLocation().getYaw());
						e.teleport(new Location(pad.getWorld(), pad.getX() + .5, pad.getY(), pad.getZ() + .5));
						pad.getWorld().playEffect(pad.getLocation(), Effect.ENDER_SIGNAL, 4);
						pad.getWorld().playEffect(remote, Effect.ENDER_SIGNAL, 4);
						return false;
					}
				}
			}
			return false;
		}
	}

	// Adam debug removal adding // javadoc
	private ArrayList<ItemStack> consumeFuel(Chest chest, Location destination) {
		Inventory inv = chest.getInventory();
		ArrayList<ItemStack> removed = new ArrayList<ItemStack>();
		int cost = (int) (l.distance(destination) / 75 + 1);
		for (int i = 0; i < inv.getSize(); i++) {
			if (cost <= 0) {
				break;
			}
			ItemStack is = inv.getItem(i);
			if (is != null) {
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
					int quantity = (int) (is.getAmount()- Math.ceil(cost / rate));
					if (is.getAmount() <= quantity) {
						removed.add(is.clone());
						cost -= is.getAmount() * rate;
						is = null;
					} else {
						ItemStack rm = is.clone();
						rm.setAmount(is.getAmount() - quantity);
						removed.add(rm);
						is.setAmount(quantity);
						cost = 0;
					}
					chest.getInventory().setItem(i, is);
				}
			}
		}
		if (cost <= 0) {
			return null;
		}
		return removed;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#postAssemble()
	 */
	@Override
	protected void postAssemble() {
	}
}
