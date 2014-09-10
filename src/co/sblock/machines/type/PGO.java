package co.sblock.machines.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.MaterialData;

import co.sblock.machines.utilities.MachineType;

/**
 * Perfectly Generic Object Machine. Mimics most objects when placed against them.
 * 
 * @author Jikoo
 */
public class PGO extends Machine {

	/**
	 * @see co.sblock.Machines.Type.Machine#Machine(Location, String)
	 */
	public PGO(Location l, String data) {
		super(l, data);
		this.blocks = shape.getBuildLocations(direction);
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#getType()
	 */
	public MachineType getType() {
		return MachineType.PERFECTLY_GENERIC_OBJECT;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#assemble(BlockPlaceEvent)
	 */
	@SuppressWarnings("deprecation")
	public void assemble(BlockPlaceEvent event) {
		Material placedOn = event.getBlockAgainst().getType();
		if (isValid(placedOn)) {
			this.blocks.put(key, new MaterialData(event.getBlockAgainst().getType(), event.getBlockAgainst().getData()));
		}
		// Future features: Make wall signs etc. valid and copy text
		this.assemble();
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#meetsAdditionalBreakConditions(BlockPlaceEvent)
	 */
	public boolean meetsAdditionalBreakConditions(BlockBreakEvent event) {
		return true;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	public boolean handleInteract(PlayerInteractEvent event) {
		return false;
	}

	/**
	 * Verifies that a PGO is allowed to be turned into the specified Material.
	 * Holy poopsicles
	 * 
	 * @param type Material
	 * 
	 * @return true if Material can be mimicked
	 */
	private boolean isValid(Material type) {
		switch (type) {
		case ACACIA_STAIRS:
		case BIRCH_WOOD_STAIRS:
		case BOOKSHELF:
		case BRICK:
		case BRICK_STAIRS:
		case CACTUS:
		case CAKE_BLOCK:
		case CAULDRON:
		case COAL_BLOCK:
		case COAL_ORE:
		case COBBLESTONE:
		case COBBLESTONE_STAIRS:
		case COBBLE_WALL:
		case DARK_OAK_STAIRS:
		case DIAMOND_BLOCK:
		case DIAMOND_ORE:
		case DIRT:
		case DOUBLE_STEP:
		case EMERALD_BLOCK:
		case EMERALD_ORE:
		case ENCHANTMENT_TABLE:
		case ENDER_STONE:
		case FENCE:
		case FENCE_GATE:
		case GLASS:
		case GLOWING_REDSTONE_ORE:
		case GLOWSTONE:
		case GOLD_BLOCK:
		case GOLD_ORE:
		case GRASS:
//		case GRAVEL:
		case HARD_CLAY:
		case HAY_BLOCK:
		case HUGE_MUSHROOM_1:
		case HUGE_MUSHROOM_2:
		case ICE:
		case IRON_BLOCK:
		case IRON_FENCE:
		case IRON_ORE:
		case JACK_O_LANTERN:
		case JUKEBOX:
		case JUNGLE_WOOD_STAIRS:
		case LAPIS_BLOCK:
		case LAPIS_ORE:
		case LEAVES:
		case LEAVES_2:
		case LOG:
		case LOG_2:
		case MELON_BLOCK:
		case MOSSY_COBBLESTONE:
		case MYCEL:
		case NETHERRACK:
		case NETHER_BRICK:
		case NETHER_BRICK_STAIRS:
		case NETHER_FENCE:
		case NOTE_BLOCK:
		case OBSIDIAN:
		case PACKED_ICE:
		case PUMPKIN:
		case QUARTZ_BLOCK:
		case QUARTZ_ORE:
		case QUARTZ_STAIRS:
		case REDSTONE_BLOCK:
		case REDSTONE_ORE:
//		case SAND:
		case SANDSTONE:
		case SANDSTONE_STAIRS:
		case SMOOTH_BRICK:
		case SMOOTH_STAIRS:
		case SNOW_BLOCK:
		case SOUL_SAND:
		case SPONGE:
		case SPRUCE_WOOD_STAIRS:
		case STAINED_CLAY:
		case STAINED_GLASS:
		case STAINED_GLASS_PANE:
		case STEP:
		case STONE:
		case THIN_GLASS:
		case TNT:
		case TRIPWIRE:
		case WEB:
		case WOOD:
		case WOOD_DOUBLE_STEP:
		case WOOD_STAIRS:
		case WOOD_STEP:
		case WOOL:
		case WORKBENCH:
			return true;
		default:
			return false;
		}
	}

	@Override
	public MachineSerialiser getSerialiser() {
		return new MachineSerialiser(key, owner, direction, data, MachineType.PERFECTLY_GENERIC_OBJECT);
	}
}
