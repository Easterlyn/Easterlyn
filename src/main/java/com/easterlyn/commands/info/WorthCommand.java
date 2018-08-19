package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.effects.Effects;
import com.easterlyn.micromodules.VillagerAdjustment;
import com.easterlyn.utilities.InventoryUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * A command for evaluating the worth of an item based on its mana cost.
 *
 * @author Jikoo
 */
public class WorthCommand extends EasterlynCommand {

	private final Captcha captcha;
	private final Effects effects;
	private final DecimalFormat format;

	public WorthCommand(Easterlyn plugin) {
		super(plugin, "worth");
		this.setAliases("value", "cost");
		this.captcha = plugin.getModule(Captcha.class);
		this.effects = plugin.getModule(Effects.class);
		this.format = new DecimalFormat("#,###,###,###.###");
		this.format.setRoundingMode(RoundingMode.CEILING);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("/mana <(exp)|(level)L>");
			return true;
		}
		Player player = (Player) sender;
		ItemStack originalHand = player.getInventory().getItemInMainHand();
		ItemStack hand = originalHand.clone();

		int multiplier = 1;
		while (Captcha.isUsedCaptcha(hand)) {
			ItemStack newModInput = captcha.captchaToItem(hand);
			if (newModInput == null || hand.isSimilar(newModInput)) {
				// Broken captcha, don't infinitely loop.
				hand = new ItemStack(Material.AIR);
				break;
			}
			multiplier *= Math.max(1, Math.abs(hand.getAmount()));
			hand = newModInput;
		}
		if (hand.getType() == Material.AIR) {
			sender.sendMessage(this.getLang().getValue("command.worth.nothing"));
			return true;
		}
		double exp = CruxiteDowel.expCost(effects, hand);
		if (Double.MAX_VALUE / multiplier <= exp) {
			sender.sendMessage(this.getLang().getValue("command.worth.expensive")
					.replace("{ITEM}", InventoryUtils.getItemName(hand)));
		} else {
			exp *= multiplier;
			StringBuilder itemName = new StringBuilder(InventoryUtils.getItemName(hand));
			if (originalHand.getAmount() > 1) {
				itemName.append('x').append(originalHand.getAmount());
			}
			sender.sendMessage(this.getLang().getValue("command.worth.cost")
					.replace("{ITEM}", itemName.toString())
					.replace("{EXP}", this.format.format(exp))
					.replace("{OVERPRICED}", this.format.format(exp / VillagerAdjustment.OVERPRICED_RATE))
					.replace("{GOOD_PRICE}", this.format.format(exp / VillagerAdjustment.NORMAL_RATE))
					.replace("{UNDERPRICED}", this.format.format(exp / VillagerAdjustment.UNDERPRICED_RATE)));
		}
		return true;
	}

}
