package co.sblock.Sblock.Machines.Type;

import org.bukkit.entity.Player;

/**
 * <code>Enum</code> for compass direction based on <code>Player</code> yaw.
 * 
 * @author Jikoo
 */
public enum Direction {
	NORTH((byte) 0, (byte) 3, (byte) 3, (byte) 3), EAST((byte) 1, (byte) 2, (byte) 4, (byte) 0),
	SOUTH((byte) 2, (byte) 4, (byte) 2, (byte) 2), WEST((byte) 3, (byte) 1, (byte) 5, (byte) 1);

	private byte dirNum;
	private byte button;
	private byte chest;
	private byte stair;
	Direction(byte b, byte button, byte chest, byte stair) {
		dirNum = b;
		this.button = button;
		this.chest = chest;
		this.stair = stair;
	}

	/**
	 * Get the numeric representation of a <code>Direction</code>.
	 * 
	 * @return the <code>byte</code> that represents this <code>Direction</code>
	 */
	public byte getDirByte() {
		return dirNum;
	}

	/**
	 * Get a <code>Direction</code> based on <code>Player</code> facing.
	 * @param p
	 *            the <code>Player</code>
	 * @return the <code>Direction</code> to rotate the <code>Shape</code> to face
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
	 * For determining rotation of buttons in a <code>Machine</code>'s
	 * <code>Shape</code>.
	 * 
	 * @return <code>byte</code>
	 */
	public byte getButtonByte() {
		return button;
	}

	/**
	 * For determining rotation of chests in a <code>Machine</code>'s
	 * <code>Shape</code>.
	 * 
	 * @return <code>byte</code>
	 */
	public byte getChestByte() {
		return chest;
	}

	/**
	 * For determining rotation of stairs in a <code>Machine</code>'s
	 * <code>Shape</code>.
	 * 
	 * @return <code>byte</code>
	 */
	public byte getStairByte() {
		return stair;
	}

	/**
	 * For determining rotation of stairs in a <code>Machine</code>'s
	 * <code>Shape</code>.
	 * 
	 * @return <code>byte</code>
	 */
	public byte getUpperStairByte() {
		return (byte) (stair + 4);
	}

	/**
	 * For obtaining rotation based on original rotation - for stairs, etc. that
	 * are not facing in the place <code>Direction</code> of the
	 * <code>Machine</code>.
	 * <p>
	 * The input <code>Direction</code> is the desired rotation for blocks
	 * relative to the original <code>Direction</code>. The original
	 * <code>Direction</code> is assumed to be north relative to the new
	 * <code>Direction</code>, as all <code>Shape</code>s are designed from a
	 * north-facing perspective.
	 * <p>
	 * Ex.: Machine placed west, block in machine faces east when machine is placed north.
	 * 
	 * @param d the <code>Direction</code> relative to this as north.
	 */
	public Direction getRelativeDirection(Direction d) {
		return getDirection((byte) ((this.dirNum + d.getDirByte()) % 4));
	}

	/**
	 * Gets <code>Direction</code> by <code>byte</code>. For database use.
	 * 
	 * @param direction
	 *            <code>byte</code>
	 * @return <code>Direction</code>
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
