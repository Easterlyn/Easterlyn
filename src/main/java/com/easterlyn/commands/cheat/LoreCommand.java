package com.easterlyn.commands.cheat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * EasterlynCommand for manipulating item lore and some other meta-related cases.
 *
 * @author Jikoo
 */
public class LoreCommand extends EasterlynCommand {

	private final String[] primaryArgs;

	// TODO lang
	public LoreCommand(Easterlyn plugin) {
		super(plugin, "lore");
		this.setDescription("Easterlyn's lore manipulation command.");
		this.setPermissionLevel(UserRank.MOD);
		this.setUsage("/lore owner: Set a skull's owner.\n"
				+ "/lore author|title: Set a book's data.\n"
				+ "/lore name: Set an item's name.\n"
				+ "/lore delete owner|author|title|name|<number>: Reset a single element.\n"
				+ "/lore clearmeta: Wipe all meta. Includes enchantments and such.\n"
				+ "/lore add: Add a new line of lore.\n"
				+ "/lore set|insert <number>: Set or insert a line at the specified index.\n"
				+ "All /lore number parameters are 1-indexed.");
		// Don't forget to update delete tab completion if more pre-delete args are added.
		primaryArgs = new String[] {"owner", "author", "title", "name", "delete", "clearmeta", "add", "set", "insert", "unique"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		// future: leather color
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length < 1) {
			return false;
		}
		args[0] = args[0].toLowerCase();
		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			player.sendMessage(Language.getColor("bad") + "You need an item in main hand to use this command!");
			return true;
		}
		if (!hand.hasItemMeta() && hand.getItemMeta() == null) {
			ItemMeta meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
			if (meta == null) {
				sender.sendMessage(Language.getColor("bad") + "This item does not support meta.");
				return true;
			}
			hand.setItemMeta(meta);
		}
		if (args[0].equals("clearmeta")) {
			ItemMeta meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
			hand.setItemMeta(meta);
			player.sendMessage("Meta cleared!");
			return true;
		}
		if (args.length < 2) {
			return false;
		}
		if (args[0].equals("owner")) {
			return owner(player, hand, args);
		}
		if (args[0].equals("author")) {
			return author(player, hand, args);
		}
		if (args[0].equals("title")) {
			return title(player, hand, args);
		}
		if (args[0].equals("name")) {
			return name(player, hand, args);
		}
		if (args[0].equals("delete")) {
			return delete(player, hand, args);
		}
		if (args[0].equals("add")) {
			return add(player, hand, args);
		}
		if (args[0].equals("set")) {
			return set(player, hand, args);
		}
		if (args[0].equals("insert")) {
			return insert(player, hand, args);
		}
		if (args[0].equals("unique")) {
			return add(player, hand, new String[] {"add", InventoryUtils.ITEM_UNIQUE});
		}
		sender.sendMessage(getUsage());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length == 0) {
			return ImmutableList.of();
		}
		if (args.length > 2) {
			return super.tabComplete(sender, alias, args);
		}
		args[0] = args[0].toLowerCase();
		ArrayList<String> matches = new ArrayList<>();
		if (args.length == 2) {
			if (args[0].equals("delete")) {
				for (String primaryArg : primaryArgs) {
					if (primaryArg.startsWith(args[1].toLowerCase())) {
						matches.add(primaryArg);
					}
				}
			}
			if (args[0].equals("delete") || args[0].equals("set") || args[0].equals("insert") && args[1].isEmpty()) {
				matches.add("#");
				return matches;
			}
			return super.tabComplete(sender, alias, args);
		}
		if (args.length == 1) {
			for (String argument : primaryArgs) {
				if (argument.startsWith(args[0])) {
					matches.add(argument);
				}
			}
		}
		return matches;
	}

	private boolean owner(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.PLAYER_HEAD) {
			player.sendMessage(Language.getColor("bad") + "You must be holding a player skull to set its owner!");
			return true;
		}
		SkullMeta meta = (SkullMeta) hand.getItemMeta();
		meta.setOwningPlayer(Bukkit.getOfflinePlayer(args[1]));
		hand.setItemMeta(meta);
		player.sendMessage(Language.getColor("good") + "Owner set to " + args[1]);
		return true;
	}

	private boolean author(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
			player.sendMessage(Language.getColor("bad") + "You must be holding a writable book to set its author.");
			return true;
		}
		BookMeta meta = (BookMeta) hand.getItemMeta();
		String author = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		meta.setAuthor(author);
		hand.setItemMeta(meta);
		player.sendMessage(Language.getColor("good") + "Author set to " + author);
		return true;
	}

	private boolean title(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
			player.sendMessage(Language.getColor("bad") + "You must be holding a writable book to set its title.");
			return true;
		}
		BookMeta meta = (BookMeta) hand.getItemMeta();
		String title = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		meta.setTitle(title);
		hand.setItemMeta(meta);
		player.sendMessage(Language.getColor("good") + "Title set to " + title);
		return true;
	}

	private boolean name(Player player, ItemStack hand, String[] args) {
		ItemMeta meta = hand.getItemMeta();
		String name = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		meta.setDisplayName(name);
		hand.setItemMeta(meta);
		player.sendMessage(Language.getColor("good") + "Name set to " + name);
		return true;
	}

	private boolean delete(Player player, ItemStack hand, String[] args) {
		if (args[1].equals("owner")) {
			if (hand.getType() != Material.PLAYER_HEAD) {
				player.sendMessage(Language.getColor("bad") + "You must be holding a player skull to delete its owner!");
				return true;
			}
			SkullMeta meta = (SkullMeta) hand.getItemMeta();
			meta.setOwningPlayer(null);
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Deleted owner!");
			return true;
		}
		if (args[1].equals("author") || args[1].equals("title")) {
			if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
				player.sendMessage(Language.getColor("bad") + "You must be holding a writable book to clear its " + args[1] + ".");
				return true;
			}
			BookMeta meta = (BookMeta) hand.getItemMeta();
			if (args[1].equals("author")) {
				meta.setAuthor(null);
			} else {
				meta.setTitle(null);
			}
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Deleted " + args[1] + "!");
			return true;
		}
		if (args[1].equals("name")) {
			ItemMeta meta = hand.getItemMeta();
			meta.setDisplayName(null);
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Deleted name!");
			return true;
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(Language.getColor("bad") + "Item has no lore!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(Language.getColor("bad") + "Item only has " + lore.size() + " lines, cannot delete " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String removed = lore.remove(line - 1);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Deleted \"" + removed + Language.getColor("good") + "\" from line " + line + "!");
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/lore delete <owner|title|author|name|[lore line number]>");
			return true;
		}
	}

	private boolean add(Player player, ItemStack hand, String[] args) {
		ItemMeta meta = hand.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		if (meta.hasLore()) {
			lore.addAll(meta.getLore());
		}
		lore.add(ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length)));
		meta.setLore(lore);
		hand.setItemMeta(meta);
		player.sendMessage(Language.getColor("good") + "Added \"" + lore.get(lore.size() - 1) + Language.getColor("good") + "\"");
		return true;
	}

	private boolean set(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(Language.getColor("bad") + "/lore set <number> <arguments>");
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(Language.getColor("bad") + "Item has no lore to set!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(Language.getColor("bad") + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String added = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 2, args.length));
			String removed = lore.set(line - 1, added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Replaced \"" + removed + Language.getColor("good")
					+ "\" with \"" + added + Language.getColor("good") + "\" at "+ line + "!");
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/lore set <number> <arguments>");
			return true;
		}
	}

	private boolean insert(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(Language.getColor("bad") + "/lore insert <number> <arguments>");
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(Language.getColor("bad") + "Item has no lore to set!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(Language.getColor("bad") + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String added = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 2, args.length));
			player.sendMessage(Language.getColor("good") + "Inserted \"" + added + Language.getColor("good") + "\" at " + line + "!");
			int size = lore.size();
			for (int i = 0; i < size; i++) {
				if (line - 1 <= i) {
					added = lore.set(i, added);
				}
			}
			lore.add(added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/lore insert <number> <arguments>");
			return true;
		}
	}
}
