package co.sblock.machines.type;

import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.MachineType;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.users.OfflineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Users;

/**
 * Machine for Entity teleportation.
 * <p>
 * Costs fuel based on distance: 1 unit of fuel per 50 blocks of direct line
 * travel rounded up. Gunpowder = 1 fuel, redstone = 2, blaze powder = 3,
 * glowstone = 4, blaze rod = 6, glowstone block = 16, redstone block = 18.
 * <p>
 * Does not store excess fuel, uses first valid fuel object(s) available.
 * 
 * @author Jikoo
 */
public class Transportalizer extends Machine {

	private long fuel;
//	private Hologram hologram;
	/**
	 * @see co.sblock.machines.type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public Transportalizer(Location l, String owner, Direction d) {
		super(l, owner, d);
		MaterialData m = new MaterialData(Material.HOPPER, d.getRelativeDirection(Direction.SOUTH).getChestByte());
		shape.addBlock(new Vector(0, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_BLOCK);
		shape.addBlock(new Vector(-1, 0, 0), m);
		shape.addBlock(new Vector(1, 0, 0), m);
		shape.addBlock(new Vector(-1, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(-1, 2, 1), m);
		shape.addBlock(new Vector(0, 2, 1), m);
		shape.addBlock(new Vector(1, 2, 1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, d.getUpperStairByte());
		shape.addBlock(new Vector(0, 0, 1), m);
		m = new MaterialData(Material.STAINED_GLASS);
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

		fuel = 0;
//		Location holoLoc = null;
//		for (Entry<Location, MaterialData> e : blocks.entrySet()) {
//			if (e.getValue().getItemType() == Material.STAINED_GLASS) {
//				holoLoc = e.getKey().clone().add(0.5, 0.5, 0.5);
//				break;
//			}
//		}
//		hologram = HolographicDisplaysAPI.createHologram(Sblock.getInstance(), holoLoc, String.valueOf(fuel));
	}

	/**
	 * @see co.sblock.machines.type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.TRANSPORTALIZER;
	}

	/**
	 * @see co.sblock.machines.type.Machine#getType()
	 */
	@Override
	public String getData() {
		return String.valueOf(fuel);
	}

	/**
	 * @see co.sblock.machines.type.Machine#getType()
	 */
	@Override
	public void setData(String data) {
		try {
			fuel = Long.valueOf(data);
//			hologram.setLine(0, String.valueOf(fuel));
//			hologram.update();
		} catch (NumberFormatException e)  {
			fuel = 0;
		}
	}

	/**
	 * @see co.sblock.machines.type.Machine#handleHopperPickupItem(org.bukkit.event.inventory.InventoryPickupItemEvent)
	 */
	@Override
	public boolean handleHopperPickupItem(InventoryPickupItemEvent event) {
		ItemStack inserted = ((Item) event.getItem()).getItemStack();
		if (hasValue(inserted.getType())) {
			fuel += getValue(inserted.getType()) * inserted.getAmount();
//			hologram.setLine(0, String.valueOf(fuel));
//			hologram.update();
			key.getWorld().playSound(key, Sound.ORB_PICKUP, 10, 1);
			event.getItem().remove();
		} else {
			event.getItem().teleport(key.clone().add(Shape.getRelativeVector(direction.getRelativeDirection(Direction.SOUTH), new Vector(0.5, 0.5, -1.5))));
		}
		return true;
	}

	/**
	 * Checks if a Material is a fuel
	 * 
	 * @param m the Material to check
	 * 
	 * @return true if the Material is a fuel
	 */
	private boolean hasValue(Material m) {
		return m == Material.SULPHUR || m == Material.REDSTONE || m == Material.BLAZE_POWDER
				|| m == Material.GLOWSTONE_DUST || m == Material.BLAZE_ROD
				|| m == Material.GLOWSTONE || m == Material.REDSTONE_BLOCK;
	}

	/**
	 * Gets the fuel value for the Material provided.
	 * 
	 * @param m the Material to check
	 * 
	 * @return the fuel value of the Material
	 */
	private int getValue(Material m) {
		switch (m) {
		case SULPHUR:
			return 1;
		case REDSTONE:
			return 2;
		case BLAZE_POWDER:
			return 3;
		case GLOWSTONE_DUST:
			return 4;
		case BLAZE_ROD:
			return 6;
		case GLOWSTONE:
			return 16;
		case REDSTONE_BLOCK:
			return 18;
		default:
			return 0;
		}
	}

