package co.sblock.machines.type;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Shape;

import net.md_5.bungee.api.ChatColor;

/**
 * Perfectly Generic Object Machine. Mimics most objects when placed against them.
 * 
 * @author Jikoo
 */
public class PGO extends Machine {

	private final ItemStack drop;

	public PGO(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Perfectly Generic Object");
		drop = new ItemStack(Material.DIRT);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Perfectly Generic Object");
		drop.setItemMeta(meta);

		getShape().setVectorData(new Vector(0, 0, 0), drop.getData());
	}

	@Override
	public int getCost() {
		return Integer.MAX_VALUE;
	}

	@Override
	public void assemble(BlockPlaceEvent event, ConfigurationSection storage) {
		MaterialData data = event.getBlockAgainst().getState().getData();
		if (!isValid(data.getItemType())) {
			data = drop.getData();
		}
		// Future features: Make wall signs etc. valid and copy text!location.getBlock().isEmpty()
		if (getMachines().isExploded(event.getBlock())) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Language.getColor("bad") + "There isn't enough space to build this Machine here.");
			this.assemblyFailed(storage);
			return;
		}
		this.assembleKeyLater(event.getBlock().getLocation(), data, storage);
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		return false;
	}

	/**
	 * Verifies that a PGO is allowed to be turned into the specified Material.
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
		case END_BRICKS:
		case FENCE:
		case FENCE_GATE:
		case GLASS:
		case GLOWING_REDSTONE_ORE:
		case GOLD_BLOCK:
		case GOLD_ORE:
		case GRASS:
		case GRASS_PATH:
		case HARD_CLAY:
		case HAY_BLOCK:
		case HUGE_MUSHROOM_1:
		case HUGE_MUSHROOM_2:
		case ICE:
		case IRON_BLOCK:
		case IRON_FENCE:
		case IRON_ORE:
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
		case OBSIDIAN:
		case PACKED_ICE:
		case PRISMARINE:
		case PUMPKIN:
		case PURPUR_BLOCK:
		case PURPUR_DOUBLE_SLAB:
		case PURPUR_PILLAR:
		case PURPUR_SLAB:
		case PURPUR_STAIRS:
		case QUARTZ_BLOCK:
		case QUARTZ_ORE:
		case QUARTZ_STAIRS:
		case RED_SANDSTONE:
		case RED_SANDSTONE_STAIRS:
		case REDSTONE_BLOCK:
		case REDSTONE_ORE:
		case SANDSTONE:
		case SANDSTONE_STAIRS:
		case SMOOTH_BRICK:
		case SMOOTH_STAIRS:
		case SNOW_BLOCK:
		case SOUL_SAND:
		case SPRUCE_WOOD_STAIRS:
		case STAINED_CLAY:
		case STAINED_GLASS:
		case STAINED_GLASS_PANE:
		case STEP:
		case STONE:
		case STONE_SLAB2:
		case THIN_GLASS:
		case TNT:
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
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
