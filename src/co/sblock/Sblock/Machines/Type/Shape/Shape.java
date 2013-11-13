package co.sblock.Sblock.Machines.Type.Shape;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * @author Jikoo
 */
public class Shape {

	// javadoc
	private ArrayList<Location> blocks;
	private Location key;

	public Shape(Location l) {
		this.key = l;
	}

	public List<Location> getBlocks() {
		return blocks;
	}

	public void add(int x, int y, int z, Material m) {
		blocks.add(key.add(x, y, z));
	}

	/**
	 * 
	 * @param p
	 *            the player who triggered the event
	 * @return the proper orientation byte for the stair
	 */
	public byte getFacingDirection(Player p) {
		byte playerFace = (byte) Math.round(p.getLocation().getYaw() / 90);
		if (playerFace == 0 || playerFace == -4 || playerFace == 4)
			return 2;// facing south
		else if (playerFace == 1 || playerFace == -3)
			return 1;// facing west
		else if (playerFace == 2 || playerFace == -2)
			return 3;// facing north
		else
			return 0;// facing east
	}
}
