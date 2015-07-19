package co.sblock.commands.utility;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.StringUtil;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.effects.Effects;
import co.sblock.utilities.general.Roman;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for applying an Effect to an item.
 * 
 * @author Jikoo
 */
public class EffectsCommand extends SblockCommand {

	public EffectsCommand() {
		super("effects");
		this.setDescription("Effects! Similar to /enchant.");
		this.setUsage("/effects <type> [level]");
		this.setAliases("fx");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}

		if (args.length < 1) {
			return false;
		}

		int level;
		if (args.length > 1) {
			try {
				level = Integer.valueOf(args[1]);
			} catch (NumberFormatException e) {
				sender.sendMessage(Color.BAD + "Effect level must be a positive integer!");
				return true;
			}
			level = level < 0 ? 0 : level;
		} else {
			level = 1;
		}

		Player player = (Player) sender;
		ItemStack hand = player.getItemInHand();
		if (hand == null || hand.getType() == Material.AIR) {
			sender.sendMessage(Color.BAD + "You need an item in hand to use this command!");
			return true;
		}

		ItemMeta meta;
		if (!hand.hasItemMeta() && hand.getItemMeta() == null) {
			meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
			if (meta == null) {
				sender.sendMessage(Color.BAD + "This item does not support meta.");
				return true;
			}
		} else {
			meta = hand.getItemMeta();
		}

		String loreString = new StringBuilder().append(ChatColor.GRAY).append(args[0]).append(' ')
				.append(Roman.fromInt(level)).toString();

		if (Effects.getInstance().getEffectFromLore(loreString, true) == null) {
			sender.sendMessage(Color.BAD + "Invalid effect! Try tab completing.");
			return true;
		}

		meta.setLore(Effects.getInstance().organizeEffectLore(meta.getLore(), true, true, loreString));
		hand.setItemMeta(meta);
		sender.sendMessage(Color.GOOD + "Added " + loreString);
		return false;
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
		for (String effectName : Effects.getInstance().getAllEffectNames()) {
			if (StringUtil.startsWithIgnoreCase(effectName, args[0])) {
				matches.add(effectName);
			}
		}
		return matches;
	}
}
