package co.sblock.effects.effect.godtier.active;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.CocoaPlant.CocoaPlantSize;
import org.bukkit.material.Crops;
import org.bukkit.material.MaterialData;
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

	@SuppressWarnings("deprecation")
	@Override
	public void handleEvent(Event event, LivingEntity entity, int level) {
		PlayerInteractEvent evt = (PlayerInteractEvent) event;
		if (!evt.hasBlock() || evt.getAction() == Action.LEFT_CLICK_BLOCK) {
			return;
		}

		Block clicked = evt.getClickedBlock();
		BlockState state = clicked.getState();
		MaterialData data = state.getData();
		Material seed;

		if (data.getItemType() == Material.CROPS && data instanceof Crops) {
			// Futureproofing: Currently, potatoes/carrots are not Crops
			// This should change, as they all extend BlockCrops
			Crops crops = (Crops) data;
			if (crops.getState() != CropState.RIPE) {
				return;
			}
			crops.setState(CropState.SEEDED);
			seed = Material.SEEDS;
		} else if (data instanceof CocoaPlant) {
			CocoaPlant cocoa = (CocoaPlant) data;
			if (cocoa.getSize() != CocoaPlantSize.LARGE) {
				return;
			}
			cocoa.setSize(CocoaPlantSize.SMALL);
			seed = Material.INK_SACK;
		} else if (data instanceof NetherWarts) {
			// Separate? Could be a neat Doom thing
			NetherWarts warts = (NetherWarts) data;
			if (warts.getState() != NetherWartsState.RIPE) {
				return;
			}
			warts.setState(NetherWartsState.SEEDED);
			seed = Material.NETHER_STALK;
		} else if (data.getItemType() == Material.POTATO) {
			if (data.getData() < 7) {
				return;
			}
			data.setData((byte) 0);
			seed = Material.POTATO_ITEM;
		} else if (data.getItemType() == Material.CARROT) {
			if (data.getData() < 7) {
				return;
			}
			data.setData((byte) 0);
			seed = Material.CARROT_ITEM;
		} else {
			return;
		}
		Player player = evt.getPlayer();
		boolean reseed = false;
		for (ItemStack drop : BlockDrops.getDrops(player, player.getItemInHand(), clicked)) {
			if (drop.getType() == seed && !reseed) {
				// Re-seed cost
				drop = InventoryUtils.decrement(drop, 1);
				reseed = true;
			}
			if (drop != null) {
				player.getWorld().dropItem(player.getLocation(), drop).setPickupDelay(0);
			}
		}
		if (!reseed) {
			Inventory inventory = player.getInventory();
			for (int i = 0; i < inventory.getSize(); i++) {
				ItemStack item = inventory.getItem(i);
				if (item == null || item.getType() != seed) {
					continue;
				}
				inventory.setItem(i, InventoryUtils.decrement(item, 1));
				reseed = true;
				break;
			}
		}
		clicked.getWorld().playSound(clicked.getLocation(), Sound.DIG_GRASS, 1, 1);
		clicked.getWorld().spigot().playEffect(clicked.getLocation().add(0.5, 0, 0.5), org.bukkit.Effect.TILE_BREAK,
				clicked.getTypeId(), clicked.getData(), 0.5F, 0.5F, 0.5F, 0, 10, 1);
		if (reseed) {
			state.setData(data);
			state.update(true);
		} else {
			clicked.setType(Material.AIR);
		}
	}

}
