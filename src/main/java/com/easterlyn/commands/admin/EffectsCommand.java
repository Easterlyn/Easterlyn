package com.easterlyn.commands.admin;

import java.util.ArrayList;
import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.effects.Effects;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.NumberUtils;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import net.md_5.bungee.api.ChatColor;

/**
 * EasterlynCommand for applying an Effect to an item.
 * 
 * @author Jikoo
 */
public class EffectsCommand extends EasterlynCommand {

	private final Effects effects;

	public EffectsCommand(Easterlyn plugin) {
		super(plugin, "effects");
		this.effects = plugin.getModule(Effects.class);
		this.setAliases("fx");
		this.setPermissionLevel(UserRank.DENIZEN);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}

		if (args.length < 1) {
			return false;
		}

		int level;
		String effectName;
		if (args.length > 1) {
			try {
				level = Integer.valueOf(args[args.length - 1]);
				level = level < 1 ? 1 : level;
				effectName = StringUtils.join(args, ' ', 0, args.length - 1);
			} catch (NumberFormatException e) {
				level = 1;
				effectName = StringUtils.join(args, ' ', 0, args.length);
			}
		} else {
			level = 1;
			effectName = StringUtils.join(args, ' ', 0, args.length);
		}

		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			sender.sendMessage(getLang().getValue("command.general.needItemInHand"));
			return true;
		}

		ItemMeta meta;
		if (!hand.hasItemMeta() && hand.getItemMeta() == null) {
			meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
			if (meta == null) {
				sender.sendMessage(getLang().getValue("command.general.noMetaSupport"));
				return true;
			}
		} else {
			meta = hand.getItemMeta();
		}

		String loreString = new StringBuilder().append(ChatColor.GRAY).append(effectName).append(' ')
				.append(NumberUtils.romanFromInt(level)).toString();

		if (effects.getEffectFromLore(loreString, true) == null) {
			sender.sendMessage(getLang().getValue("command.general.invalidParameters")
					.replace("{PARAMETER}", loreString));
			return true;
		}

		meta.setLore(effects.organizeEffectLore(meta.getLore(), true, true, false, loreString));
		hand.setItemMeta(meta);
		sender.sendMessage(Language.getColor("good") + "Added " + loreString);
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!(sender instanceof Player) || !sender.hasPermission(this.getPermission())
				|| args.length == 0 || args.length > 2) {
			return ImmutableList.of();
		}
		ArrayList<String> matches = new ArrayList<>();
		if (args.length == 2) {
			matches.add("1");
			return matches;
		}
		for (String effectName : effects.getAllEffectNames()) {
			if (StringUtil.startsWithIgnoreCase(effectName, args[0])) {
				matches.add(effectName);
			}
		}
		return matches;
	}
}
