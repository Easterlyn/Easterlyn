package co.sblock.Sblock.Machines.Type;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.Machines.SblockMachines;
import co.sblock.Sblock.UserData.SblockUser;

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
		this.blocks = new HashMap<Location, ItemStack>();
	}

	/**
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
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	public MachineType getType() {
		return MachineType.COMPUTER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	public boolean handleClick(InventoryClickEvent event) {
		if (!event.getWhoClicked().getName().equals(this.getData()) && !event.getWhoClicked().hasPermission("group.denizen")) {
			event.setResult(Result.DENY);
			return true;
		}
		if (event.getCurrentItem() == null) {
			event.setResult(Result.DENY);
			return true;
		}
		for (Icon ico : Icon.values()) {
			if (event.getCurrentItem().equals(ico.getIcon())) {
				switch (ico) {
				case PESTERCHUM:
					break;
				case SBURBCLIENT:
					event.getWhoClicked().openInventory(getClientInventory());
					break;
				case SBURBSERVER:
					event.getWhoClicked().openInventory(getVerify("SburbServer"));
					break;
				case BACK:
					// if upper inv = getServerInv remove server status
					event.getWhoClicked().openInventory(getInventory());
					break;
				case CONFIRM:
					
				default:
					break;
				}
				break;
			}
		}
		event.setResult(Result.DENY);
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	public boolean handleInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		if (!event.getPlayer().getName().equals(this.getData())) {
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
				SblockUser u = SblockUser.getUser(event.getPlayer().getName());
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
		SblockUser u = SblockUser.getUser(getData());
		Inventory i = Bukkit.createInventory(this, 9, getData() + "@sblock.co:~/");
		if (u == null) {
			return i;
		}
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
	 * Create an Inventory that represents our Sburb client adaptation.
	 * 
	 * @return the Inventory created
	 */
	private Inventory getVerify(String s) {
		Inventory i = Bukkit.createInventory(this, 9, "~/verify/" + s);
		i.addItem(Icon.CONFIRM.getIcon());
		i.setItem(i.getSize() - 1, Icon.BACK.getIcon());
		return i;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#postAssemble()
	 */
	@Override
	protected void postAssemble() {
	}
}
