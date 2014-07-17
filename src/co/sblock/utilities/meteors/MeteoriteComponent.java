package co.sblock.utilities.meteors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R4.CraftWorld;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_7_R4.EntityFallingBlock;

/**
 * Wrapper for EntityFallingBlock to allow easier detection of Meteorite components.
 * 
 * @author Jikoo
 */
public class MeteoriteComponent extends EntityFallingBlock {

	private boolean explode;
	private boolean bore;
	@SuppressWarnings("deprecation")
	public MeteoriteComponent(Location l, Material material, boolean explode, boolean bore) {
		super(((CraftWorld) l.getWorld()).getHandle(), l.getBlockX(),
						l.getBlockY(), l.getBlockZ(), net.minecraft.server.v1_7_R4.Block.getById(material.getId()), 0);
		this.explode = explode;
		this.bore = bore;
		this.ticksLived = 1;
		this.dropItem = false;
		((CraftWorld) l.getWorld()).getHandle().addEntity(this, SpawnReason.CUSTOM);
	}

	public boolean shouldExplode() {
		return explode;
	}

	public boolean shouldBore() {
		return bore;
	}
}
