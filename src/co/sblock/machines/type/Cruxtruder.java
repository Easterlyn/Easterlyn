package co.sblock.machines.type;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.captcha.CruxiteDowel;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.progression.Entry;
import co.sblock.users.User;
import co.sblock.users.ProgressionState;

import net.md_5.bungee.api.ChatColor;

/**
 * Simulate a Sburb Cruxtender in Minecraft.
 * 
 * @author Jikoo
 */
public class Cruxtruder extends Machine {

	private final Entry entry;
	private final ItemStack drop;

	public Cruxtruder(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape());
		this.entry = plugin.getModule(Entry.class);
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
		Location broken = event.getBlock().getLocation();
		if (getKey(storage).add(new Vector(0, 1, 0)).equals(broken)) {
			User user = getUsers().getUser(event.getPlayer().getUniqueId());
			if (entry.canStart(user)) {
				entry.startEntry(user, event.getBlock().getLocation());
			}
			if (user.getProgression() != ProgressionState.NONE || entry.isEntering(user)) {
				event.getBlock().setType(Material.GLASS);
			} else {
				return true;
			}
			broken.getWorld().dropItemNaturally(broken.add(0.5, 1, 0.5), CruxiteDowel.getDowel());
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
