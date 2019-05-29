package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.machines.utilities.Shape.MaterialDataValue;
import com.easterlyn.utilities.InventoryUtils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

/**
 * Simulate a Sburb Cruxtender in Minecraft.
 *
 * @author Jikoo
 */
public class Cruxtruder extends Machine {

	private final ItemStack drop;

	public Cruxtruder(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Cruxtruder");
		Shape shape = getShape();
		MaterialDataValue m = new Shape.MaterialDataValue(Material.SEA_LANTERN);
		shape.setVectorData(new Vector(0, 0, 0), m);
		shape.setVectorData(new Vector(0, 1, 0), m);
		shape.setVectorData(new Vector(0, 0, -1),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.NORTH));
		shape.setVectorData(new Vector(1, 0, 0),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.WEST));
		shape.setVectorData(new Vector(-1, 0, 0),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.EAST));
		shape.setVectorData(new Vector(0, 0, 1),
				new Shape.MaterialDataValue(Material.QUARTZ_STAIRS).withBlockData(Directional.class, Direction.SOUTH));

		m = new Shape.MaterialDataValue(Material.QUARTZ_SLAB);
		shape.setVectorData(new Vector(1, 0, -1), m);
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 0, 1), m);

		drop = new ItemStack(Material.BEACON);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Cruxtruder");
			drop.setItemMeta(itemMeta);
		});
	}

	@Override
	public boolean handleBreak(BlockBreakEvent event, ConfigurationSection storage) {
		Location broken = event.getBlock().getLocation();
		if (getKey(storage).add(new Vector(0, 1, 0)).equals(broken)) {
			if (event.getBlock().getType() != Material.GLASS) {
				event.getBlock().setType(Material.GLASS);
			}
			if (broken.getWorld() != null) {
				broken.getWorld().dropItemNaturally(broken.add(0.5, 1, 0.5), Captcha.getBlankDowel());
			}
			return true;
		}
		return super.handleBreak(event, storage);
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
