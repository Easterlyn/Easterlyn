package com.easterlyn.commands.cheat;

import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommand for enchanting a held item.
 * 
 * @author Jikoo
 */
public class EnchantCommand extends EasterlynCommand {

	public EnchantCommand(Easterlyn plugin) {
		super(plugin, "enchant");
		this.setPermissionLevel(UserRank.ADMIN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}

		if (args.length < 2) {
			return false;
		}

		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();

		if (hand.getType() == Material.AIR) {
			return false;
		}

		Enchantment enchantment = Enchantment.getByName(args[0].toUpperCase());

		if (enchantment == null) {
			sender.sendMessage(getLang().getValue("command.general.invalidParameters").replace("{PARAMETER}", args[0]));
			return true;
		}

		int level;
		try {
			level = Integer.valueOf(args[1]);
		} catch (NumberFormatException e) {
			return false;
		}

		// Internally, enchantments are a short. Cap level here to prevent user confusion.
		if (level > Short.MAX_VALUE) {
			level = Short.MAX_VALUE;
		} else if (level < Short.MIN_VALUE) {
			level = Short.MIN_VALUE;
		}

		boolean flagKeepAsBook, flagForceLevel;
		if (args.length > 2 && args[2].length() > 1 && args[2].charAt(0) == '-') {
			flagKeepAsBook = args[2].indexOf('b') > 0;
			flagForceLevel = args[2].indexOf('l') > 0;
		} else {
			flagKeepAsBook = flagForceLevel = false;
		}

		// Unless flag is set, automatically convert books to enchanted books.
		if (hand.getType() == Material.BOOK && !flagKeepAsBook) {
			hand.setType(Material.ENCHANTED_BOOK);
		}

		// Unless flag is set, remove enchantment if level is under 1.
		if (level < 1 && !flagForceLevel) {
			removeEnchantment(hand, enchantment);
			sender.sendMessage(getLang().getValue("command.enchant.success.remove"));
		} else {
			setEnchantment(hand, enchantment, level);
			sender.sendMessage(getLang().getValue("command.enchant.success.add"));
		}
		return true;
	}

	private void removeEnchantment(ItemStack item, Enchantment enchantment) {
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof EnchantmentStorageMeta) {
			((EnchantmentStorageMeta) meta).removeStoredEnchant(enchantment);
		} else if (meta != null) {
			meta.removeEnchant(enchantment);
		}
		item.setItemMeta(meta);
	}

	private void setEnchantment(ItemStack item, Enchantment enchantment, int level) {
		ItemMeta meta = item.getItemMeta();
		if (meta instanceof EnchantmentStorageMeta) {
			((EnchantmentStorageMeta) meta).addStoredEnchant(enchantment, level, true);
		} else {
			meta.addEnchant(enchantment, level, true);
		}
		item.setItemMeta(meta);
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length > 2) {
			// Unable to use command or too many arguments
			return ImmutableList.of();
		}

		// Tab completing enchantment name
		if (args.length == 1) {
			List<String> matches = new ArrayList<>();
			String toMatch = args[0].toUpperCase();
			for (Enchantment enchantment : Enchantment.values()) {
				if (enchantment.getName().startsWith(toMatch)) {
					matches.add(enchantment.getName());
				}
			}
			return matches;
		}

		// Tab completing level, just offer 1
		return ImmutableList.of("1");
	}

}
