package com.easterlyn.utilities;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_12_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.ArrayList;
import java.util.List;

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
		CraftWorld w = (CraftWorld) l.getWorld();

		BlockPosition blockPos = new BlockPosition(l.getBlockX(), l.getBlockY(), l.getBlockZ());
		// Sets the light source at the location to the level
		w.getHandle().a(EnumSkyBlock.BLOCK, blockPos, level);

		updateChunk(l);
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

	/**
	 * Gets all the chunks touching/diagonal to the chunk the location is in and updates players
	 * with them.
	 *
	 * @param l the location
	 */
	private static void updateChunk(Location l) {
		List<Chunk> chunks = new ArrayList<>();

		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				chunks.add(((CraftChunk) l.clone().add(16 * x, 0, 16 * z).getChunk()).getHandle());
			}
		}

		// TODO 1.12
//		PacketPlayOutMapChunkBulk packet = new PacketPlayOutMapChunkBulk(chunks);
//
//		for (Player player : l.getWorld().getPlayers()) {
//			if (player.getLocation().distance(l) <= Bukkit.getServer().getViewDistance() * 16) {
//				((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
//			}
//		}
	}

}
