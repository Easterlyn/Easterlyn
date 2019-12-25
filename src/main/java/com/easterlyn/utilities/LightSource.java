package com.easterlyn.utilities;

import net.minecraft.server.v1_15_R1.BlockPosition;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_15_R1.CraftWorld;

/**
 * http://forums.bukkit.org/threads/resource-server-side-lighting-no-it-isnt-just-client-side.154503/
 */
public class LightSource {

	/**
	 * Create light with level at a location.
	 *
	 * @param l the location to light
	 * @param level the level of light
	 */
	public static void createLightSource(Location l, int level) {
		if (!(l.getWorld() instanceof CraftWorld)) {
			return;
		}

		CraftWorld w = (CraftWorld) l.getWorld();

		BlockPosition blockPos = new BlockPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		w.getHandle().getChunkProvider().getLightEngine().a(blockPos, level);
	}

	/**
	 * Updates the block making the light source return to what it actually is
	 *
	 * @param l the location of the light source
	 */
	public static void deleteLightSource(Location l) {
		Block block = l.getBlock();
		BlockState state = block.getState();
		block.setType(block.getType() == Material.STONE ? Material.DIRT : Material.STONE, false);
		state.update(true, true);
	}

}
