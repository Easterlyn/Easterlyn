package co.sblock.machines.type;

import java.util.HashMap;

import org.bukkit.Location;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

import co.sblock.machines.utilities.MachineType;

/**
 * For Zack, with love.
 * 
 * @author Jikoo
 */
public class PBO extends Machine {

	/**
	 * @see co.sblock.Machines.Type.Machine#Machine(Location, String)
	 */
	public PBO(Location l, String data) {
		super(l, data);
		this.blocks = new HashMap<Location, MaterialData>();
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(BlockBreakEvent)
	 */
	@Override
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return true;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#assemble(BlockPlaceEvent)
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void assemble(BlockPlaceEvent event) {
		event.getBlockPlaced().setTypeIdAndData(event.getBlockAgainst().getTypeId(),
				event.getBlockAgainst().getData(), false);
		event.setCancelled(true);
		event.getPlayer().updateInventory();
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.PERFECT_BUILDING_OBJECT;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		return false;
	}
}
