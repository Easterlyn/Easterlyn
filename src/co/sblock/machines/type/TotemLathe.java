package co.sblock.machines.type;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.captcha.CruxiteDowel;
import co.sblock.machines.MachineInventoryTracker;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.progression.Entry;
import co.sblock.users.User;
import co.sblock.users.ProgressionState;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Simulates a Totem Lathe from Sburb.
 * 
 * @author Dublek, Jikoo
 */
public class TotemLathe extends Machine	{

	private final Entry entry;
	private final MachineInventoryTracker tracker;
	private final ItemStack drop;

	public TotemLathe(Sblock plugin, Machines machines) {
		super(plugin, machines, new Shape());
		entry = plugin.getModule(Entry.class);
		tracker = machines.getInventoryTracker();
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK, (byte) 2);
		shape.setVectorData(new Vector(0, 0, 0), m);
		shape.setVectorData(new Vector(0, 1, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK, (byte) 1);
		shape.setVectorData(new Vector(0, 2, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.WEST, "upperstair");
		shape.setVectorData(new Vector(1, 0, 0), m);
		shape.setVectorData(new Vector(1, 2, 0), m);
		m = shape.new MaterialDataValue(Material.STEP, (byte) 7);
		shape.setVectorData(new Vector(0, 3, 0), m);
		shape.setVectorData(new Vector(1, 3, 0), m);
		shape.setVectorData(new Vector(2, 3, 0), m);
		m = shape.new MaterialDataValue(Material.STEP, (byte) 15);
		shape.setVectorData(new Vector(2, 0, 0), m);
		shape.setVectorData(new Vector(3, 0, 0), m);
		m = shape.new MaterialDataValue(Material.DAYLIGHT_DETECTOR);
		shape.setVectorData(new Vector(1, 1, 0), m);
		m = shape.new MaterialDataValue(Material.ANVIL, Direction.NORTH, "anvil");
		shape.setVectorData(new Vector(3, 1, 0), m);
		m = shape.new MaterialDataValue(Material.HOPPER);
		shape.setVectorData(new Vector(2, 2, 0), m);

		drop = new ItemStack(Material.ANVIL);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Totem Lathe");
		drop.setItemMeta(meta);
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		if (super.handleInteract(event, storage)) {
			return true;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		if (event.getPlayer().isSneaking()) {
			return false;
		}
		User user = getUsers().getUser(event.getPlayer().getUniqueId());
		if (user != null && (user.getProgression() != ProgressionState.NONE || entry.isEntering(user))) {
			openInventory(event.getPlayer(), storage);
		}
		return true;
	}

	/**
	 * Open a Totem Lathe inventory for a Player.
	 * 
	 * @param player the Player
	 */
	public void openInventory(Player player, ConfigurationSection storage) {
		tracker.openVillagerInventory(player, this, getKey(storage));
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		if (event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
			// Clicked inv is not the top.
			return false;
		}
		if (event.getSlot() != 2 || event.getCurrentItem() == null
				|| event.getCurrentItem().getType() == Material.AIR) {
			// No result
			return false;
		}
		// Item is being crafted
		Inventory top = event.getView().getTopInventory();
		Player player = (Player) event.getWhoClicked();
		int decrement;
		if (event.getClick().name().contains("SHIFT")) {
			// This is not a good way to handle shift clicks for normal crafting, but in
			// this case the result is guaranteed to be a single item that stacks to 64.
			decrement = Math.min(top.getItem(0).getAmount(), top.getItem(1).getAmount());
			ItemStack add = event.getCurrentItem().clone();
			add.setAmount(decrement);
			if (InventoryUtils.hasSpaceFor(add, player.getInventory())) {
				player.getInventory().addItem(add);
			} else {
				return true;
			}
		} else if (event.getCursor() == null
				|| event.getCursor().getType() == Material.AIR
				|| (event.getCursor().isSimilar(event.getCurrentItem())
						&& event.getCursor().getAmount() + event.getCurrentItem().getAmount()
						<= event.getCursor().getMaxStackSize())) {
			decrement = 1;
			ItemStack result = event.getCurrentItem().clone();
			if (result.isSimilar(event.getCursor())) {
				result.setAmount(result.getAmount() + event.getCursor().getAmount());
			}
			event.setCursor(result);
		} else {
			return true;
		}
		event.setCurrentItem(null);
		top.setItem(0, InventoryUtils.decrement(top.getItem(0), decrement));
		top.setItem(1, InventoryUtils.decrement(top.getItem(1), decrement));
		player.updateInventory();
		return true;
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 * 
	 * @param name the name of the player who is using the Totem Lathe
	 */
	public void updateInventory(final UUID id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
			@Override
			public void run() {
				// Must re-obtain player or update doesn't seem to happen
				Player player = Bukkit.getPlayer(id);
				if (player == null || !tracker.hasMachineOpen(player)) {
					// Player has logged out or closed inventory. Inventories are per-player, ignore.
					return;
				}

				Inventory open = player.getOpenInventory().getTopInventory();
				ItemStack card = null;
				ItemStack result = null;
				if (CruxiteDowel.isBlankDowel(open.getItem(0))) {
					card = open.getItem(1);
				} else if (CruxiteDowel.isBlankDowel(open.getItem(1))) {
					card = open.getItem(0);
				}
				if (Captcha.isPunch(card)) {
					result = CruxiteDowel.carve(card);
				}
				// Set items
				open.setItem(2, result);
				player.updateInventory();
			}
		});
	}

	@Override
	public ItemStack getUniqueDrop() {
		return drop;
	}
}
