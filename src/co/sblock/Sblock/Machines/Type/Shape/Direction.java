package co.sblock.Sblock.Machines.Type.Shape;

import org.bukkit.entity.Player;

/**
 * <code>Enum</code> for compass direction based on <code>Player</code> yaw.
 * @author Jikoo
 */
public enum Direction {
	NORTH((byte) 0), EAST((byte) 1), SOUTH((byte) 2), WEST((byte) 3);

	private byte dirNum;
	Direction(byte b) {
		dirNum = b;
	}

	/**
	 * Get the numeric representation of a number.
	 * 
	 * @return the byte that represents this <code>Direction</code>
	 */
	public byte getDirectionNumber() {
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
}
