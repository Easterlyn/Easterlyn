package co.sblock.utilities.meteors;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_7_R3.CraftWorld;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import net.minecraft.server.v1_7_R3.EntityFallingBlock;

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
		super(((CraftWorld) l.getWorld()).getHandle(), l.getBlockX(), l.getBlockY(), l.getBlockZ(),
				net.minecraft.server.v1_7_R3.Block.e(material.getId()), 0);
		this.explode = explode;
		this.bore = bore;
		this.b = 1;
		((CraftWorld) l.getWorld()).getHandle().addEntity(this, SpawnReason.CUSTOM);
		((FallingBlock) this.getBukkitEntity()).setDropItem(false);
	}

	public boolean shouldExplode() {
		return explode;
	}

	public boolean shouldBore() {
		return bore;
	}
}
