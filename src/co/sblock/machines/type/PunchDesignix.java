package co.sblock.machines.type;

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

import co.sblock.Sblock;
import co.sblock.machines.utilities.MachineType;
import co.sblock.machines.utilities.Direction;
import co.sblock.machines.MachineInventoryTracker;
import co.sblock.users.ProgressionState;
import co.sblock.users.User;
import co.sblock.users.UserManager;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.inventory.InventoryUtils;
import co.sblock.utilities.progression.Entry;

/**
 * Simulate a Sburb Punch Designix in Minecraft.
 * 
 * @author Jikoo
 */
public class PunchDesignix extends Machine {

	/** The ItemStack used to create usage help */
	private static ItemStack[] exampleRecipes;

	/**
	 * @see co.sblock.Machines.Type.Machine#Machine(Location, String, Direction)
	 */
	@SuppressWarnings("deprecation")
	public PunchDesignix(Location l, String data, Direction d) {
		super(l, data, d);
		MaterialData m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.WEST).getUpperStairByte());
		shape.addBlock(new Vector(0, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.EAST).getUpperStairByte());
		shape.addBlock(new Vector(1, 0, 0), m);
		m = new MaterialData(Material.QUARTZ_STAIRS,
				d.getRelativeDirection(Direction.NORTH).getStairByte());
		shape.addBlock(new Vector(0, 1, 0), m);
		shape.addBlock(new Vector(1, 1, 0), m);
		m = new MaterialData(Material.STEP, (byte) 15);
		shape.addBlock(new Vector(0, 0, -1), m);
		shape.addBlock(new Vector(1, 0, -1), m);
		m = new MaterialData(Material.CARPET, (byte) 8);
		shape.addBlock(new Vector(0, 1, -1), m);
		shape.addBlock(new Vector(1, 1, -1), m);
		blocks = shape.getBuildLocations(getFacingDirection());
		createExampleRecipes();
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#getType()
	 */
	@Override
	public MachineType getType() {
		return MachineType.PUNCH_DESIGNIX;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#handleInteract(PlayerInteractEvent)
	 */
	@Override
	public boolean handleInteract(PlayerInteractEvent event) {
		if (super.handleInteract(event)) {
			return true;
		}
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return true;
		}
		User user = UserManager.getUser(event.getPlayer().getUniqueId());
		if (user != null && (user.getProgression() != ProgressionState.NONE
				|| Entry.getEntry().isEntering(user))) {
			openInventory(event.getPlayer());
		}
		return true;
	}

	/**
	 * @see co.sblock.Machines.Type.Machine#handleClick(InventoryClickEvent)
	 */
	@SuppressWarnings("deprecation")
	public boolean handleClick(InventoryClickEvent event) {
		if (event.getSlot() == 2 && event.getRawSlot() == event.getView().convertSlot(event.getRawSlot())
				&& event.getCurrentItem() != null && event.getCurrentItem().getType() != Material.AIR) {
			// Clicking an item in result slot

			Inventory merchant = event.getInventory();

			// Possible results:
			// 1) slot 0 is Captcha, slot 1 is null. Result: slot 2 = punch 0. 0, 1 consumed.
			// 2) slot 0 is Punch, slot 1 is Captcha. Result: slot 2 = copy 0. 1 consumed.
			// 3) slot 0 is Punch, slot 1 is Punch. Result: slot 2 = combine 0, 1. 0, 1 consumed.
			ItemStack result;
			if (Captcha.isCaptcha(merchant.getItem(1))) {
				// Copies and punches first, ignores lore of second.
				result = Captcha.createCombinedPunch(merchant.getItem(0), null);
			} else {
				// Combine cards (or, if second is null, punch first)
				result = Captcha.createCombinedPunch(merchant.getItem(0), merchant.getItem(1));
			}

			int crafts = 0;

			// Clicking a villager result slot with vanilla client treats right clicks as left clicks.
			if (event.getClick().name().contains("SHIFT")) {
				// Shift-clicks are craft-max attempts.
				if (Captcha.isPunch(merchant.getItem(0)) && Captcha.isCaptcha(merchant.getItem(1))) {
					crafts = merchant.getItem(1).getAmount();
				} else {
					crafts = getMaximumCrafts(merchant.getItem(0), merchant.getItem(1));
				}
				result.setAmount(crafts);

				// Decrement number of crafts by number of items that failed to be added
				crafts -= InventoryUtils.getAddFailures(event.getWhoClicked().getInventory().addItem(result));
			} else if (event.getCursor() == null || event.getCursor().getType() == Material.AIR) {
				// Single click. Attempting to pick up a single item (even if right click)
				crafts = 1;

				// Set cursor to single stack
				event.setCursor(result);
			} else {
				// Invalid craft, cancel and update result
				updateInventory(event.getWhoClicked().getUniqueId());
				return true;
			}

			// This will be recalculated in the synchronous delayed inventory update task.
			event.setCurrentItem(null);

			// If second item is a captcha, first item is a punchcard being copied. Do not decrement.
			if (!Captcha.isCaptcha(merchant.getItem(1))) {
				merchant.setItem(0, InventoryUtils.decrement(merchant.getItem(0), crafts));
			}

			// In all cases (combine, punch single, copy punch) if second is not null it decrements.
			merchant.setItem(1, InventoryUtils.decrement(merchant.getItem(1), crafts));

			updateInventory(event.getWhoClicked().getUniqueId());
			return true;
		}
		updateInventory(event.getWhoClicked().getUniqueId());
		return false;
	}

	/**
	 * Calculates the maximum number of items that can be crafted with the given ItemStacks.
	 * 
	 * @param slot1 the first ItemStack
	 * @param slot2 the second ItemStack
	 * 
	 * @return the least of the two, or, if slot2 is null, the amount in slot1
	 */
	private int getMaximumCrafts(ItemStack slot1, ItemStack slot2) {
		return slot2 == null ? slot1.getAmount() 
				: slot1.getAmount() > slot2.getAmount() ? slot1.getAmount() : slot2.getAmount();
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
				if (Captcha.isCaptcha(open.getItem(1))) {
					if (!Captcha.isPunch(open.getItem(0))) {
						result = null;
					}
					result = Captcha.createCombinedPunch(open.getItem(0), null);
				} else {
					result = Captcha.createCombinedPunch(open.getItem(0), open.getItem(1));
				}
				open.setItem(2, result);
				player.updateInventory();
			}
		});
	}

	/**
	 * Open a PunchDesignix inventory for a Player.
	 * 
	 * @param player the Player
	 */
	public void openInventory(Player player) {
		MachineInventoryTracker.getTracker().openMachineInventory(player, this, InventoryType.MERCHANT, getExampleRecipes());
	}

	/**
	 * Singleton for getting usage help ItemStacks.
	 */
	public static ItemStack[] getExampleRecipes() {
		if (exampleRecipes == null) {
			exampleRecipes = createExampleRecipes();
		}
		return exampleRecipes;
	}

	/**
	 * Creates the ItemStacks used in displaying usage help.
	 * 
	 * @return
	 */
	private static ItemStack[] createExampleRecipes() {
		ItemStack is1 = new ItemStack(Material.PAPER);
		ItemMeta im = is1.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Slot 1 options:");
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.GOLD + "1) Captchacard " + ChatColor.DARK_RED + "(consumed)");
		lore.add(ChatColor.GOLD + "2) Punchcard");
		lore.add(ChatColor.GOLD + "3) Punchcard " + ChatColor.DARK_RED + "(consumed)");
		im.setLore(lore);
		is1.setItemMeta(im);

		ItemStack is2 = new ItemStack(Material.PAPER);
		im = is2.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Slot 2 options:");
		lore = new ArrayList<>();
		lore.add(ChatColor.GOLD + "1) Empty");
		lore.add(ChatColor.GOLD + "2) Captchacard " + ChatColor.DARK_RED + "(consumed)");
		lore.add(ChatColor.GOLD + "3) Punchcard " + ChatColor.DARK_RED + "(consumed)");
		im.setLore(lore);
		is2.setItemMeta(im);

		ItemStack is3 = new ItemStack(Material.PAPER);
		im = is3.getItemMeta();
		im.setDisplayName(ChatColor.GOLD + "Punchcard Result:");
		lore = new ArrayList<>();
		lore.add(ChatColor.GOLD + "1) Card 1 punched");
		lore.add(ChatColor.GOLD + "2) Copy of card 1");
		lore.add(ChatColor.GOLD + "3) Card 1 and lore of 2");
		lore.add(ChatColor.DARK_RED + "Note: Due to a Minecraft");
		lore.add(ChatColor.DARK_RED + "limitation, the resulting card");
		lore.add(ChatColor.DARK_RED + "may not display properly.");
		lore.add(ChatColor.AQUA + "Click the slot anyway.");
		im.setLore(lore);
		is3.setItemMeta(im);

		return new ItemStack[] {is1, is2, is3};
	}
}
