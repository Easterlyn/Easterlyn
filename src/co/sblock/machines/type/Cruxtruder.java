package co.sblock.machines.type;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.users.OfflineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Users;
import co.sblock.utilities.captcha.CruxiteDowel;
import co.sblock.utilities.progression.Entry;

import net.md_5.bungee.api.ChatColor;

/**
 * Simulate a Sburb Cruxtender in Minecraft.
 * 
 * @author Jikoo
 */
public class Cruxtruder extends Machine {

	private final ItemStack drop;

	public Cruxtruder() {
		super(new Shape());
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.SEA_LANTERN);
		shape.setVectorData(new Vector(0, 0, 0), m);
		shape.setVectorData(new Vector(0, 1, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.NORTH, "stair");
		shape.setVectorData(new Vector(1, 0, -1), m);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.WEST, "stair");
		shape.setVectorData(new Vector(1, 0, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.EAST, "stair");
		shape.setVectorData(new Vector(-1, 0, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.SOUTH, "stair");
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(0, 0, 1), m);
		shape.setVectorData(new Vector(-1, 0, 1), m);

		drop = new ItemStack(Material.BEACON);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Cruxtruder");
		drop.setItemMeta(meta);
	}

	@Override
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		if (getKey(storage).add(new Vector(0, 1, 0)).equals(event.getBlock().getLocation())) {
			OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
			if (Entry.getEntry().canStart(user)) {
				Entry.getEntry().startEntry(user, event.getBlock().getLocation());
			}
			if (user.getProgression() != ProgressionState.NONE || Entry.getEntry().isEntering(user)) {
				event.getBlock().setType(Material.GLASS);
			} else {
				return true;
			}
			event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), CruxiteDowel.getDowel());
		} else {
			super.handleBreak(event, storage);
		}
		return true;
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		return false;
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
