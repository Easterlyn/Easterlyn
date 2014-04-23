package co.sblock.machines.type;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

/**
 * 
 * 
 * @author Jikoo
 */
public class Bank extends Machine {

	/**
	 * @param l
	 * @param data
	 */
	@SuppressWarnings("deprecation")
	public Bank(Location l, String data) {
		super(l, data);
		data = "admin placed";
		shape.addBlock(new Vector(0, 0, 0), new MaterialData(Material.QUARTZ_BLOCK, (byte) 2));
		shape.addBlock(new Vector(0, 1, 0), new MaterialData(Material.STAINED_GLASS));
		shape.addBlock(new Vector(0, 2, 0), new MaterialData(Material.STEP, (byte) 7));
	}

	/* (non-Javadoc)
	 * @see co.sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.BANK;
	}

	/* (non-Javadoc)
	 * @see co.sblock.Machines.Type.Machine#handleInteract(org.bukkit.event.player.PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "bossshop open bank " + event.getPlayer().getName());
		return true;
	}

}
