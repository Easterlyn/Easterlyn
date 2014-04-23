package co.sblock.Sblock.Machines.Type;

import java.util.ArrayList;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;
import org.bukkit.util.Vector;

import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Machines.MachineInventoryTracker;
import co.sblock.Sblock.Utilities.Captcha.Captcha;
import co.sblock.Sblock.Utilities.Captcha.CruxiteDowel;
import co.sblock.Sblock.Utilities.Inventory.InventoryUtils;
import co.sblock.Sblock.Utilities.experience.Experience;

/**
 * Simulate a Sburb Alchemiter in Minecraft.
 * 
 * @author Jikoo
 */
public class Alchemiter extends Machine {

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public Alchemiter(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 1);
		shape.addBlock(new Vector(0, 0, 0), m);
		shape.addBlock(new Vector(0, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 1), m);
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_BLOCK, (byte) 2);
		shape.addBlock(new Vector(0, 0, 2), m);
		m = new MaterialData(Material.NETHER_FENCE);
		shape.addBlock(new Vector(0, 1, 2), m);
		shape.addBlock(new Vector(0, 2, 2), m);
		shape.addBlock(new Vector(0, 3, 2), m);
		shape.addBlock(new Vector(0, 3, 1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS, d.getStairByte());
		shape.addBlock(new Vector(-1, 0, -1), m);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(1, 0, -1), m);
		shape.addBlock(new Vector(2, 0, -1), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.SOUTH).getStairByte());
		shape.addBlock(new Vector(-1, 0, 2), m);
		shape.addBlock(new Vector(1, 0, 2), m);
		shape.addBlock(new Vector(2, 0, 2), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.WEST).getStairByte());
		shape.addBlock(new Vector(-1, 0, 1), m);
		shape.addBlock(new Vector(-1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.EAST).getStairByte());
		shape.addBlock(new Vector(2, 0, 1), m);
		shape.addBlock(new Vector(2, 0, 0), m);
		blocks = shape.getBuildLocations(getFacingDirection());
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.ALCHEMITER;
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		openInventory(event.getPlayer());
		return true;
	}

	/**
	 * Open a PunchDesignix inventory for a Player.
	 * 
	 * @param player the Player
	 */
	public void openInventory(Player player) {
		MachineInventoryTracker.getTracker().openMachineInventory(player, this, InventoryType.MERCHANT);
	}

	/**
	 * @see co.sblock.Sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	@SuppressWarnings("deprecation")
	public boolean handleClick(InventoryClickEvent event) {
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
				player.getInventory().addItem(event.getCurrentItem().clone());
			} else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
				event.setCursor(event.getCurrentItem());
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
			@SuppressWarnings("deprecation")
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
						result = MachineType.PERFECTLY_GENERIC_OBJECT.getUniqueDrop();
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
}