	/**
	 * @see co.sblock.machines.type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (super.handleInteract(event)) {
			return true;
		}

		// Dispenser inventory really only exists for easy slow fuel consumption.
		// Players should not be able to access it.
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.DISPENSER) {
			return true;
		}

		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user != null && user.getProgression() == ProgressionState.NONE) {
			// Transportalizers can only be used by players who have completed Entry.
			// Any entity, including pre-entry players, can be transported by a
			// post-entry player pressing the button.
			return true;
		}

		if (event.getClickedBlock().getType() != Material.WOOD_BUTTON) {
			return false;
		}

		// Check for a sign in the proper location
		Block signBlock = this.key.clone().add(new Vector(0, 2, 0)).getBlock();
		if (signBlock.getType() != Material.WALL_SIGN) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "Please place a sign on your transportalizer between the buttons to use it."
					+ "\nThe third row should contain your desired coordinates in x,y,x format."
					+ "\nAll the other rows can contain whatever you like.");
			return false;
		}

		// Check sign for proper format - sign lines are 0-3, third line is line 2
		String line3 = ((Sign) signBlock.getState()).getLine(2);
		if (!line3.matches("\\-?[0-9]+,[0-9]+,\\-?[0-9]+")) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The third line of your transportalizer sign must contain "
					+ "your desired destination in x,y,z format. Ex: 0,64,0");
			return false;
		}

		// Parse remote location. Do not allow invalid height or coords. TODO warn?
		String[] locString = line3.split(",");
		int x = Integer.parseInt(locString[0]);
		x = x > 25000 ? 25000 : x < -25000 ? -25000 : x;
		int y = Integer.parseInt(locString[1]);
		y = y > 0 ? y < 256 ? y : 255 : 63;
		int z = Integer.parseInt(locString[2]);
		z = z > 25000 ? 25000 : z < -25000 ? -25000 : z;
		Location remote = new Location(event.getClickedBlock().getWorld(), x, y, z);

		// 50 fuel per block of distance, rounded up.
		int cost = (int) (key.distance(remote) / 50 + 1);
		// CHECK FUEL
		if (fuel < cost) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The Transportalizer begins humming through standard teleport procedure,"
					+ " when all of a sudden it growls to a halt."
					+ "\nPerhaps it requires more fuel?");
			key.getWorld().playSound(key, Sound.WOLF_GROWL, 16, 0);
			return false;
		}

		// TELEPORT
		Block pad = event.getClickedBlock().getRelative(BlockFace.DOWN);
		Location source;
		Location target;
		if (pad.getState().getData().getData() == 5) {
			source = pad.getLocation();
			target = remote;
		} else {
			source = remote;
			target = pad.getLocation();
		}
		for (Entity e : key.getWorld().getEntities()) {
			if (e.getLocation().getBlock().equals(source.getBlock())) {
				source.getWorld().playSound(source, Sound.NOTE_PIANO, 5, 2);
				target.getWorld().playSound(target, Sound.NOTE_PIANO, 5, 2);
				fuel -= cost;
//				hologram.setLine(0, String.valueOf(fuel));
//				hologram.update();
				e.teleport(new Location(target.getWorld(), target.getX() + .5, target.getY(), target.getZ() + .5,
						e.getLocation().getYaw(), e.getLocation().getPitch()));
				key.getWorld().playSound(key, Sound.NOTE_PIANO, 5, 2);
				source.getWorld().playEffect(source, Effect.ENDER_SIGNAL, 4);
				source.getWorld().playEffect(target, Effect.ENDER_SIGNAL, 4);
				return false;
			}
		}
		return false;
	}

	/**
	 * @see co.sblock.machines.type.Machine#handleClick(org.bukkit.event.inventory.InventoryClickEvent)
	 */
	@Override
	public boolean handleClick(InventoryClickEvent event) {
		return false;
	}

	/**
	 * @see co.sblock.machines.type.Machine#disable()
	 */
	@Override
	public void disable() {
//		hologram.delete();
	}
}
