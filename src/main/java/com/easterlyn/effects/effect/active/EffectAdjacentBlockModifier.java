package com.easterlyn.effects.effect.active;

import java.util.Arrays;
import java.util.Collection;

import com.easterlyn.Easterlyn;
import com.easterlyn.effects.effect.BehaviorActive;
import com.easterlyn.effects.effect.Effect;
import com.easterlyn.events.BlockUpdateManager;
import com.easterlyn.events.Events;
import com.easterlyn.events.event.SblockBreakEvent;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * Base for multiple block break behaviors that affect adjacent blocks.
 * 
 * @author Jikoo
 */
public abstract class EffectAdjacentBlockModifier extends Effect implements BehaviorActive {

	private final BlockUpdateManager budManager;
	private final BlockFace[] faces;
	private final Material[] updateMaterials;
	protected int currentCount;

	protected EffectAdjacentBlockModifier(Easterlyn plugin, int cost, String name, Material... updateMaterials) {
		super(plugin, cost, 1, 1, name);
		this.budManager = plugin.getModule(Events.class).getBlockUpdateManager();
		this.faces = new BlockFace[] { BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH,
				BlockFace.EAST, BlockFace.WEST };
		this.updateMaterials = updateMaterials;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(BlockBreakEvent.class, SblockBreakEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		BlockBreakEvent breakEvent = (BlockBreakEvent) event;
		Player player = breakEvent.getPlayer();
		this.currentCount = 0;
		for (BlockFace face : faces) {
			Block relative = breakEvent.getBlock().getRelative(face);
			if (handleAdjacentBlock(player, relative) && updateMaterials.length > 0) {
				++this.currentCount;
				checkRelative: for (BlockFace toUpdateFace : faces) {
					Block toUpdate = relative.getRelative(toUpdateFace);
					for (Material material : updateMaterials) {
						if (material == toUpdate.getType()) {
							budManager.queueBlock(relative);
							break checkRelative;
						}
					}
				}
			}
		}
	}

	protected abstract boolean handleAdjacentBlock(Player player, Block block);

}
