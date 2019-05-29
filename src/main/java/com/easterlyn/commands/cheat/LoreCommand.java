package com.easterlyn.commands.cheat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import java.util.Objects;
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
import org.jetbrains.annotations.NotNull;

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
				+ "/lore unique: Add lore for making an item un-dublexable"
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
		if (hand.getType() == Material.AIR) {
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
		switch (args[0]) {
			case "owner":
				owner(player, hand, args);
				break;
			case "author":
				author(player, hand, args);
				break;
			case "title":
				title(player, hand, args);
				break;
			case "name":
				name(player, hand, args);
				break;
			case "delete":
				delete(player, hand, args);
				break;
			case "add":
				add(player, hand, args);
				break;
			case "set":
				set(player, hand, args);
				break;
			case "insert":
				insert(player, hand, args);
				break;
			case "unique":
				add(player, hand, new String[]{"add", InventoryUtils.ITEM_UNIQUE});
				break;
			default:
				sender.sendMessage(getUsage());
				break;
		}
		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || this.getPermission() != null && !sender.hasPermission(this.getPermission())
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
		for (String argument : primaryArgs) {
			if (argument.startsWith(args[0])) {
				matches.add(argument);
			}
		}
		return matches;
	}

	private void owner(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.PLAYER_HEAD) {
			player.sendMessage(Language.getColor("bad") + "You must be holding a player skull to set its owner!");
			return;
		}
		InventoryUtils.consumeAs(SkullMeta.class, hand.getItemMeta(), skullMeta -> {
			//noinspection deprecation // No alternative API
			skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(args[1]));
			hand.setItemMeta(skullMeta);
		});
		player.sendMessage(Language.getColor("good") + "Owner set to " + args[1]);
		return;
	}

	private void author(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
			player.sendMessage(Language.getColor("bad") + "You must be holding a writable book to set its author.");
			return;
		}
		String author = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		InventoryUtils.consumeAs(BookMeta.class, hand.getItemMeta(), bookMeta -> {
			bookMeta.setAuthor(author);
			hand.setItemMeta(bookMeta);
		});
		player.sendMessage(Language.getColor("good") + "Author set to " + author);
		return;
	}

	private void title(Player player, ItemStack hand, String[] args) {
		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
			player.sendMessage(Language.getColor("bad") + "You must be holding a writable book to set its title.");
			return;
		}
		String title = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		InventoryUtils.consumeAs(BookMeta.class, hand.getItemMeta(), bookMeta -> {
			bookMeta.setTitle(title);
			hand.setItemMeta(bookMeta);
		});
		player.sendMessage(Language.getColor("good") + "Title set to " + title);
		return;
	}

	private void name(Player player, ItemStack hand, String[] args) {
		String name = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		ItemMeta meta = hand.getItemMeta();
		if (meta != null) {
			meta.setDisplayName(name);
		}
		hand.setItemMeta(meta);
		player.sendMessage(Language.getColor("good") + "Name set to " + name);
		return;
	}

	private void delete(Player player, ItemStack hand, String[] args) {
		if (args[1].equals("owner")) {
			if (hand.getType() != Material.PLAYER_HEAD) {
				player.sendMessage(Language.getColor("bad") + "You must be holding a player skull to delete its owner!");
				return;
			}
			InventoryUtils.consumeAs(SkullMeta.class, hand.getItemMeta(), skullMeta -> {
				skullMeta.setOwningPlayer(null);
				hand.setItemMeta(skullMeta);
			});
			player.sendMessage(Language.getColor("good") + "Deleted owner!");
			return;
		}
		if (args[1].equals("author") || args[1].equals("title")) {
			if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
				player.sendMessage(Language.getColor("bad") + "You must be holding a writable book to clear its " + args[1] + ".");
				return;
			}
			InventoryUtils.consumeAs(BookMeta.class, hand.getItemMeta(), bookMeta -> {
				if (args[1].equals("author")) {
					bookMeta.setAuthor(null);
				} else {
					bookMeta.setTitle(null);
				}
				hand.setItemMeta(bookMeta);
			});
			player.sendMessage(Language.getColor("good") + "Deleted " + args[1] + "!");
			return;
		}
		if (args[1].equals("name")) {
			ItemMeta meta = hand.getItemMeta();
			if (meta != null) {
				meta.setDisplayName(null);
			}
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Deleted name!");
			return;
		}
		int line;
		try {
			line = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/lore delete <owner|title|author|name|[lore line number]>");
			return;
		}

		InventoryUtils.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			if (!meta.hasLore()) {
				player.sendMessage(Language.getColor("bad") + "Item has no lore!");
				return;
			}
			ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
			if (lore.size() < line) {
				player.sendMessage(Language.getColor("bad") + "Item only has " + lore.size() + " lines, cannot delete " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return;
			}

			String removed = lore.remove(line - 1);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Deleted \"" + removed + Language.getColor("good") + "\" from line " + line + "!");
		});

		return;
	}

	private void add(Player player, ItemStack hand, String[] args) {
		String loreLine = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 1, args.length));
		InventoryUtils.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			ArrayList<String> lore = new ArrayList<>();
			if (meta.hasLore()) {
				lore.addAll(Objects.requireNonNull(meta.getLore()));
			}
			lore.add(loreLine);
			meta.setLore(lore);
			hand.setItemMeta(meta);
		});
		player.sendMessage(Language.getColor("good") + "Added \"" + loreLine + Language.getColor("good") + "\"");
		return;
	}

	private void set(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(Language.getColor("bad") + "/lore set <number> <arguments>");
		}
		int line;
		try {
			line = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/lore set <number> <arguments>");
			return;
		}

		InventoryUtils.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			if (!meta.hasLore()) {
				player.sendMessage(Language.getColor("bad") + "Item has no lore to set!");
				return;
			}
			ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
			if (lore.size() < line) {
				player.sendMessage(Language.getColor("bad") + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return;
			}
			String added = ChatColor.translateAlternateColorCodes('&', TextUtils.join(args, ' ', 2, args.length));
			String removed = lore.set(line - 1, added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage(Language.getColor("good") + "Replaced \"" + removed + Language.getColor("good")
					+ "\" with \"" + added + Language.getColor("good") + "\" at " + line + "!");
		});

		return;
	}

	private void insert(Player player, ItemStack hand, String[] args) {
		if (args.length < 3) {
			player.sendMessage(Language.getColor("bad") + "/lore insert <number> <arguments>");
		}
		int line;
		try {
			line = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			player.sendMessage(Language.getColor("bad") + "/lore insert <number> <arguments>");
			return;
		}

		InventoryUtils.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			if (!meta.hasLore()) {
				player.sendMessage(Language.getColor("bad") + "Item has no lore to set!");
				return;
			}
			ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
			if (lore.size() < line) {
				player.sendMessage(Language.getColor("bad") + "Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage(Language.getColor("bad") + "Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return;
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
		});

		return;
	}
}
