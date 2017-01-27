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

	NORTH((byte) 0, (byte) 3, (byte) 3, (byte) 3), EAST((byte) 1, (byte) 2, (byte) 4, (byte) 0),
	SOUTH((byte) 2, (byte) 4, (byte) 2, (byte) 2), WEST((byte) 3, (byte) 1, (byte) 5, (byte) 1);

	/** The arbitrarily defined byte deciding direction facing. */
	private byte dirNum;

	/** The byte for rotating a button to face the correct direction. */
	private byte button;

	/** The byte for rotating a chest or furnace to face the correct direction. */
	private byte chest;

	/** The byte for rotating stairs to face the correct direction. */
	private byte stair;

	/**
	 * Constructor for Direction.
	 * 
	 * @param b the direction number
	 * @param button the button direction byte
	 * @param chest the chest or furnace direction byte
	 * @param stair the stair direction byte
	 */
	Direction(byte b, byte button, byte chest, byte stair) {
		dirNum = b;
		this.button = button;
		this.chest = chest;
		this.stair = stair;
	}

	/**
	 * Get the numeric representation of a Direction.
	 * 
	 * @return the byte that represents this Direction
	 */
	public byte getDirByte() {
		return dirNum;
	}

	/**
	 * Used to determine data values for directional blocks in Machines.
	 * <p>
	 * Valid types: anvil, button, chest, stair, upperstair, door, upperdoor.
	 * 
	 * @param type the name of the directional block type
	 * @return the byte for a block of the type specified in the correct rotation
	 */
	public byte getTypeByte(String type) {
		switch (type) {
		case "anvil":
			return (byte) (dirNum % 2 == 0 ? 1 : 0);
		case "button":
			return button;
		case "chest":
		case "hopper":
			return chest;
		case "door":
			if (dirNum == 0) {
				return 3;
			}
			return (byte) (dirNum - 1);
		case "portal":
			return (byte) (dirNum % 2 + 1);
		case "stair":
			return stair;
		case "upperdoor":
			return 8;
		case "upperstair":
			return (byte) (stair + 4);
		case "bedfood":
			return (byte) ((dirNum + 2) % 4); 
		case "bedhead":
			return (byte) ((dirNum + 2) % 4 + 8); 
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
		byte dirNum = (byte) ((this.dirNum + direction.getDirByte()) % 4);
		for (Direction dir : Direction.values()) {
			if (dir.dirNum == dirNum) {
				return dir;
			}
		}
		return NORTH;
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
