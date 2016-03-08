package co.sblock.machines.type;

import java.util.Arrays;

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
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.machines.Machines;
import co.sblock.machines.type.computer.BadButton;
import co.sblock.machines.type.computer.BlockInventoryWrapper;
import co.sblock.machines.type.computer.GoodButton;
import co.sblock.machines.type.computer.Programs;
import co.sblock.machines.utilities.Shape;
import co.sblock.micromodules.Protections;
import co.sblock.micromodules.protectionhooks.ProtectionHook;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * power 20 * 1 second = 19 blocks up
 * 
 * @author Jikoo
 */
public class Elevator extends Machine {

	private final Protections protections;
	private final ItemStack drop;

	public Elevator(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Elevator");
		this.protections = plugin.getModule(Protections.class);
		drop = new ItemStack(Material.IRON_PLATE);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Elevator");
		drop.setItemMeta(meta);

		getShape().setVectorData(new Vector(0, 0, 0), new MaterialData(Material.PURPUR_PILLAR));
		getShape().setVectorData(new Vector(0, 1, 0), new MaterialData(Material.IRON_PLATE));
	}

	public int getCurrentBoost(ConfigurationSection storage) {
		return storage.getInt("duration");
	}

	public void adjustBlockBoost(ConfigurationSection storage, int difference) {
		int boost = getCurrentBoost(storage) + difference;
		if (boost < 0) {
			return;
		}
		if (boost > 50) {
			boost = 50;
		}
		storage.set("duration", boost * 20 / 19);
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		Player player = event.getPlayer();
		// Allow sneaking players to cross or place blocks, but don't allow elevators to trigger redstone devices.
		if (player.isSneaking()) {
			return event.getAction() != Action.PHYSICAL;
		}
		if (event.getAction() == Action.PHYSICAL) {
			event.getClickedBlock().getWorld().playSound(event.getClickedBlock().getLocation(),
					Sound.ENTITY_ENDERDRAGON_FLAP, 0.2F, 0F);
			// Effect power is 0-indexed.
			player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, storage.getInt("duration"),
					19, true), true);
			return true;
		}
		Location interacted = event.getClickedBlock().getLocation();
		for (ProtectionHook hook : protections.getHooks()) {
			if (!hook.canOpenChestsAt(player, interacted)) {
				player.sendMessage(Color.BAD + "You do not have permission to adjust elevators here!");
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
		return false;
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
