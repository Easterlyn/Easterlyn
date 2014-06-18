package co.sblock.machines.utilities;

import org.bukkit.entity.Player;

/**
 * Enum for compass direction based on Player yaw.
 * 
 * @author Jikoo
 */
public enum Direction {
	NORTH((byte) 0, (byte) 3, (byte) 3, (byte) 3),
	EAST((byte) 1, (byte) 2, (byte) 4, (byte) 0),
	SOUTH((byte) 2, (byte) 4, (byte) 2, (byte) 2),
	WEST((byte) 3, (byte) 1, (byte) 5, (byte) 1);

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
	 * Get a Direction based on Player facing.
	 * 
	 * @param p the Player
	 * 
	 * @return the Direction to rotate the Shape to face
	 */
	public static Direction getFacingDirection(Player p) {
		byte playerFace = (byte) Math.round(p.getLocation().getYaw() / 90);
		if (playerFace == 0 || playerFace == -4 || playerFace == 4)
			return SOUTH;
		else if (playerFace == 1 || playerFace == -3)
			return WEST;
		else if (playerFace == 2 || playerFace == -2)
			return NORTH;
		else
			return EAST;
	}

	/**
	 * For determining rotation of buttons in a Machine's Shape.
	 * 
	 * @return byte
	 */
	public byte getButtonByte() {
		return button;
	}

	/**
	 * For determining rotation of chests in a Machine's Shape.
	 * 
	 * @return byte
	 */
	public byte getChestByte() {
		return chest;
	}

	/**
	 * For determining rotation of stairs in a Machine's Shape.
	 * 
	 * @return byte
	 */
	public byte getStairByte() {
		return stair;
	}

	/**
	 * For determining rotation of stairs in a Machine's Shape.
	 * 
	 * @return byte
	 */
	public byte getUpperStairByte() {
		return (byte) (stair + 4);
	}

	/**
	 * For determining rotation of doors in a Machine's Shape.
	 * 
	 * @return byte
	 */
	public byte getDoorByte() {
		if (dirNum == 0) {
			return 3;
		}
		return (byte) (dirNum - 1);
	}

	/**
	 * Door tops always have a data value of 8, I'm just going to forget this.
	 * 
	 * @return byte
	 */
	public byte getDoorTopByte() {
		return 8;
	}

	/**
	 * For obtaining rotation based on original rotation - for stairs, etc. that are not facing in
	 * the place Direction of the Machine.
	 * <p>
	 * The input Direction is the desired rotation for blocks relative to the original Direction.
	 * The original Direction is assumed to be north relative to the new Direction, as all Shapes
	 * are designed from a north-facing perspective.
	 * <p>
	 * Ex.: Machine placed west, block in machine faces east when machine is placed north.
	 * 
	 * @param d the Direction relative to this as north.
	 */
	public Direction getRelativeDirection(Direction d) {
		return getDirection((byte) ((this.dirNum + d.getDirByte()) % 4));
	}

	/**
	 * Gets Direction by byte. For database use.
	 * 
	 * @param direction byte
	 * 
	 * @return Direction
	 */
	public static Direction getDirection(byte direction) {
		for (Direction d : Direction.values()) {
			if (d.dirNum == direction) {
				return d;
			}
		}
		return NORTH;
	}
}
