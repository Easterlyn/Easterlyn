/**
 * 
 */
package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import co.sblock.Sblock.UserData.SblockUser;

/**
 * @author Jikoo
 *
 */
public class Computer extends Machine implements InventoryHolder {

	/**
	 * @param l
	 *  location placed
	 * @param data
	 * name of the player who placed the computer
	 */
	public Computer(Location l, String data) {
		super(l, data);
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#assemble()
	 */
	public void assemble(BlockPlaceEvent event) {
		// Machine is single block, nothing to do!
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(org.bukkit.event.block.BlockBreakEvent)
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return event.getPlayer().getName().equals(getData());
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#getLocations()
	 */
	public List<Location> getLocations() {
		return new ArrayList<Location>();
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	public MachineType getType() {
		return MachineType.COMPUTER;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(org.bukkit.event.inventory.InventoryClickEvent)
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

	/* (non-Javadoc)
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
}
