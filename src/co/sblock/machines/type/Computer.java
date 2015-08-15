package co.sblock.machines.type;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.Color;
import co.sblock.machines.Machines;
import co.sblock.machines.type.computer.Program;
import co.sblock.machines.type.computer.Programs;
import co.sblock.machines.utilities.Shape;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.inventory.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Computers for players! Inventory-based selection system.
 * 
 * @author Jikoo
 */
public class Computer extends Machine implements InventoryHolder {

	private final ItemStack drop;

	public Computer() {
		super(new Shape());

		drop = new ItemStack(Material.JUKEBOX);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Computer");
		drop.setItemMeta(meta);
	}

	/**
	 * Players can only have one computer, and servers cannot place them for the client.
	 * 
	 * @see co.sblock.machines.type.Machine#assemble()
	 */
	@Override
	public void assemble(BlockPlaceEvent event, ConfigurationSection storage) {
		if (Machines.getInstance().hasComputer(event.getPlayer(), getKey(storage))) {
			if (event.getPlayer().hasPermission("sblock.horrorterror")) {
				event.getPlayer().sendMessage("Bypassing Computer cap. You devilish admin you.");
				return;
			}
			event.setCancelled(true);
			event.getBlock().setType(Material.AIR);
			event.getPlayer().sendMessage(Color.BAD + "You can only have one Computer placed!");
			this.assemblyFailed(storage);
			return;
		}
		super.assemble(event, storage);
	}

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		if (!event.getWhoClicked().getUniqueId().equals(getOwner(storage))
				&& !event.getWhoClicked().hasPermission("sblock.denizen")) {
			event.setResult(Result.DENY);
			return true;
		}
		if (event.getCurrentItem() == null) {
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
		if (!event.getPlayer().getUniqueId().equals(getOwner(storage))) {
			if (event.getPlayer().hasPermission("sblock.denizen")) {
				event.getPlayer().sendMessage("Allowing admin override for interaction with Computer.");
			} else {
				return true;
			}
		}
		if (event.getMaterial().name().contains("RECORD")) { // prevent non-program Icons from being registered
			event.setCancelled(true);
			Program program = Programs.getProgramByInstaller(event.getItem());
			if (program != null) {
				event.getPlayer().sendMessage(Color.GOOD + "Installed "
						+ event.getItem().getItemMeta().getDisplayName() + Color.GOOD + "!");
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
					event.getPlayer().setItemInHand(InventoryUtils.decrement(event.getPlayer().getItemInHand(), 1));
				}
				OfflineUser u = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
				u.addProgram(program.getName());
				return true;
			}
		}
		if (event.getPlayer().isSneaking()) {
			return false;
		}
		openInventory(event.getPlayer());
		return true;
	}

	@Override
	public Inventory getInventory() {
		return Bukkit.createInventory(this, 9, "Computer");
	}

	public void openInventory(Player player) {
		Inventory inventory = getInventory();
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		for (String id : user.getPrograms()) {
			inventory.addItem(Programs.getProgramByName(id).getIcon());
		}
		if (inventory.firstEmpty() == 0) {
			user.getPlayer().sendMessage(Color.BAD + "You do not have any programs installed!");
			return;
		}
		player.openInventory(inventory);
		InventoryUtils.changeWindowName(player, player.getName() + "@sblock.co:~/");
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
