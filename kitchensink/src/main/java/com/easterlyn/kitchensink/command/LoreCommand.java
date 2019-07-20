package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.GenericUtil;
import java.util.ArrayList;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

@CommandAlias("lore")
@Description("Manipulate lore and other item metadata.")
@CommandPermission("easterlyn.command.lore")
@CommandRank(UserRank.MODERATOR)
public class LoreCommand extends BaseCommand {

	@Subcommand("clearmeta")
	@Description("Clear ALL item meta.")
	@Syntax("/lore clearmeta")
	@CommandCompletion("@none")
	public void clearMeta(@Flags(CoreContexts.SELF) Player player) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		hand.setItemMeta(null);

		player.sendMessage("Reset meta!");
	}

	@CommandAlias("skull")
	@Subcommand("owner")
	@Description("Set a player head's owner.")
	@Syntax("/lore owner <player>")
	@CommandCompletion("@player")
	public void owner(@Flags(CoreContexts.SELF) Player player, @Single String owner) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if ("skull".equals(getExecCommandLabel())) {
			if (hand.getType() != Material.PLAYER_HEAD) {
				hand.setType(Material.PLAYER_HEAD);
			}
		} else {
			if (invalidState(player, hand)) {
				return;
			}

			if (hand.getType() != Material.PLAYER_HEAD) {
				player.sendMessage("You must be holding a player head to use this command.");
				return;
			}
		}

		GenericUtil.consumeAs(SkullMeta.class, hand.getItemMeta(), skullMeta -> {
			//noinspection deprecation // No alternative API
			skullMeta.setOwningPlayer(owner == null ? null : Bukkit.getOfflinePlayer(owner));
			hand.setItemMeta(skullMeta);
		});
		if (owner != null) {
			player.sendMessage("Owner set to " + owner);
		} else {
			player.sendMessage("Owner deleted!");
		}
	}

	@Subcommand("author")
	@Description("Set a book's author.")
	@Syntax("/lore author <author name>")
	@CommandCompletion("@player")
	public void author(@Flags(CoreContexts.SELF) Player player, String args) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
			player.sendMessage("You must be holding a writable book to set author.");
			return;
		}
		String author = args == null ? null : ChatColor.translateAlternateColorCodes('&', args);
		GenericUtil.consumeAs(BookMeta.class, hand.getItemMeta(), bookMeta -> {
			bookMeta.setAuthor(author);
			hand.setItemMeta(bookMeta);
		});
		if (author != null) {
			player.sendMessage( "Author set to " + author);
		} else {
			player.sendMessage("Author deleted!");
		}
	}

	@Subcommand("title")
	@Description("Set a book's title.")
	@Syntax("/lore title <sample text>")
	@CommandCompletion("Sample Text")
	public void title(@Flags(CoreContexts.SELF) Player player, String args) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		if (hand.getType() != Material.WRITTEN_BOOK && hand.getType() != Material.WRITABLE_BOOK) {
			player.sendMessage("You must be holding a writable book to set title.");
			return;
		}
		String title = args == null ? null : ChatColor.translateAlternateColorCodes('&', args);
		GenericUtil.consumeAs(BookMeta.class, hand.getItemMeta(), bookMeta -> {
			bookMeta.setTitle(title);
			hand.setItemMeta(bookMeta);
		});
		if (title != null) {
			player.sendMessage("Title set to " + title);
		} else {
			player.sendMessage("Title deleted!");
		}
	}

	@Subcommand("name")
	@Description("Set an item's name.")
	@Syntax("/lore name <sample text>")
	@CommandCompletion("Sample Text")
	public void name(@Flags(CoreContexts.SELF) Player player, String args) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		String name = args == null ? null : ChatColor.translateAlternateColorCodes('&', args);
		GenericUtil.consumeAs(ItemMeta.class, hand.getItemMeta(), bookMeta -> {
			bookMeta.setDisplayName(name);
			hand.setItemMeta(bookMeta);
		});
		if (name != null) {
			player.sendMessage("Name set to " + name);
		} else {
			player.sendMessage("Name deleted!");
		}
	}

	@Subcommand("delete owner")
	@Description("Remove a skull's owner")
	@Syntax("/lore delete owner")
	@CommandCompletion("@none")
	public void deleteOwner(@Flags(CoreContexts.SELF) Player player) {
		owner(player, null);
	}

	@Subcommand("delete author")
	@Description("Remove a book's author.")
	@Syntax("/lore delete author")
	@CommandCompletion("@none")
	public void deleteAuthor(@Flags(CoreContexts.SELF) Player player) {
		author(player, null);
	}

	@Subcommand("delete title")
	@Description("Remove a book's title.")
	@Syntax("/lore delete title")
	@CommandCompletion("@none")
	public void deleteTitle(@Flags(CoreContexts.SELF) Player player) {
		title(player, null);
	}

	@Subcommand("delete name")
	@Description("Remove an item's name.")
	@Syntax("/lore delete name")
	@CommandCompletion("@none")
	public void deleteName(@Flags(CoreContexts.SELF) Player player) {
		name(player, null);
	}

	@Subcommand("delete")
	@Description("Delete a line of lore.")
	@Syntax("/lore delete <line number>")
	@CommandCompletion("@integer") // TODO range for lore
	public void delete(@Flags(CoreContexts.SELF) Player player, int line) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		GenericUtil.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			if (!meta.hasLore()) {
				player.sendMessage("Item has no lore!");
				return;
			}
			ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
			if (lore.size() < line) {
				player.sendMessage("Item only has " + lore.size() + " lines, cannot delete " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage("Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return;
			}

			String removed = lore.remove(line - 1);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage("Deleted \"" + removed + org.bukkit.ChatColor.WHITE + "\" from line " + line + "!");
		});
	}

	@Subcommand("add")
	@Description("Add a line of lore.")
	@Syntax("/lore add <text>")
	@CommandCompletion("Sample Text")
	public void add(@Flags(CoreContexts.SELF) Player player, String text) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		String loreLine = ChatColor.translateAlternateColorCodes('&', text);
		GenericUtil.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			ArrayList<String> lore = new ArrayList<>();
			if (meta.hasLore()) {
				lore.addAll(Objects.requireNonNull(meta.getLore()));
			}
			lore.add(loreLine);
			meta.setLore(lore);
			hand.setItemMeta(meta);
		});
		player.sendMessage("Added \"" + loreLine + ChatColor.WHITE + "\"");
	}

	@Subcommand("set")
	@Description("Set a line of lore.")
	@Syntax("/lore set <line number> <text>")
	@CommandCompletion("@integer Sample Text")
	public void set(@Flags(CoreContexts.SELF) Player player, int line, String text) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		GenericUtil.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			if (!meta.hasLore()) {
				player.sendMessage("Item has no lore to set!");
				return;
			}
			ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
			if (lore.size() < line) {
				player.sendMessage("Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage("Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return;
			}
			String added = ChatColor.translateAlternateColorCodes('&', text);
			String removed = lore.set(line - 1, added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage("Replaced \"" + removed + ChatColor.WHITE + "\" with \"" + added + ChatColor.WHITE
					+ "\" at " + line + "!");
		});
	}

	@Subcommand("insert")
	@Description("Insert a line of lore.")
	@Syntax("/lore insert <line number> <text>")
	@CommandCompletion("@integer Sample Text")
	private void insert(Player player, int line, String text) {
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (invalidState(player, hand)) {
			return;
		}

		GenericUtil.consumeAs(ItemMeta.class, hand.getItemMeta(), meta -> {
			if (!meta.hasLore()) {
				player.sendMessage("Item has no lore to set!");
				return;
			}
			ArrayList<String> lore = new ArrayList<>(Objects.requireNonNull(meta.getLore()));
			if (lore.size() < line) {
				player.sendMessage("Item only has " + lore.size() + " lines, cannot set " + line + "!");
				return;
			}
			if (line < 1) {
				player.sendMessage("Index must be between 1 and " + lore.size() + "! " + line + " is invalid.");
				return;
			}
			String added = ChatColor.translateAlternateColorCodes('&', text);
			lore.add(line - 1, added);
			meta.setLore(lore);
			hand.setItemMeta(meta);
			player.sendMessage("Inserted \"" + added + ChatColor.WHITE + "\" at " + line + "!");
		});
	}

	private boolean invalidState(Player player, ItemStack itemStack) {
		if (itemStack.getType() == Material.AIR) {
			player.sendMessage("You need an item in hand to use this command!");
			return true;
		}
		if (!itemStack.hasItemMeta() && itemStack.getItemMeta() == null) {
			player.sendMessage("Item does not support meta.");
			return true;
		}
		return false;
	}

}
