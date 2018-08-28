package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.ManaMappings;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.effects.Effects;
import com.easterlyn.utilities.Experience;
import com.easterlyn.utilities.InventoryUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * EasterlynCommand for getting information about mana costs and totals.
 *
 * @author Jikoo
 */
public class ManaCommand extends EasterlynCommand {

	private final Captcha captcha;
	private final Effects effects;
	private final DecimalFormat format;

	public ManaCommand(Easterlyn plugin) {
		super(plugin, "mana");
		this.setAliases("grist");
		this.captcha = plugin.getModule(Captcha.class);
		this.effects = plugin.getModule(Effects.class);
		this.format = new DecimalFormat("#,###,###,###.###");
		this.format.setRoundingMode(RoundingMode.CEILING);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 1) {
			return false;
		}
		try {
			if (args[0].length() > 1) {
				char lastChar = args[0].charAt(args[0].length() - 1);
				if (lastChar == 'L' || lastChar == 'l') {
					int level = Integer.parseInt(args[0].substring(0, args[0].length() - 1));
					sender.sendMessage(this.getLang().getValue("command.mana.level")
							.replace("{LEVEL}", this.format.format(level))
							.replace("{EXP}", this.format.format(Experience.getExpFromLevel(level))));
					return true;
				}
			}
			int mana = Integer.parseInt(args[0]);
			sender.sendMessage(this.getLang().getValue("command.mana.exp")
					.replace("{LEVEL}", this.format.format(Experience.getLevelFromExp(mana)))
					.replace("{EXP}", this.format.format(mana)));
			return true;
		} catch (NumberFormatException e) {
			if (!args[0].equalsIgnoreCase("cost") && !args[0].equalsIgnoreCase("current")) {
				return false;
			}
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("/mana <(exp)|(level)L>");
			return true;
		}
		Player player = (Player) sender;
		if (args[0].equalsIgnoreCase("current")) {
			sender.sendMessage(this.getLang().getValue("command.mana.current")
					.replace("{EXP}", this.format.format(Experience.getExp(player))));
			return true;
		}
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (Captcha.isUsedCaptcha(hand)) {
			hand = captcha.captchaToItem(hand);
		}
		if (hand == null || hand.getType() == Material.AIR) {
			sender.sendMessage(this.getLang().getValue("command.mana.nothing"));
			return true;
		}
		double exp = ManaMappings.expCost(effects, hand);
		if (exp == Double.MAX_VALUE) {
			sender.sendMessage(this.getLang().getValue("command.mana.expensive")
					.replace("{ITEM}", InventoryUtils.getItemName(hand)));
		} else {
			StringBuilder itemName = new StringBuilder(InventoryUtils.getItemName(hand));
			if (hand.getAmount() > 1) {
				itemName.append('x').append(hand.getAmount());
			}
			sender.sendMessage(this.getLang().getValue("command.mana.cost")
					.replace("{ITEM}", itemName.toString())
					.replace("{EXP}", this.format.format(exp)));
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (!sender.hasPermission(this.getPermission()) || args.length != 1) {
			return ImmutableList.of();
		}
		args[0] = args[0].toLowerCase();
		List<String> completions = new ArrayList<>();
		if ("cost".startsWith(args[0])) {
			completions.add("cost");
		}
		if ("current".startsWith(args[0])) {
			completions.add("current");
		}
		return completions;
	}

}
