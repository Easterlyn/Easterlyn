package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.computer.BadButton;
import com.easterlyn.machines.type.computer.BlockInventoryWrapper;
import com.easterlyn.machines.type.computer.GoodButton;
import com.easterlyn.machines.type.computer.Programs;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;
import com.easterlyn.utilities.InventoryUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.Arrays;

/**
 * power 20 * 1 second = 19 blocks up
 *
 * @author Jikoo
 */
public class Elevator extends Machine {

	private final Protections protections;
	private final ItemStack drop;

	public Elevator(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Elevator");
		this.protections = plugin.getModule(Protections.class);
		drop = new ItemStack(Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Elevator");
		drop.setItemMeta(meta);

		getShape().setVectorData(new Vector(0, 0, 0),Material.PURPUR_PILLAR);
		getShape().setVectorData(new Vector(0, 1, 0), Material.HEAVY_WEIGHTED_PRESSURE_PLATE);
	}

	public int getCurrentBoost(ConfigurationSection storage) {
		return storage.getInt("duration");
	}

	public int adjustBlockBoost(ConfigurationSection storage, int difference) {
		int boost = getCurrentBoost(storage) + difference;
		if (boost < 0) {
			return 0;
		}
		if (boost > 50) {
			boost = 50;
		}
		storage.set("duration", boost);
		return boost;
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		Player player = event.getPlayer();
		// Allow sneaking players to cross or place blocks, but don't allow elevators to trigger redstone devices.
		if (player.isSneaking()) {
			return event.getAction() == Action.PHYSICAL;
		}
		if (event.getAction() == Action.PHYSICAL) {
			event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(),
					Sound.ENTITY_ENDER_DRAGON_FLAP, 0.2F, 0F);
			int duration = storage.getInt("duration");
			// Effect power is 0-indexed.
			player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, duration, 19, true), true);
			PermissionAttachment attachment = player.addAttachment(getPlugin(), (int) (duration * 1.5));
			attachment.setPermission("nocheatplus.checks.moving.creativefly", true);
			return true;
		}
		Location interacted = event.getClickedBlock().getLocation();
		for (ProtectionHook hook : protections.getHooks()) {
			if (!hook.canOpenChestsAt(player, interacted)) {
				player.sendMessage(Language.getColor("bad") + "You do not have permission to adjust elevators here!");
				return true;
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory inventory = ((Computer) getMachines().getMachineByName("Computer")).getInventory();
			inventory = new BlockInventoryWrapper(inventory, this.getKey(storage));
			inventory.setItem(3, ((GoodButton) Programs.getProgramByName("GoodButton"))
					.getIconFor(ChatColor.GREEN + "Increase Boost"));
			ItemStack gauge = new ItemStack(Material.ELYTRA);
			ItemMeta meta = gauge.getItemMeta();
			meta.setDisplayName(ChatColor.GOLD + "Ticks of Boost");
			meta.setLore(Arrays.asList(ChatColor.WHITE + "1 tick = 1/20 second",
					ChatColor.WHITE + "Roughly, +1 block/tick"));
			gauge.setItemMeta(meta);
			gauge.setAmount(getCurrentBoost(storage));
			inventory.setItem(4, gauge);
			inventory.setItem(5, ((BadButton) Programs.getProgramByName("BadButton"))
					.getIconFor(ChatColor.RED + "Decrease Boost"));
			event.getPlayer().openInventory(inventory);
			InventoryUtils.changeWindowName(event.getPlayer(), "Elevator Configuration");
		}
		return true;
	}

	@Override
	public int getCost() {
		return 200;
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
