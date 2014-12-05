package co.sblock.utilities.progression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import co.sblock.machines.SblockMachines;
import co.sblock.machines.type.Machine;
import co.sblock.users.OfflineUser;

/**
 * 
 * 
 * @author Jikoo
 */
public class ServerMode {

	private static ServerMode instance;

	private Map<Material, Integer> approved;

	public ServerMode() {
		createApprovedSet();
	}

	/**
	 * @return
	 */
	private void createApprovedSet() {
		approved = new HashMap<Material, Integer>();
		approved.put(Material.STONE, 0);
		approved.put(Material.GRASS, 0);
		approved.put(Material.DIRT, 1);
		approved.put(Material.COBBLESTONE, 0);
		approved.put(Material.WOOD, 5);
		approved.put(Material.SAND, 1);
		approved.put(Material.GRAVEL, 0);
		approved.put(Material.LOG, 3);
		approved.put(Material.LOG_2, 1);
		approved.put(Material.GLASS, 0);
		approved.put(Material.SANDSTONE, 1);
		approved.put(Material.WOOL, 15);
		approved.put(Material.STEP, 7);
		approved.put(Material.BRICK, 0);
		approved.put(Material.SNOW_BLOCK, 0);
		approved.put(Material.CLAY, 0);
		approved.put(Material.NETHERRACK, 0);
		approved.put(Material.STAINED_GLASS, 15);
		approved.put(Material.SMOOTH_BRICK, 0);
		approved.put(Material.NETHER_BRICK, 0);
		approved.put(Material.ENDER_STONE, 0);
		approved.put(Material.QUARTZ_BLOCK, 2);
		approved.put(Material.HARD_CLAY, 0);
		approved.put(Material.STAINED_CLAY, 15);
		approved.put(Material.LEAVES, 3);
		approved.put(Material.LEAVES_2, 1);
		approved.put(Material.WORKBENCH, 0);
		approved.put(Material.CHEST, 0);
		approved.put(Material.FURNACE, 0);
		approved.put(Material.IRON_FENCE, 0);
		approved.put(Material.THIN_GLASS, 0);
		approved.put(Material.STAINED_GLASS_PANE, 15);
		approved.put(Material.CARPET, 15);
		approved.put(Material.NOTE_BLOCK, 0);
		approved.put(Material.WOOD_DOOR, 0);
	}

	public boolean isApproved(Material m) {
		return approved.containsKey(m);
	}

	public Set<Material> getApprovedSet() {
		return approved.keySet();
	}

	public ItemStack cycleData(ItemStack is) {
		if (!approved.containsKey(is.getType())) {
			return is;
		}
		int current = is.getDurability();
		int max = approved.get(is.getType());
		if (current < max) {
			is.setDurability((short) (current + 1));
		} else {
			is.setDurability((short) 0);
		}
		return is;
	}

	public boolean isWithinRange(OfflineUser server, Block broken) {
		Machine computer = SblockMachines.getInstance().getComputer(server.getClient());
		return computer != null && computer.getKey().distanceSquared(broken.getLocation()) <= 625;
	}

	public static ServerMode getInstance() {
		if (instance == null) {
			instance = new ServerMode();
		}
		return instance;
	}
}
