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
import co.sblock.machines.utilities.Icon;
import co.sblock.machines.utilities.Shape;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
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

	Computer() {
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
		for (Icon ico : Icon.values()) {
			if (event.getCurrentItem().equals(ico.getIcon())) {
				switch (ico) {
				case BACK:
					event.getWhoClicked().openInventory(getInventory(Users.getGuaranteedUser(event.getWhoClicked().getUniqueId())));
					break;
				case BOONDOLLAR_SHOP:
					// Keiko, shop name is all you, set to LOHACSE for now
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bossshop open LOHACSE " + event.getWhoClicked().getName());
					break;
				case SBURBCLIENT:
					// if gamestate != none
				case PESTERCHUM:
					break;
				case SBURBSERVER:
					event.getWhoClicked().openInventory(getServerConfirmation());
					break;
				case CONFIRM:
					OfflineUser offUser = Users.getGuaranteedUser(event.getWhoClicked().getUniqueId());
					if (!(offUser instanceof OnlineUser)) {
						((Player) event.getWhoClicked()).sendMessage(
								Color.BAD + "Your data appears to not have loaded properly. Please relog.");
						break;
					}
					OnlineUser onUser = (OnlineUser) offUser;
					// All checks for starting server mode handled inside startServerMode()
					if (onUser.isServer()) {
						onUser.stopServerMode();
					} else {
						onUser.startServerMode();
					}
				default:
					break;
				}
				break;
			}
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
			Icon ico = Icon.getIcon(event.getItem());
			if (ico != null) {
				event.getPlayer().sendMessage(Color.GOOD + "Installed "
						+ event.getItem().getItemMeta().getDisplayName() + Color.GOOD + "!");
				if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
					event.getPlayer().setItemInHand(InventoryUtils.decrement(event.getPlayer().getItemInHand(), 1));
				}
				OfflineUser u = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
				u.addProgram(ico.getProgramID());
				return true;
			}
		}
		if (event.getPlayer().isSneaking()) {
			return false;
		}
		event.getPlayer().openInventory(getInventory(Users.getGuaranteedUser(event.getPlayer().getUniqueId())));
		return true;
	}

	@Override
	public Inventory getInventory() {
		return null;
	}

	public Inventory getInventory(OfflineUser user) {
		Inventory i = Bukkit.createInventory(this, 9, user.getPlayerName() + "@sblock.co:~/");
		for (int i1 : user.getPrograms()) {
			i.addItem(Icon.getIcon(i1).getIcon());
		}
		if (i.firstEmpty() == 9) {
			user.getPlayer().sendMessage(Color.BAD + "You do not have any programs installed!");
		}
		return i;
	}

	/**
	 * Create a confirmation screen prior to entering server mode.
	 * 
	 * @return the Inventory created
	 */
	private Inventory getServerConfirmation() {
		Inventory i = Bukkit.createInventory(this, 9, "~/Verify?initialize=SburbServer");
		i.setItem(0, Icon.CONFIRM.getIcon());
		i.setItem(i.getSize() - 1, Icon.BACK.getIcon());
		return i;
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
