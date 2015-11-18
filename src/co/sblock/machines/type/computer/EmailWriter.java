package co.sblock.machines.type.computer;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.chat.Color;

import net.md_5.bungee.api.ChatColor;

/**
 * A Program for obtaining a sendable Letter.
 * 
 * @author Jikoo
 */
public class EmailWriter extends Program {

	private final ItemStack icon;
	private final ItemStack writable;

	protected EmailWriter() {
		icon = new ItemStack(Material.BOOK_AND_QUILL);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "Write Mail");
		icon.setItemMeta(meta);

		writable = new ItemStack(Material.BOOK_AND_QUILL);
		meta = writable.getItemMeta();
		meta.setDisplayName(Color.GOOD_EMPHASIS + "Letter");
		meta.setLore(Arrays.asList(Color.GOOD + "Write your message inside.",
				Color.GOOD + "When done, sign and title the book with the recipient's name.",
				Color.GOOD + "The first 20 letters will be the title of the mail.", "",
				Color.BAD + "Dropping or clicking this item will delete it."));
		writable.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		player.closeInventory();
		if (player.getInventory().firstEmpty() == -1) {
			player.sendMessage(Color.BAD + "You don't have space for a book to write!");
			return;
		}

		ItemStack readd = player.getItemInHand().clone();
		player.setItemInHand(writable.clone());
		if (readd != null) {
			player.getInventory().addItem(readd);
		}

		player.sendMessage(Color.GOOD + "You've been given a letter. Sign it with the recipient's name as the title to send it!");
	}

	public boolean isLetter(ItemStack item) {
		return item != null && item.getType() == Material.BOOK_AND_QUILL && item.hasItemMeta()
				&& isLetterMeta(item.getItemMeta());
	}

	public boolean isLetterMeta(ItemMeta meta) {
		ItemMeta writableMeta = writable.getItemMeta();
		return meta.hasDisplayName() && meta.hasLore()
				&& meta.getDisplayName().equals(writableMeta.getDisplayName())
				&& meta.getLore().equals(writableMeta.getLore());
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
