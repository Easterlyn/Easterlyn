package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.ImmutableList;

/**
 * SblockCommand for manipulating item lore and some other meta-related cases.
 * 
 * @author Jikoo
 */
public class LoreCommand extends SblockCommand {

	private final String[] primaryArgs;

	public LoreCommand() {
		super("lore");
		this.setDescription("Sblock's lore manipulation command.");
		this.setUsage("/lore owner: Set a skull's owner.\n"
				+ "/lore author|title: Set a book's data.\n"
				+ "/lore name: Set an item's name.\n"
				+ "/lore delete owner|author|title|name|<number>: Reset a single element.\n"
				+ "/lore clearmeta: Wipe all meta. Includes enchantments and such.\n"
				+ "/lore add: Add a new line of lore.\n"
				+ "/lore set|insert <number>: Set or insert a line at the specidified index.\n"
				+ "All /lore number parameters are 1-indexed.");
		this.setPermissionLevel("felt");
		// Don't forget to update delete tab completion if more pre-delete args are added.
		primaryArgs = new String[] {"owner", "author", "title", "name", "delete", "clearmeta", "add", "set", "insert"};
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		// TODO better help, leather color
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (args.length < 2) {
			sender.sendMessage(getUsage());
			return true;
		}
		args[0] = args[0].toLowerCase();
		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand == null) {
			player.sendMessage(ChatColor.RED + "You need an item in hand to use this command!");
			return true;
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
		if (args[0].equals("clearmeta")) {
			ItemMeta meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
			hand.setItemMeta(meta);
			player.sendMessage("Meta cleared!");
			return true;
		}
		sender.sendMessage(getUsage());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length == 0) {
			return ImmutableList.of();
		}
		if (args.length > 2) {
			return super.tabComplete(sender, alias, args);
		}
		args[0] = args[0].toLowerCase();
		ArrayList<String> matches = new ArrayList<>();
		if (args.length == 2) {
			if (args[0].equals("delete")) {
				for (int i = 0; i < 4; i++) { // This will need changing if more args are added
					if (primaryArgs[i].startsWith(args[1].toLowerCase())) {
						matches.add(primaryArgs[i]);
					}
				}
			}
			if (args[0].equals("delete") || args[0].equals("set") || args.equals("insert") && args[1].isEmpty()) {
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
		if (hand.getType() != Material.SKULL_ITEM && hand.getDurability() != 3) {
			player.sendMessage(ChatColor.RED + "You must be holding a player skull to set its owner!");
			return true;
		}
		SkullMeta meta = (SkullMeta) hand.getItemMeta();
		meta.setOwner(args[1]);
		hand.setItemMeta(meta);
		player.sendMessage(ChatColor.GREEN + "Owner set to " + args[1]);
		return true;
	}

	private boolean author(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
			player.sendMessage(ChatColor.RED + "You must be holding a writable book to set its author.");
			return true;
		}
		BookMeta meta = (BookMeta) hand.getItemMeta();
		String author = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length));
		meta.setAuthor(author);
		hand.setItemMeta(meta);
		player.sendMessage(ChatColor.GREEN + "Author set to " + author);
		return true;
	}

	private boolean title(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
			player.sendMessage(ChatColor.RED + "You must be holding a writable book to set its title.");
			return true;
		}
		BookMeta meta = (BookMeta) hand.getItemMeta();
		String title = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length));
		meta.setTitle(title);
		hand.setItemMeta(meta);
		player.sendMessage(ChatColor.GREEN + "Title set to " + title);
		return true;
	}

	private boolean name(Player player, ItemStack hand, String[] args) {
		ItemMeta meta = hand.getItemMeta();
		String name = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length));
		meta.setDisplayName(name);
		hand.setItemMeta(meta);
		player.sendMessage(ChatColor.GREEN + "Name set to " + name);
		return true;
	}

	private boolean delete(Player player, ItemStack hand, String[] args) {
		if (args[1].equals("owner")) {
			if (hand.getType() != Material.SKULL_ITEM && hand.getDurability() != 3) {
				player.sendMessage(ChatColor.RED + "You must be holding a player skull to delete its owner!");
				return true;
			}
			SkullMeta meta = (SkullMeta) hand.getItemMeta();
			meta.setOwner(null);
			hand.setItemMeta(meta);
			player.sendMessage(ChatColor.GREEN + "Owner cleared!");
			return true;
		}
		if (args[1].equals("author")) {
			if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
				player.sendMessage(ChatColor.RED + "You must be holding a writable book to clear its author.");
				return true;
			}
			BookMeta meta = (BookMeta) hand.getItemMeta();
			meta.setAuthor(null);
			hand.setItemMeta(meta);
			player.sendMessage(ChatColor.GREEN + "Author cleared!");
			return true;
		}
		if (args[1].equals("title")) {
			if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
				player.sendMessage(ChatColor.RED + "You must be holding a writable book to clear its title.");
				return true;
			}
			BookMeta meta = (BookMeta) hand.getItemMeta();
			meta.setTitle(null);
			hand.setItemMeta(meta);
			player.sendMessage(ChatColor.GREEN + "Title cleared!");
			return true;
		}
		if (args[1].equals("name")) {
			ItemMeta meta = hand.getItemMeta();
			meta.setDisplayName(null);
			hand.setItemMeta(meta);
			player.sendMessage(ChatColor.GREEN + "Name cleared!");
			return true;
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(ChatColor.RED + "Item has no lore!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(ChatColor.RED + "Item only has " + lore.size() + " lines, cannot delete " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(ChatColor.RED + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String removed = lore.remove(line - 1);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(ChatColor.GREEN + "Deleted \"" + removed + ChatColor.GREEN + "\" from line " + line + "!");
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "/lore delete <owner|title|author|name|[lore line number]>");
			return true;
		}
	}

	private boolean add(Player player, ItemStack hand, String[] args) {
		ItemMeta meta = hand.getItemMeta();
		ArrayList<String> lore = new ArrayList<String>();
		if (meta.hasLore()) {
			lore.addAll(meta.getLore());
		}
		lore.add(ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length)));
		meta.setLore(lore);
		hand.setItemMeta(meta);
		player.sendMessage(ChatColor.GREEN + "Added \"" + lore.get(lore.size() - 1) + ChatColor.GREEN + "\"");
		return true;
	}

	private boolean set(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(ChatColor.RED + "/lore set <number> <arguments>");
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(ChatColor.RED + "Item has no lore to set!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(ChatColor.RED + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(ChatColor.RED + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String added = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 2, args.length));
			String removed = lore.set(line - 1, added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(ChatColor.GREEN + "Replaced \"" + removed + ChatColor.GREEN
					+ "\" with \"" + added + ChatColor.GREEN + "\" at "+ line + "!");
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(ChatColor.RED + "/lore set <number> <arguments>");
			return true;
		}
	}

	private boolean insert(Player player, ItemStack hand, String[] args) {

		if (args.length < 3) {
			player.sendMessage(ChatColor.RED + "/lore insert <number> <arguments>");
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(ChatColor.RED + "Item has no lore to set!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(ChatColor.RED + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(ChatColor.RED + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String added = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 2, args.length));
			player.sendMessage(ChatColor.GREEN + "Inserted \"" + added + ChatColor.GREEN + "\" at " + line + "!");
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
			player.sendMessage(ChatColor.RED + "/lore insert <number> <arguments>");
			return true;
		}
	}
}
