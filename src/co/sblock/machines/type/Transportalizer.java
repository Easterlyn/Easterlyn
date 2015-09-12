package co.sblock.machines.type;

import org.bukkit.DyeColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.WorldBorder;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.gmail.filoghost.holographicdisplays.api.Hologram;

import co.sblock.chat.Color;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.utilities.Holograms;

import net.md_5.bungee.api.ChatColor;

/**
 * Machine for Entity teleportation.
 * <p>
 * Costs fuel based on distance: 1 unit of fuel per 50 blocks of direct line
 * travel rounded up. Gunpowder = 1 fuel, redstone = 2, blaze powder = 3,
 * glowstone = 4, blaze rod = 6, glowstone block = 16, redstone block = 18.
 * 
 * @author Jikoo
 */
public class Transportalizer extends Machine {

	private final ItemStack drop;

	@SuppressWarnings("deprecation")
	public Transportalizer() {
		super(new Shape());
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.HOPPER, Direction.SOUTH, "chest");
		shape.setVectorData(new Vector(0, 0, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK);
		shape.setVectorData(new Vector(-1, 0, 0), m);
		shape.setVectorData(new Vector(1, 0, 0), m);
		shape.setVectorData(new Vector(-1, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 2, 1), m);
		shape.setVectorData(new Vector(0, 2, 1), m);
		shape.setVectorData(new Vector(1, 2, 1), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.NORTH, "upperstair");
		shape.setVectorData(new Vector(0, 0, 1), m);
		m = shape.new MaterialDataValue(Material.STAINED_GLASS);
		shape.setVectorData(new Vector(0, 1, 1), m);
		m = shape.new MaterialDataValue(Material.WOOD_BUTTON, Direction.NORTH, "button");
		shape.setVectorData(new Vector(-1, 2, 0), m);
		shape.setVectorData(new Vector(1, 2, 0), m);
		m = shape.new MaterialDataValue(Material.STEP, (byte) 7);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, -1), m);
		m = shape.new MaterialDataValue(Material.NETHER_FENCE);
		shape.setVectorData(new Vector(-1, 1, 1), m);
		shape.setVectorData(new Vector(1, 1, 1), m);
		m = shape.new MaterialDataValue(Material.CARPET, DyeColor.RED.getWoolData());
		shape.setVectorData(new Vector(-1, 1, 0), m);
		m = shape.new MaterialDataValue(Material.CARPET, DyeColor.LIME.getWoolData());
		shape.setVectorData(new Vector(1, 1, 0), m);

		drop = new ItemStack(Material.CHEST);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Transportalizer");
		drop.setItemMeta(meta);
	}

	@Override
	public int getCost() {
		return 1000;
	}

	private Location getHoloLocation(ConfigurationSection storage) {
		return getKey(storage).add(Shape.getRelativeVector(getDirection(storage), new Vector(0.5, 1.5, 1.5)));
	}

	public void setFuel(ConfigurationSection storage, long fuel) {
		Hologram hologram = Holograms.getHologram(getHoloLocation(storage));
		if (hologram != null) {
			hologram.clearLines();
			hologram.appendTextLine(String.valueOf(fuel));
		}
		storage.set("fuel", fuel);
	}

	public long getFuel(ConfigurationSection storage) {
		return storage.getLong("fuel", 0);
	}

	@Override
	public void assemble(BlockPlaceEvent event, ConfigurationSection storage) {
		super.assemble(event, storage);
		setFuel(storage, getFuel(storage));
	}

	@Override
	public boolean handleHopperPickupItem(InventoryPickupItemEvent event, ConfigurationSection storage) {
		ItemStack inserted = event.getItem().getItemStack();
		Location key = getKey(storage);
		if (hasValue(inserted.getType())) {
			setFuel(storage, getFuel(storage) + getValue(inserted.getType()) * inserted.getAmount());
			key.getWorld().playSound(key, Sound.ORB_PICKUP, 10, 1);
			event.getItem().remove();
		} else {
			event.getItem().teleport(
					key.add(Shape.getRelativeVector(
							getDirection(storage).getRelativeDirection(Direction.SOUTH),
							new Vector(0.5, 0.5, -1.5))));
		}
		return true;
	}

	@Override
	public boolean handleHopperMoveItem(InventoryMoveItemEvent event, ConfigurationSection storage) {
		// future: allow pulling from chest or something like that
		return super.handleHopperMoveItem(event, storage);
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

	@SuppressWarnings("deprecation")
	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		if (super.handleInteract(event, storage)) {
			return true;
		}

		// Hopper inventory has to suck up items from the world, it should not be openable.
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK
				&& event.getClickedBlock().getType() == Material.HOPPER) {
			return true;
		}

		if (event.getClickedBlock().getType() != Material.WOOD_BUTTON) {
			return false;
		}

		Location key = getKey(storage);

		// Check for a sign in the proper location
		Block signBlock = key.clone().add(new Vector(0, 2, 0)).getBlock();
		if (signBlock.getType() != Material.WALL_SIGN) {
			event.getPlayer().sendMessage(Color.BAD
					+ "Please place a sign on your transportalizer between the buttons to use it."
					+ "\nThe third row should contain your desired coordinates in x,y,x format."
					+ "\nAll the other rows can contain whatever you like.");
			return false;
		}

		Sign sign = (Sign) signBlock.getState();
		// Check sign for proper format - sign lines are 0-3, third line is line 2
		String line3 = sign.getLine(2);
		if (!line3.matches("\\-?[0-9]+, ?[0-9]+, ?\\-?[0-9]+")) {
			event.getPlayer().sendMessage(Color.BAD
					+ "The third line of your transportalizer sign must contain "
					+ "your desired destination in x, y, z format. Ex: 0, 64, 0");
			return false;
		}

		// Parse remote location. Do not allow invalid height or coords.
		WorldBorder border = key.getWorld().getWorldBorder();
		String[] locString = line3.split(", ?");
		int x0 = Integer.parseInt(locString[0]);
		int max = (int) (border.getCenter().getX() + border.getSize());
		int x = Math.max(-max, Math.min(max, x0));
		int y0 = Integer.parseInt(locString[1]);
		int y = Math.max(1, Math.min(255, y0));
		int z0 = Integer.parseInt(locString[2]);
		max = (int) (border.getCenter().getZ() + border.getSize());
		int z = Math.max(-max, Math.min(max, z0));
		if (x != x0 | y != y0 || z != z0) {
			sign.setLine(2, x + ", " + y + ", " + z);
			sign.update();
		}
		Location remote = new Location(event.getClickedBlock().getWorld(), x, y, z);

		// 50 fuel per block of distance, rounded up.
		int cost = (int) (key.distance(remote) / 50 + 1);
		// CHECK FUEL
		long fuel = getFuel(storage);
		if (fuel < cost) {
			event.getPlayer().sendMessage(Color.BAD
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
				setFuel(storage, fuel - cost);
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

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection section) {
		return false;
	}

	@Override
	public void disable(ConfigurationSection section) {
		Holograms.removeHologram(getHoloLocation(section));
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
