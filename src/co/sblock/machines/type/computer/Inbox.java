package co.sblock.machines.type.computer;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;
import co.sblock.machines.type.Computer;
import co.sblock.users.Users;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Inbox and mail collection for the GUI mail client.
 * 
 * @author Jikoo
 */
public class Inbox extends Program {

	private final Users users;
	private final ItemStack icon;

	public Inbox(Machines machines) {
		super(machines);
		this.users = machines.getPlugin().getModule(Users.class);
		icon = new ItemStack(Material.WRITTEN_BOOK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Read Mail");
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		if (clicked == null) {
			return;
		}

		ItemMeta meta = clicked.getItemMeta();
		// The NBT flag for not displaying unbreakable status represents the
		// mail client being reached without computer access
		boolean atComputer = !meta.hasItemFlag(ItemFlag.HIDE_UNBREAKABLE);
		if (!meta.hasLore() || atComputer) {
			Inventory inventory = ((Computer) getMachines().getMachineByName("Computer")).getInventory(54);
			List<ItemStack> items = users.getUser(player.getUniqueId()).getMailItems();
			ItemStack nope = ((Nope) Programs.getProgramByName("Nope")).getIconFor(
					ChatColor.WHITE + "Computer access is required",
					ChatColor.WHITE + "to collect items from mail.");
			for (int i = 0; i < 45 && i < items.size(); i++) {
				ItemStack item = items.get(i);
				if (!atComputer && item.getType() != Material.WRITTEN_BOOK) {
					inventory.setItem(i, nope.clone());
				} else {
					inventory.setItem(i, iconFromItem(item));
				}
			}
			ItemStack back = ((Back) Programs.getProgramByName("Back")).getBackTo("Email");
			if (!atComputer) {
				// Pass flag back to Email when back button is used
				ItemMeta backMeta = back.getItemMeta();
				backMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
				back.setItemMeta(backMeta);
			}
			inventory.setItem(49, back);
			player.openInventory(inventory);
			InventoryUtils.changeWindowName(player, "~/Email/Inbox");
			return;
		}
		InventoryView view = player.getOpenInventory();
		if (view.getBottomInventory().firstEmpty() == -1
				|| view.getTopInventory().removeItem(clicked).size() > 0) {
			return;
		}
		player.getInventory().addItem(itemFromIcon(clicked));
	}

	private ItemStack iconFromItem(ItemStack item) {
		if (item.getType() == Material.WRITTEN_BOOK) {
			BookMeta iconMeta = (BookMeta) icon.getItemMeta();
			BookMeta itemMeta = (BookMeta) item.getItemMeta();
			iconMeta.setAuthor(itemMeta.getAuthor());
			iconMeta.setLore(Arrays.asList(ChatColor.WHITE + itemMeta.getTitle()));
			iconMeta.setPages(itemMeta.getPages());
			ItemStack icon = this.icon.clone();
			icon.setItemMeta(iconMeta);
			return icon;
		}
		return null; // POST-ENTRY: Item collection icon (probably a captcha)
	}

	private ItemStack itemFromIcon(ItemStack icon) {
		if (icon.getType() == Material.WRITTEN_BOOK) {
			BookMeta meta = (BookMeta) icon.getItemMeta();
			meta.setDisplayName(null);
			meta.setLore(null);
			icon.setItemMeta(meta);
			return icon;
		}
		return null; // POST-ENTRY: Item collection
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

}
