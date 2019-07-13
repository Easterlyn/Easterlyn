package com.easterlyn.kitchensink.listener;

import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import com.easterlyn.util.BlockUtil;
import com.easterlyn.util.ExperienceUtil;
import com.easterlyn.util.inventory.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.ExpBottleEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;

public class BottleExperience implements Listener {

	private final String keyBottleCreate = "kitchensink:expBottleCreate";
	private final String keyBottleThrow = "kitchensink:expBottleThrow";

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (BlockUtil.hasRightClickFunction(event)) {
				return;
			}
		} else if (event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		if (event.getPlayer().isSneaking()) {
			return;
		}

		ItemStack held = ItemUtil.getHeldItem(event);

		if (held.getType() != Material.GLASS_BOTTLE) {
			return;
		}

		RegisteredServiceProvider<EasterlynCore> easterlynProvider = Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		Player player = event.getPlayer();
		User user = easterlynProvider.getProvider().getUserManager().getUser(player.getUniqueId());

		Object cooldown = user.getTemporaryStorage().get(keyBottleCreate);
		if (cooldown instanceof Long && (Long) cooldown >= System.currentTimeMillis()) {
			return;
		}

		int exp = ExperienceUtil.getExp(player);
		if (exp >= 11) {
			ExperienceUtil.changeExp(player, -11);
			ItemUtil.setHeldItem(player.getInventory(), event.getHand() == EquipmentSlot.HAND, ItemUtil.decrement(held, 1));
			player.getWorld().dropItem(player.getLocation(),
					new ItemStack(Material.EXPERIENCE_BOTTLE, 1)).setPickupDelay(0);
			user.getTemporaryStorage().put(keyBottleThrow, System.currentTimeMillis() + 2000);
			event.setUseItemInHand(Event.Result.DENY);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onExpBottle(ExpBottleEvent event) {
		event.setExperience(10);
		event.setShowEffect(false);
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		User user = easterlynProvider.getProvider().getUserManager().getUser(event.getPlayer().getUniqueId());
		user.getTemporaryStorage().put(keyBottleCreate, System.currentTimeMillis() + 2000);
	}

	@EventHandler(ignoreCancelled = true)
	public void onProjectileLaunch(ProjectileLaunchEvent event) {
		if (!(event.getEntity() instanceof ThrownExpBottle) || !(event.getEntity().getShooter() instanceof Player)) {
			return;
		}
		RegisteredServiceProvider<EasterlynCore> easterlynProvider = Bukkit.getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (easterlynProvider == null) {
			return;
		}

		User user = easterlynProvider.getProvider().getUserManager().getUser(((Player) event.getEntity().getShooter()).getUniqueId());

		Object cooldown = user.getTemporaryStorage().get(keyBottleThrow);
		if (cooldown instanceof Long && (Long) cooldown >= System.currentTimeMillis()) {
			event.setCancelled(true);
		}
	}

}
