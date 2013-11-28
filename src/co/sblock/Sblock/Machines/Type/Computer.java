package co.sblock.Sblock.Machines.Type;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.Sblock.Machines.MachineModule;
import co.sblock.Sblock.Machines.Type.Programs.Icon;
import co.sblock.Sblock.Machines.Type.Shape.Direction;
import co.sblock.Sblock.UserData.SblockUser;

/**
 * @author Jikoo
 */
public class Computer extends Machine implements InventoryHolder {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String)
	 */
	public Computer(Location l, String data) {
		super(l, data, Direction.NORTH);
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble()
	 */
	public void assemble(BlockPlaceEvent event) {
		if (MachineModule.getInstance().getManager().hasComputer(event.getPlayer())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You can only have one Computer!");
			event.getPlayer().setItemInHand(null);
		}
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(BlockBreakEvent)
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return event.getPlayer().getName().equals(getData());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getLocations()
	 */
	public Set<Location> getLocations() {
		return new HashSet<Location>();
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
		for (Icon i : Icon.values()) {
			if (event.getCurrentItem().equals(i.getIcon())) {
				i.execute((Player) event.getWhoClicked());
			}
		}
		event.setResult(Result.DENY);
		return false;
	}

	/**
	 * @see org.bukkit.inventory.InventoryHolder#getInventory()
	 */
	@Override
	public Inventory getInventory() {
		SblockUser u = SblockUser.getUser(getData());
		Inventory i = Bukkit.createInventory(this, 9, getData() + "'s Computer");
		for (int i1 : u.getPrograms()) {
			i.addItem(Icon.getIcon(i1).getIcon());
		}
		return i;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	public boolean handleInteract(PlayerInteractEvent event) {
		// Adam
		return true;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getFacingDirection()
	 */
	@Override
	public Direction getFacingDirection() {
		return Direction.NORTH;
	}
}
