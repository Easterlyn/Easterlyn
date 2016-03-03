package co.sblock.machines.type;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.machines.Machines;
import co.sblock.machines.type.computer.BadButton;
import co.sblock.machines.type.computer.BlockInventoryWrapper;
import co.sblock.machines.type.computer.GoodButton;
import co.sblock.machines.type.computer.Programs;
import co.sblock.machines.utilities.Shape;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * power 20 * 1 second = 19 blocks up
 * 
 * @author Jikoo
 */
public class Elevator extends Machine {

	private final ItemStack drop;

	public Elevator(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Elevator");
		drop = new ItemStack(Material.IRON_PLATE);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Elevator");

		getShape().setVectorData(new Vector(0, 0, 0), new MaterialData(Material.PURPUR_PILLAR));
		getShape().setVectorData(new Vector(0, 1, 0), new MaterialData(Material.IRON_PLATE));
	}

	public int getCurrentBoostBlocks(ConfigurationSection storage) {
		// 20 ticks of power 20 levitation raises 19 blocks.
		return storage.getInt("duration") * 19 / 20;
	}

	public void adjustBlockBoost(ConfigurationSection storage, int difference) {
		int blocks = getCurrentBoostBlocks(storage) + difference;
		if (blocks < 0 || blocks > 100) {
			return;
		}
		storage.set("duration", blocks * 20 / 19 + 1);
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		// Allow sneaking players to cross or place blocks, but don't allow elevators to trigger redstone devices.
		if (event.getPlayer().isSneaking()) {
			return event.getAction() != Action.PHYSICAL;
		}
		if (event.getAction() == Action.PHYSICAL) {
			// Power of potion effects is 0-indexed.
			event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, storage.getInt("duration"), 19, true), true);
			return true;
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory inventory = ((Computer) getMachines().getMachineByName("Computer")).getInventory();
			inventory = new BlockInventoryWrapper(inventory, this.getKey(storage));
			inventory.setItem(3, ((GoodButton) Programs.getProgramByName("GoodButton")).getIconFor(ChatColor.GREEN + "Increase Boost"));
			ItemStack gauge = new ItemStack(Material.ELYTRA);
			ItemMeta meta = gauge.getItemMeta();
			meta.setDisplayName("Blocks of Boost");
			gauge.setAmount(getCurrentBoostBlocks(storage));
			inventory.setItem(4, gauge);
			inventory.setItem(5, ((BadButton) Programs.getProgramByName("BadButton")).getIconFor(ChatColor.RED + "Decrease Boost"));
			event.getPlayer().openInventory(inventory);
			InventoryUtils.changeWindowName(event.getPlayer(), "Elevator Configuration");
		}
		return false;
	}

	@Override
	public int getCost() {
		return 200;
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}

}
