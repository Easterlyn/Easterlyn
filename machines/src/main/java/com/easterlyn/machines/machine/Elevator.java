package com.easterlyn.machines.machine;

import com.easterlyn.machines.EasterlynMachines;
import com.easterlyn.machines.Machine;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.ProtectionUtil;
import com.easterlyn.util.Shape;
import com.easterlyn.util.inventory.Button;
import com.easterlyn.util.inventory.SimpleUI;
import java.util.Arrays;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * power 20 * 1 second = 19 blocks up
 *
 * @author Jikoo
 */
public class Elevator extends Machine {

	private final ItemStack drop;

	public Elevator(EasterlynMachines machines) {
		super(machines, new Shape(), "Elevator");

		getShape().setVectorData(new Vector(0, 0, 0), Material.PURPUR_PILLAR);
		getShape().setVectorData(new Vector(0, 1, 0), Material.HEAVY_WEIGHTED_PRESSURE_PLATE);

		drop = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		GenericUtil.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Elevator");
			drop.setItemMeta(itemMeta);
		});
	}

	private int getCurrentBoost(ConfigurationSection storage) {
		return storage.getInt("duration", 1);
	}

	private int adjustBlockBoost(ConfigurationSection storage, int difference) {
		int boost = getCurrentBoost(storage) + difference;
		if (boost < 1) {
			return 1;
		}
		if (boost > 50) {
			return 50;
		}
		storage.set("duration", boost);
		return boost;
	}

	@Override
	public void handleInteract(@NotNull PlayerInteractEvent event, @NotNull ConfigurationSection storage) {
		super.handleInteract(event, storage);
		if (event.useInteractedBlock() == Event.Result.DENY) {
			return;
		}

		Player player = event.getPlayer();
		// Allow sneaking players to cross or place blocks
		if (player.isSneaking()) {
			return;
		}
		if (event.getClickedBlock() == null) {
			return;
		}
		if (event.getAction() == Action.PHYSICAL) {
			event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(),
					Sound.ENTITY_ENDER_DRAGON_FLAP, 0.2F, 0F);
			int duration = storage.getInt("duration");
			// Effect power is 0-indexed.
			player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 19, true), true);
			PermissionAttachment attachment = player.addAttachment(getMachines(), (int) (duration * 1.2));
			if (attachment != null) {
				attachment.setPermission("nocheatplus.checks.moving.creativefly", true);
			}
			return;
		}
		Location interacted = event.getClickedBlock().getLocation();
		if (!ProtectionUtil.canOpenChestsAt(player, interacted)) {
			player.sendMessage(ChatColor.RED + "You do not have permission to adjust elevators here!");
			return;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			event.getPlayer().openInventory(getInventory(storage));
		}
	}

	@Override
	public double getCost() {
		return 200;
	}

	@NotNull
	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

	@NotNull
	private Inventory getInventory(@NotNull ConfigurationSection storage) {
		SimpleUI ui = new SimpleUI(getMachines(), "Densificator Configuration");
		ItemStack itemStack1 = new ItemStack(Material.RED_WOOL);
		GenericUtil.consumeAs(ItemMeta.class, itemStack1.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Decrease Boost");
			itemStack1.setItemMeta(itemMeta);
		});
		ui.setButton(3, new Button(itemStack1, event -> {
			int amount = adjustBlockBoost(storage, -1);
			Button display = ui.getButton(4);
			if (display != null) {
				// Item is not cloned, this is fine.
				display.getItem().setAmount(amount);
			}
			ui.draw(event.getView().getTopInventory());
		}));
		ItemStack itemStack2 = new ItemStack(Material.ELYTRA);
		GenericUtil.consumeAs(ItemMeta.class, itemStack2.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Ticks of Boost");
			itemMeta.setLore(Arrays.asList(ChatColor.WHITE + "1 tick = 1/20 second", ChatColor.WHITE + "Roughly 1 block/tick"));
			itemStack2.setItemMeta(itemMeta);
		});
		ui.setButton(4, new Button(itemStack2, event -> {}));
		ItemStack itemStack3 = new ItemStack(Material.LIME_WOOL);
		GenericUtil.consumeAs(ItemMeta.class, itemStack3.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Increase Boost");
			itemStack3.setItemMeta(itemMeta);
		});
		ui.setButton(5, new Button(itemStack3, event -> {
			int amount = adjustBlockBoost(storage, 1);
			Button display = ui.getButton(4);
			if (display != null) {
				display.getItem().setAmount(amount);
			}
			ui.draw(event.getView().getTopInventory());

		}));
		return ui.getInventory();
	}

}
