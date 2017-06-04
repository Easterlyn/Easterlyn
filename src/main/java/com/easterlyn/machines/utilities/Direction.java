package com.easterlyn.machines.utilities;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

/**
 * Enum for compass direction based on Player yaw.
 * 
 * @author Jikoo
 */
public enum Direction {

	NORTH((byte) 3, (byte) 3, (byte) 3, (byte) 4), EAST((byte) 2, (byte) 4, (byte) 0, (byte) 3),
	SOUTH((byte) 4, (byte) 2, (byte) 2, (byte) 4), WEST((byte) 1, (byte) 5, (byte) 1, (byte) 3),
	UP((byte) 0, (byte) 3, (byte) 3, (byte) 2), DOWN((byte) 0, (byte) 3, (byte) 3, (byte) 2);

	/* The byte for rotating a button to face the correct direction. */
	private final byte button;

	/* The byte for rotating a chest or furnace to face the correct direction. */
	private final byte chest;

	/* The byte for rotating stairs to face the correct direction. */
	private final byte stair;

	/* The byte for rotating quartz pillars to face the correct direction. */
	private final byte quartzPillar;

	/**
	 * Constructor for Direction.
	 *
	 * @param button the button direction byte
	 * @param chest the chest or furnace direction byte
	 * @param stair the stair direction byte
	 * @param quartzPillar the quartz pillar direction byte
	 */
	Direction(byte button, byte chest, byte stair, byte quartzPillar) {
		this.button = button;
		this.chest = chest;
		this.stair = stair;
		this.quartzPillar = quartzPillar;
	}

	/**
	 * Used to determine data values for directional blocks in Machines.
	 * <p>
	 * Valid types: anvil, button, chest, hopper, stair, upperstair, door, upperdoor.
	 * 
	 * @param type the name of the directional block type
	 * @return the byte for a block of the type specified in the correct rotation
	 */
	public byte getTypeByte(String type) {
		switch (type) {
		case "anvil":
			return (byte) (this.ordinal() % 2 == 0 ? 1 : 0);
		case "button":
			return button;
		case "chest":
		case "hopper":
			return chest;
		case "door":
			return (byte) (this.ordinal() > 3 || this.ordinal() == 0 ? 3 : this.ordinal() - 1);
		case "portal":
			return (byte) (this.ordinal() % 2 + 1);
		case "stair":
			return stair;
		case "upperdoor":
			return 8;
		case "upperstair":
			return (byte) (stair + 4);
		case "bedfoot":
			return (byte) ((this.ordinal() + 2) % 4); 
		case "bedhead":
			return (byte) ((this.ordinal() + 2) % 4 + 8);
		case "quartzpillar":
			return this.quartzPillar;
		default:
			return 0;
		}
	}

	/**
	 * Gets the Bukkit BlockFace corresponding with this Direction.
	 * 
	 * @return the BlockFace
	 */
	public BlockFace toBlockFace() {
		switch(this) {
		case EAST:
			return BlockFace.EAST;
		case NORTH:
			return BlockFace.NORTH;
		case SOUTH:
			return BlockFace.SOUTH;
		case WEST:
			return BlockFace.WEST;
		case UP:
			return BlockFace.UP;
		case DOWN:
			return BlockFace.DOWN;
		default:
			return BlockFace.SELF;
		}
	}

	/**
	 * For obtaining rotation based on original rotation - for stairs, etc. that
	 * are not facing in the place Direction of the Machine.
	 * <p>
	 * The input Direction is the desired rotation for blocks relative to the
	 * original Direction. The original Direction is assumed to be north
	 * relative to the new Direction, as all Shapes are designed from a
	 * north-facing perspective.
	 * <p>
	 * Ex.: Machine placed west, block in machine faces east when machine is
	 * placed north.
	 * 
	 * @param direction the Direction relative to this as north.
	 */
	public Direction getRelativeDirection(Direction direction) {
		if (this.ordinal() > 3) {
			return this;
		}
		return values()[(this.ordinal() + direction.ordinal()) % 4];
	}

	/**
	 * Get a Direction based on Player facing.
	 * 
	 * @param player the Player
	 * 
	 * @return the Direction
	 */
	public static Direction getFacingDirection(Player player) {
		return getFacingDirection(player.getLocation());
	}

	/**
	 * Get a Direction based on Location yaw.
	 * 
	 * @param location the Location
	 * 
	 * @return the Direction
	 */
	public static Direction getFacingDirection(Location location) {
		byte playerFace = (byte) Math.round(location.getYaw() / 90);
		if (playerFace == 0 || playerFace == -4 || playerFace == 4)
			return SOUTH;
		else if (playerFace == 1 || playerFace == -3)
			return WEST;
		else if (playerFace == 2 || playerFace == -2)
			return NORTH;
		else
			return EAST;
	}

}
