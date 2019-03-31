package com.easterlyn.micromodules;

import net.minecraft.server.v1_13_R2.DamageSource;
import net.minecraft.server.v1_13_R2.EntityFallingBlock;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

/**
 * Wrapper for EntityFallingBlock to allow easier detection of Meteorite components.
 *
 * @author Jikoo
 */
public class MeteoriteComponent extends EntityFallingBlock {

	private final boolean explode;
	private final boolean bore;

	@SuppressWarnings("deprecation")
	MeteoriteComponent(Location l, Material material, boolean explode, boolean bore) {
		super(((CraftWorld) l.getWorld()).getHandle(), l.getBlockX(), l.getBlockY(), l.getBlockZ(), CraftMagicNumbers.getBlock(material).getBlockData());
		this.explode = explode;
		this.bore = bore;
		this.ticksLived = 1;
		this.dropItem = false;
		((CraftWorld) l.getWorld()).getHandle().addEntity(this, SpawnReason.CUSTOM);
	}

	public boolean shouldExplode() {
		return explode;
	}

	@Override
	public boolean isInvulnerable(DamageSource source) {
		/*
		 * Cancelling damage no longer prevents physics applying to EntityTNTPrimed and
		 * EntityFallingBlock. As a workaround, we set the MeteoriteComponent invincible.
		 */
		return bore && source != DamageSource.OUT_OF_WORLD;
	}

}
