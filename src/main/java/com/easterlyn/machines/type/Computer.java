package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.computer.PowerManager;
import com.easterlyn.machines.type.computer.Program;
import com.easterlyn.machines.type.computer.Programs;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.player.PermissionUtils;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import net.md_5.bungee.api.ChatColor;

/**
 * Computers for players! Inventory-based selection system.
 *
 * @author Jikoo
 */
public class Computer extends Machine implements InventoryHolder {

	private final ItemStack drop;

	public Computer(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Computer");

		drop = new ItemStack(Material.JUKEBOX);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Computer");
		drop.setItemMeta(meta);

		getShape().setVectorData(new Vector(0, 0, 0), drop.getType());

		PermissionUtils.addParent("easterlyn.machines.administrate", UserRank.MOD.getPermission());
	}

	/**
	 * Handles Machine deconstruction.
	 *
	 * @param event the BlockBreakEvent
	 *
	 * @return true if event should be cancelled
	 */
	@Override
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		if (!getOwner(storage).equals(event.getPlayer().getUniqueId())
				&& !event.getPlayer().hasPermission("easterlyn.machines.administrate")) {
			return true;
		}
		if (event.getPlayer().getGameMode() == GameMode.SURVIVAL) {
			Location key = getKey(storage);
			key.getWorld().dropItemNaturally(key.add(0.5, 0, 0.5), getUniqueDrop());
		}
		remove(storage);
		return true;
	}

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
			event.setResult(Result.DENY);
			return true;
		}
		event.setResult(Result.DENY);
		Program program = Programs.getProgramByIcon(event.getCurrentItem());
		if (program != null) {
			program.execute((Player) event.getWhoClicked(), event.getCurrentItem());
		}
		return true;
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		if (super.handleInteract(event, storage)) {
			return true;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		if (event.getMaterial().name().contains("RECORD")) { // prevent non-program Icons from being registered
			event.setCancelled(true);
		}
		if (event.getPlayer().isSneaking()) {
			return event.isCancelled();
		}
		openInventory(event.getPlayer());
		return true;
	}

	@Override
	public Inventory getInventory() {
		return Bukkit.createInventory(this, 9, "Computer");
	}
	public Inventory getInventory(int size) {
		return Bukkit.createInventory(this, size, "Computer");
	}

	public void openInventory(Player player) {
		Inventory inventory = getInventory();
		for (Program program : Programs.getPrograms()) {
			if (program.isDefault() || program instanceof PowerManager && player.hasPermission(UserRank.DANGER_DANGER_HIGH_VOLTAGE.getPermission())) {
				inventory.addItem(program.getIcon());
			}
		}
		player.openInventory(inventory);
		InventoryUtils.changeWindowName(player, player.getName() + "@easterlyn.com:~/");
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
