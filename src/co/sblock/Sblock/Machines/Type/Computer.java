package co.sblock.Sblock.Machines.Type;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.UserData.User;

/**
 * Computers for players! Inventory-based selection system.
 * 
 * @author Jikoo
 */
public class Computer extends Machine implements InventoryHolder {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String)
	 */
	public Computer(Location l, String data) {
		super(l, data);
		this.blocks = shape.getBuildLocations(d);
	}

	/**
	 * Players can only have one computer, and servers cannot place them for the client.
	 * 
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble()
	 */
	@Override
	public void assemble(BlockPlaceEvent event) {
		if (SblockMachines.getMachines().getManager().hasComputer(event.getPlayer(), l)) {
			if (event.getPlayer().hasPermission("group.horrorterror")) {
				event.getPlayer().sendMessage("Bypassing Computer cap. You devilish admin you.");
				return;
			}
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You can only have one Computer placed!");
			SblockMachines.getMachines().getManager().removeMachineListing(l);
		}
	}

	/**
	 * Servers cannot break client's computer.
	 * 
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(BlockBreakEvent)
	 */
	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return getData().equals(event.getPlayer().getName()) || event.getPlayer().hasPermission("group.denizen");
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	public MachineType getType() {
		return MachineType.COMPUTER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	public boolean handleClick(InventoryClickEvent event) {
		if (!event.getWhoClicked().getName().equals(this.getData())
				&& !event.getWhoClicked().hasPermission("group.denizen")) {
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
					event.getWhoClicked().openInventory(getInventory());
					break;
				case BOONDOLLAR_SHOP:
					// Keiko, shop name is all you,t set to LOHACSE for now
					Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bossshop open LOHACSE " + event.getWhoClicked().getName());
					break;
				case PESTERCHUM:
					break;
				case SBURBCLIENT:
					event.getWhoClicked().openInventory(getClientInventory());
					break;
				case SBURBSERVER:
					event.getWhoClicked().openInventory(getServerConfirmation());
					break;
				case CONFIRM:
					User u = User.getUser(event.getWhoClicked().getUniqueId());
					if (u == null) {
						((Player) event.getWhoClicked()).sendMessage(
								ChatColor.RED + "Your data appears to not have loaded properly. Please relog.");
						break;
					}
					// All checks for starting server mode handled inside startServerMode()
					if (u.isServer()) {
						u.stopServerMode();
					} else {
						u.startServerMode();
					}
				default:
					break;
				}
				break;
			}
		}
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	public boolean handleInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		if (!event.getPlayer().getUniqueId().toString().equals(this.getData())) {
			if (event.getPlayer().hasPermission("group.denizen")) {
				event.getPlayer().sendMessage("Allowing admin override for interaction with Computer.");
			} else {
				return true;
			}
		}
		if (event.getMaterial().name().contains("RECORD")) { // prevent non-program Icons from being registered
			Icon ico = Icon.getIcon(event.getItem());
			if (ico != null) {
				event.getPlayer().sendMessage(ChatColor.GREEN + "Installed "
						+ event.getItem().getItemMeta().getDisplayName() + ChatColor.GREEN + "!");
				event.setCancelled(true);
				event.getPlayer().setItemInHand(null);
				User u = User.getUser(event.getPlayer().getUniqueId());
				u.addProgram(ico.getProgramID());
				return true;
			} else {
				event.getPlayer().openInventory(getInventory());
			}
		}
		event.getPlayer().openInventory(getInventory());
		return true;
	}

	/**
	 * @see org.bukkit.inventory.InventoryHolder#getInventory()
	 */
	@Override
	public Inventory getInventory() {
		User u = User.getUser(UUID.fromString(getData()));
		Inventory i = Bukkit.createInventory(this, 9, u.getPlayerName() + "@sblock.co:~/");
		for (int i1 : u.getPrograms()) {
			i.addItem(Icon.getIcon(i1).getIcon());
		}
		if (i.firstEmpty() == 9) {
			u.getPlayer().sendMessage(ChatColor.RED + "You do not have any programs installed!");
		}
		return i;
	}

	/**
	 * Create an Inventory that represents our Sburb client adaptation.
	 * 
	 * @return the Inventory created
	 */
	private Inventory getClientInventory() {
		Inventory i = Bukkit.createInventory(this, 18, "~/SburbClient");
		i.setItem(i.getSize() - 1, Icon.BACK.getIcon());
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
}
