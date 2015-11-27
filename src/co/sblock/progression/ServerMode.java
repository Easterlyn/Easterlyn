package co.sblock.progression;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Machine;
import co.sblock.module.Module;
import co.sblock.users.User;

/**
 * 
 * 
 * @author Jikoo
 */
public class ServerMode extends Module {

	private final Map<Material, Integer> approved;

	private Machines machines;

	public ServerMode(Sblock plugin) {
		super(plugin);
		this.approved = new HashMap<Material, Integer>();
		this.fillApprovedSet();
	}

	@Override
	protected void onEnable() {
		this.machines = getPlugin().getModule(Machines.class);
	}

	@Override
	protected void onDisable() { }

	private void fillApprovedSet() {
		approved.put(Material.STONE, 6);
		approved.put(Material.GRASS, 0);
		approved.put(Material.DIRT, 1);
		approved.put(Material.COBBLESTONE, 0);
		approved.put(Material.WOOD, 5);
		approved.put(Material.LOG, 3);
		approved.put(Material.LOG_2, 1);
		approved.put(Material.GLASS, 0);
		approved.put(Material.SANDSTONE, 2);
		approved.put(Material.RED_SANDSTONE, 2);
		approved.put(Material.WOOL, 15);
		approved.put(Material.STEP, 7);
		approved.put(Material.BRICK, 0);
		approved.put(Material.SNOW_BLOCK, 0);
		approved.put(Material.NETHERRACK, 0);
		approved.put(Material.STAINED_GLASS, 15);
		approved.put(Material.SMOOTH_BRICK, 0);
		approved.put(Material.NETHER_BRICK, 0);
		approved.put(Material.ENDER_STONE, 0);
		approved.put(Material.QUARTZ_BLOCK, 2);
		approved.put(Material.STAINED_CLAY, 15);
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

	public boolean isWithinRange(User server, Block broken) {
		Pair<Machine, ConfigurationSection> pair = machines.getComputer(server.getClient());
		return pair != null && pair.getLeft().getKey(pair.getRight()).distanceSquared(broken.getLocation()) <= 625;
	}

	@Override
	public String getName() {
		return "ServerMode";
	}
}
