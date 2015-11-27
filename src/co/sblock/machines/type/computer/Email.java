package co.sblock.machines.type.computer;

import java.util.Arrays;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.Color;
import co.sblock.effects.Effects;
import co.sblock.machines.Machines;
import co.sblock.machines.type.Computer;
import co.sblock.users.Region;
import co.sblock.users.Users;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * GUI mail client.
 * 
 * @author Jikoo
 */
public class Email extends Program {

	private final Effects effects;
	private final Users users;
	private final ItemStack icon;

	public Email(Machines machines) {
		super(machines);
		this.effects = machines.getPlugin().getModule(Effects.class);
		this.users = machines.getPlugin().getModule(Users.class);
		icon = new ItemStack(Material.WRITTEN_BOOK);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Email");
		meta.setLore(Arrays.asList(ChatColor.WHITE + "Check your messages!"));
		icon.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {

		// Prevent players from checking mail in improper places
		if (Region.getRegion(player.getWorld().getName()).isDream()) {
			player.closeInventory();
			player.sendMessage(Color.BAD + "You dream of a clean, empty inbox.");
			return;
		}
		if (player.getGameMode() != GameMode.SURVIVAL) {
			player.closeInventory();
			player.sendMessage(Color.BAD + "You can only manage your mail while playing normally.");
			return;
		}
		if (users.getUser(player.getUniqueId()).isServer()) {
			player.closeInventory();
			player.sendMessage(Color.BAD + "You're too busy helping your client to check your email.");
			return;
		}

		Inventory inventory = ((Computer) getMachines().getMachineByName("Computer")).getInventory(9);
		boolean atComputer = clicked != null && !clicked.getItemMeta().hasItemFlag(ItemFlag.HIDE_UNBREAKABLE)
				|| effects.getAllEffects(player).containsKey(effects.getEffect("Computer"));
		ItemStack inbox = Programs.getProgramByName("Inbox").getIcon();
		if (!atComputer) {
			ItemMeta inMeta = inbox.getItemMeta();
			inMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			inbox.setItemMeta(inMeta);
		}
		inventory.addItem(inbox, Programs.getProgramByName("EmailWriter").getIcon());
		if (atComputer) {
			inventory.setItem(8, Programs.getProgramByName("Back").getIcon());
		}
		player.openInventory(inventory);
		InventoryUtils.changeWindowName(player, "~/Email");
	}

	@Override
	public boolean isDefault() {
		return true;
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
