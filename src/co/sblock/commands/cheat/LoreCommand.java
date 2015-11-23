package co.sblock.commands.cheat;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for manipulating item lore and some other meta-related cases.
 * 
 * @author Jikoo
 */
public class LoreCommand extends SblockCommand {

	private final String[] primaryArgs;

	public LoreCommand(Sblock plugin) {
		super(plugin, "lore");
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
		ItemStack hand = player.getItemInHand();
		if (hand == null || hand.getType() == Material.AIR) {
			player.sendMessage(Color.BAD + "You need an item in hand to use this command!");
			return true;
		}
		if (!hand.hasItemMeta() && hand.getItemMeta() == null) {
			ItemMeta meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
			if (meta == null) {
				sender.sendMessage(Color.BAD + "This item does not support meta.");
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
			player.sendMessage(Color.BAD + "You must be holding a player skull to set its owner!");
			return true;
		}
		SkullMeta meta = (SkullMeta) hand.getItemMeta();
		meta.setOwner(args[1]);
		hand.setItemMeta(meta);
		player.sendMessage(Color.GOOD + "Owner set to " + args[1]);
		return true;
	}

	private boolean author(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
			player.sendMessage(Color.BAD + "You must be holding a writable book to set its author.");
			return true;
		}
		BookMeta meta = (BookMeta) hand.getItemMeta();
		String author = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length));
		meta.setAuthor(author);
		hand.setItemMeta(meta);
		player.sendMessage(Color.GOOD + "Author set to " + author);
		return true;
	}

	private boolean title(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
			player.sendMessage(Color.BAD + "You must be holding a writable book to set its title.");
			return true;
		}
		BookMeta meta = (BookMeta) hand.getItemMeta();
		String title = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length));
		meta.setTitle(title);
		hand.setItemMeta(meta);
		player.sendMessage(Color.GOOD + "Title set to " + title);
		return true;
	}

	private boolean name(Player player, ItemStack hand, String[] args) {
		ItemMeta meta = hand.getItemMeta();
		String name = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 1, args.length));
		meta.setDisplayName(name);
		hand.setItemMeta(meta);
		player.sendMessage(Color.GOOD + "Name set to " + name);
		return true;
	}

	private boolean delete(Player player, ItemStack hand, String[] args) {
		if (args[1].equals("owner")) {
			if (hand.getType() != Material.SKULL_ITEM && hand.getDurability() != 3) {
				player.sendMessage(Color.BAD + "You must be holding a player skull to delete its owner!");
				return true;
			}
			SkullMeta meta = (SkullMeta) hand.getItemMeta();
			meta.setOwner(null);
			hand.setItemMeta(meta);
			player.sendMessage(Color.GOOD + "Deleted owner!");
			return true;
		}
		if (args[1].equals("author") || args[1].equals("title")) {
			if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.BOOK_AND_QUILL) {
				player.sendMessage(Color.BAD + "You must be holding a writable book to clear its " + args[1] + ".");
				return true;
			}
			BookMeta meta = (BookMeta) hand.getItemMeta();
			if (args[1].equals("author")) {
				meta.setAuthor(null);
			} else {
				meta.setTitle(null);
			}
			hand.setItemMeta(meta);
			player.sendMessage(Color.GOOD + "Deleted " + args[1] + "!");
			return true;
		}
		if (args[1].equals("name")) {
			ItemMeta meta = hand.getItemMeta();
			meta.setDisplayName(null);
			hand.setItemMeta(meta);
			player.sendMessage(Color.GOOD + "Deleted name!");
			return true;
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(Color.BAD + "Item has no lore!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(Color.BAD + "Item only has " + lore.size() + " lines, cannot delete " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(Color.BAD + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String removed = lore.remove(line - 1);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(Color.GOOD + "Deleted \"" + removed + Color.GOOD + "\" from line " + line + "!");
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(Color.BAD + "/lore delete <owner|title|author|name|[lore line number]>");
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
		player.sendMessage(Color.GOOD + "Added \"" + lore.get(lore.size() - 1) + Color.GOOD + "\"");
		return true;
	}

	private boolean set(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(Color.BAD + "/lore set <number> <arguments>");
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(Color.BAD + "Item has no lore to set!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(Color.BAD + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(Color.BAD + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String added = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 2, args.length));
			String removed = lore.set(line - 1, added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(Color.GOOD + "Replaced \"" + removed + Color.GOOD
					+ "\" with \"" + added + Color.GOOD + "\" at "+ line + "!");
			return true;
		} catch (NumberFormatException e) {
			player.sendMessage(Color.BAD + "/lore set <number> <arguments>");
			return true;
		}
	}

	private boolean insert(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(Color.BAD + "/lore insert <number> <arguments>");
		}
		try {
			int line = Integer.parseInt(args[1]);
			ItemMeta meta = hand.getItemMeta();
			if (!meta.hasLore()) {
				player.sendMessage(Color.BAD + "Item has no lore to set!");
				return true;
			}
			ArrayList<String> lore = new ArrayList<>(meta.getLore());
			if (lore.size() < line) {
				player.sendMessage(Color.BAD + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return true;
			}
			if (line < 1) {
				player.sendMessage(Color.BAD + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return true;
			}
			String added = ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ', 2, args.length));
			player.sendMessage(Color.GOOD + "Inserted \"" + added + Color.GOOD + "\" at " + line + "!");
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
			player.sendMessage(Color.BAD + "/lore insert <number> <arguments>");
			return true;
		}
	}
}
