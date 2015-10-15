package co.sblock.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

import co.sblock.effects.effect.BehaviorActive;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;
import co.sblock.utilities.BlockDrops;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Effect for automatically harvesting and planting crops when right clicked.
 * 
 * @author Jikoo
 */
public class EffectGreenThumb extends Effect implements BehaviorActive, BehaviorGodtier {

	public EffectGreenThumb() {
		super(Integer.MAX_VALUE, 1, 1, "Green Thumb");
	}

	@Override
	public Collection<UserAspect> getAspects() {
		return Arrays.asList(UserAspect.LIFE);
	}

	@Override
	public List<String> getDescription(UserAspect aspect) {
		ArrayList<String> list = new ArrayList<>();
		switch (aspect) {
		case MIND:
			list.add(aspect.getColor() + "Harvest Goddess");
			break;
		default:
			break;
		}
		list.add(ChatColor.WHITE + "Tending plants made easy.");
		list.add(ChatColor.GRAY + "Right click crops to harvest and replant.");
		return list;
	}

	@Override
	public Collection<Class<? extends Event>> getApplicableEvents() {
		return Arrays.asList(PlayerInteractEvent.class);
	}

	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		PlayerInteractEvent evt = (PlayerInteractEvent) event;
		if (!evt.hasBlock() || evt.getAction() == Action.LEFT_CLICK_BLOCK) {
			return;
		}
		Block clickedBlock = evt.getClickedBlock();
		BlockState clickedState = clickedBlock.getState();
		Material seed;
		if (clickedState instanceof Crops) {
			Crops crops = (Crops) clickedState;
			if (crops.getState() != CropState.RIPE) {
				return;
			}
			crops.setState(CropState.SEEDED);
			seed = crops.getItemType();
			// clicked.update(); // May not be needed
		} else if (clickedState instanceof CocoaPlant) {
			CocoaPlant cocoa = (CocoaPlant) clickedState;
			if (cocoa.getSize() != CocoaPlantSize.LARGE) {
				return;
			}
			cocoa.setSize(CocoaPlantSize.SMALL);
			seed = cocoa.getItemType();
		} else if (clickedState instanceof NetherWarts) {
			// Separate? Could be a neat Doom thing
			NetherWarts warts = (NetherWarts) clickedState;
			if (warts.getState() != NetherWartsState.RIPE) {
				return;
			}
			warts.setState(NetherWartsState.SEEDED);
			seed = warts.getItemType();
		} else {
			return;
		}
		Player player = evt.getPlayer();
		for (ItemStack drop : BlockDrops.getDrops(player, player.getItemInHand(), clickedBlock)) {
			if (drop.getType() == seed) {
				// Re-seed cost
				drop = InventoryUtils.decrement(drop, 1);
			}
			if (drop != null) {
				player.getWorld().dropItem(player.getLocation(), drop).setPickupDelay(0);
			}
		}
	}

}
