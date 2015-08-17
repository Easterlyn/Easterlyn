package co.sblock.machines.type;

import java.util.ArrayList;
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
import co.sblock.machines.MachineInventoryTracker;
import co.sblock.machines.Machines;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.utilities.Shape;
import co.sblock.machines.utilities.Shape.MaterialDataValue;
import co.sblock.micromodules.Captcha;
import co.sblock.micromodules.CruxiteDowel;
import co.sblock.progression.Entry;
import co.sblock.users.OfflineUser;
import co.sblock.users.ProgressionState;
import co.sblock.users.Users;
import co.sblock.utilities.Experience;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Simulate a Sburb Alchemiter in Minecraft.
 * 
 * @author Jikoo
 */
public class Alchemiter extends Machine {

	private final ItemStack drop;

	public Alchemiter() {
		super(new Shape());
		Shape shape = getShape();
		MaterialDataValue m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK, (byte) 1);
		shape.setVectorData(new Vector(0, 0, 0), m);
		shape.setVectorData(new Vector(0, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 1), m);
		shape.setVectorData(new Vector(1, 0, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_BLOCK, (byte) 2);
		shape.setVectorData(new Vector(0, 0, 2), m);
		m = shape.new MaterialDataValue(Material.NETHER_FENCE);
		shape.setVectorData(new Vector(0, 1, 2), m);
		shape.setVectorData(new Vector(0, 2, 2), m);
		shape.setVectorData(new Vector(0, 3, 2), m);
		shape.setVectorData(new Vector(0, 3, 1), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.NORTH, "stair");
		shape.setVectorData(new Vector(-1, 0, -1), m);
		shape.setVectorData(new Vector(0, 0, -1), m);
		shape.setVectorData(new Vector(1, 0, -1), m);
		shape.setVectorData(new Vector(2, 0, -1), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.SOUTH, "stair");
		shape.setVectorData(new Vector(-1, 0, 2), m);
		shape.setVectorData(new Vector(1, 0, 2), m);
		shape.setVectorData(new Vector(2, 0, 2), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.EAST, "stair");
		shape.setVectorData(new Vector(-1, 0, 1), m);
		shape.setVectorData(new Vector(-1, 0, 0), m);
		m = shape.new MaterialDataValue(Material.QUARTZ_STAIRS, Direction.WEST, "stair");
		shape.setVectorData(new Vector(2, 0, 1), m);
		shape.setVectorData(new Vector(2, 0, 0), m);

		drop = new ItemStack(Material.QUARTZ_BLOCK, 1, (short) 2);
		ItemMeta meta = drop.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Alchemiter");
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
		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if (user != null && (user.getProgression() != ProgressionState.NONE
				|| Entry.getEntry().isEntering(user))) {
			openInventory(event.getPlayer(), storage);
		}
		return true;
	}

	/**
	 * Open a Alchemiter inventory for a Player.
	 * 
	 * @param player the Player
	 */
	public void openInventory(Player player, ConfigurationSection storage) {
		MachineInventoryTracker.getTracker().openVillagerInventory(player, this, getKey(storage));
	}

	@Override
	@SuppressWarnings("deprecation")
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		updateInventory(event.getWhoClicked().getUniqueId());
		if (event.getRawSlot() != event.getView().convertSlot(event.getRawSlot())) {
			// Clicked inv is not the top.
			return false;
		}
		if (event.getSlot() == 1) {
			// Exp slot is being clicked. No adding or removing items.
			return true;
		}
		if (event.getSlot() == 2 && event.getCurrentItem() != null
				&& event.getCurrentItem().getType() != Material.AIR) {
			// Item is being crafted
			Inventory top = event.getView().getTopInventory();
			Player player = (Player) event.getWhoClicked();
			if (event.getClick().name().contains("SHIFT")) {
				if (InventoryUtils.hasSpaceFor(event.getCurrentItem(), player.getInventory())) {
					player.getInventory().addItem(event.getCurrentItem().clone());
				} else {
					return true;
				}
			} else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR
					|| (event.getCursor().isSimilar(event.getCurrentItem())
						&& event.getCursor().getAmount() + event.getCurrentItem().getAmount()
						< event.getCursor().getMaxStackSize())) {
				ItemStack result = event.getCurrentItem().clone();
				if (result.isSimilar(event.getCursor())) {
					result.setAmount(result.getAmount() + event.getCursor().getAmount());
				}
				event.setCursor(result);
			} else {
				return true;
			}
			event.setCurrentItem(null);
			top.setItem(0, InventoryUtils.decrement(top.getItem(0), 1));
			// Color code + "Grist cost: " = 14 chars
			int expCost = Integer.valueOf(top.getItem(1).getItemMeta().getDisplayName().substring(14));
			Experience.changeExp(player, -expCost);
			player.updateInventory();
		}
		return false;
	}

	/**
	 * Calculate result slot and update inventory on a delay (post-event completion)
	 * 
	 * @param name the name of the player who is using the Punch Designix
	 */
	public void updateInventory(final UUID id) {
		Bukkit.getScheduler().scheduleSyncDelayedTask(Sblock.getInstance(), new Runnable() {
			@Override
			public void run() {
				// Must re-obtain player or update doesn't seem to happen
				Player player = Bukkit.getPlayer(id);
				if (player == null || !MachineInventoryTracker.getTracker().hasMachineOpen(player)) {
					// Player has logged out or closed inventory. Inventories are per-player, ignore.
					return;
				}

				Inventory open = player.getOpenInventory().getTopInventory();
				ItemStack result;
				ItemStack expCost;
				if (CruxiteDowel.isUsedDowel(open.getItem(0))) {
					if (open.getItem(0).getItemMeta().getLore().contains("Blank")) {
						result = Machines.getMachineByName("PGO").getUniqueDrop();
					} else {
						result = Captcha.captchaToItem(open.getItem(0));
					}
					expCost = new ItemStack(Material.EXP_BOTTLE);
					int exp = CruxiteDowel.expCost(result);
					ItemMeta im = expCost.getItemMeta();
					int playerExp = Experience.getExp(player);
					int remainder = playerExp - exp;
					ChatColor color = remainder > 0 ? ChatColor.GREEN : ChatColor.DARK_RED;
					im.setDisplayName(color + "Grist cost: " + exp);
					ArrayList<String> lore = new ArrayList<>();
					lore.add(ChatColor.GOLD + "Current: " + playerExp);
					if (remainder >= 0) {
						lore.add(ChatColor.GOLD + "Remainder: " + remainder);
					} else {
						lore.add(ChatColor.DARK_RED.toString() + ChatColor.BOLD + "Not enough grist!");
						result = null;
					}
					im.setLore(lore);
					expCost.setItemMeta(im);
				} else {
					result = null;
					expCost = null;
				}
				// Set items
				open.setItem(1, expCost);
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
