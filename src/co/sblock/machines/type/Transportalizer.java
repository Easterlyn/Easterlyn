package co.sblock.machines.type;

import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import com.dsh105.holoapi.HoloAPI;
import com.dsh105.holoapi.api.Hologram;
import com.dsh105.holoapi.api.HologramFactory;

import co.sblock.Sblock;
import co.sblock.machines.utilities.MachineType;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.inventory.InventoryUtils;

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
	private Hologram fuelHolo;
	/**
	 * @see co.sblock.machines.type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public Transportalizer(Location l, String data, Direction d) {
		super(l, data, d);
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
		m = new MaterialData(Material.DISPENSER, (byte) 1);
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
		Location holoLoc = null;
		for (Entry<Location, MaterialData> e : blocks.entrySet()) {
			if (e.getValue().equals(new MaterialData(Material.STAINED_GLASS))) {
				holoLoc = e.getKey().clone().add(0.5, 0, 0.5);
				break;
			}
		}
		fuelHolo = new HologramFactory(Sblock.getInstance()).withLocation(holoLoc)
				.withText(String.valueOf(fuel)).build();
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
			// HoloAPI doesn't accept line updates till all holograms are loaded at 10 seconds after start.
			Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
				@Override
				public void run() {
					if (fuelHolo != null) {
						fuelHolo.updateLine(0, String.valueOf(fuel));
					}
				}
			}, 200);
		} catch (NumberFormatException e)  {
			fuel = 0;
		}
	}

	/**
	 * @see co.sblock.machines.type.Machine#handleHopper(InventoryMoveItemEvent)
	 */
	@Override
	public boolean handleHopper(final org.bukkit.event.inventory.InventoryMoveItemEvent event) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {

			@Override
			public void run() {
				try {
					for (int i = 0; i < event.getSource().getSize(); i++) {
						if (event.getSource().getItem(i) == null) {
							continue;
						}
						if (hasValue(event.getSource().getItem(i).getType())) {
							fuel += getValue(event.getSource().getItem(i).getType());
							fuelHolo.updateLine(0, String.valueOf(fuel));
							key.getWorld().playSound(key, Sound.ORB_PICKUP, 10, 1);
							event.getSource().setItem(i, InventoryUtils.decrement(event.getSource().getItem(i), 1));
							break;
						} else {
							key.getWorld().dropItem(key.clone().add(Shape.getRelativeVector(direction
											.getRelativeDirection(Direction.SOUTH), new Vector(0.5, 0.5, -1.5))),
									event.getSource().getItem(i));
							event.getSource().setItem(i, null);
							break;
						}
					}
				} catch (Exception e) {
					// Machine was destroyed, probably with items in the hopper.
				}
			}
			
		});
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
	 * @see co.sblock.data.sql.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
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

		User user = UserManager.getUser(event.getPlayer().getUniqueId());
		if (user != null && user.getProgression() == ProgressionState.NONE) {
			// Transportalizers can only be used by players who have completed Entry.
			// Any entity, including pre-entry players, can be transported by a
			// post-entry player pressing the button.
			return true;
		}

		if (!event.getClickedBlock().getType().equals(Material.WOOD_BUTTON)) {
			return false;
		}

		// Check for a sign in the proper location
		Block signBlock = this.key.clone().add(new Vector(0, 2, 0)).getBlock();
		if (!signBlock.getType().equals(Material.WALL_SIGN)) {
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

		// Parse remote location. Do not allow invalid height.
		String[] locString = line3.split(",");
		int y = Integer.parseInt(locString[1]);
		y = y > 0 ? y < 256 ? y : 255 : 63;
		Location remote = new Location(event.getClickedBlock().getWorld(),
				Double.parseDouble(locString[0]) + .5, y, Double.parseDouble(locString[2]) + .5);

		// 
		int cost = (int) (key.distance(remote) / 50 + 1);
		// CHECK FUEL
		if (fuel < cost) {
			event.getPlayer().sendMessage(ChatColor.RED
					+ "The Transportalizer begins humming through standard teleport procedure,"
					+ " when all of a sudden it growls to a halt."
					+ "\nPerhaps it requires more fuel?");
			key.getWorld().playSound(key, Sound.WOLF_GROWL, 16, 2);
			return false;
		}

		key.getWorld().playSound(key, Sound.NOTE_PIANO, 5, 2);

		// TELEPORT
		// Messy, but avoids deprecation for now.
		Block pad = event.getClickedBlock().getRelative(BlockFace.DOWN);
		if (pad.getState().getData().toItemStack().getDurability() == (short) 5) {
			for (Entity e : key.getWorld().getEntities()) {
				if (e.getLocation().getBlock().equals(pad)) {
					key.getWorld().playSound(key, Sound.NOTE_PIANO, 5, 2);
					fuel -= cost;
					fuelHolo.updateLine(0, String.valueOf(fuel));
					remote.setPitch(e.getLocation().getPitch());
					remote.setYaw(e.getLocation().getYaw());
					e.teleport(remote);
					pad.getWorld().playEffect(pad.getLocation(), Effect.ENDER_SIGNAL, 4);
					pad.getWorld().playEffect(remote, Effect.ENDER_SIGNAL, 4);
					return false;
				}
			}
		} else {
			for (Entity e : key.getWorld().getEntities()) {
				if (e.getLocation().getBlock().equals(remote.getBlock())) {
					fuel -= cost;
					fuelHolo.updateLine(0, String.valueOf(fuel));
					e.teleport(new Location(pad.getWorld(), pad.getX() + .5, pad.getY(), pad.getZ() + .5,
							e.getLocation().getYaw(), e.getLocation().getPitch()));
					key.getWorld().playSound(key, Sound.NOTE_PIANO, 5, 2);
					pad.getWorld().playEffect(pad.getLocation(), Effect.ENDER_SIGNAL, 4);
					pad.getWorld().playEffect(remote, Effect.ENDER_SIGNAL, 4);
					return false;
				}
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
		HoloAPI.getManager().stopTracking(fuelHolo);
		HoloAPI.getManager().clearFromFile(fuelHolo);
		fuelHolo.clearAllPlayerViews();
		fuelHolo = null;
	}

	@Override
	public MachineSerialiser getSerialiser() {
		return new MachineSerialiser(key, owner, direction, data, MachineType.TRANSPORTALIZER);
	}
}
