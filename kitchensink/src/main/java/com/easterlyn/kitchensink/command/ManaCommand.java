package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.ExperienceUtil;
import com.easterlyn.util.inventory.ItemUtil;
import java.text.DecimalFormat;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("mana")
@Description("Check your mana or get mana information!")
@CommandPermission("easterlyn.command.mana")
public class ManaCommand extends BaseCommand {

	@Dependency
	EasterlynCaptchas captchas;

	@Subcommand("current")
	@CommandCompletion("@none")
	@Syntax("/mana current")
	public void current(@Flags(CoreContexts.SELF) Player player) {
		player.sendMessage("Current mana: " + ExperienceUtil.getExp(player));
	}

	@CommandAlias("mana")
	@CommandCompletion("@integer")
	@Syntax("/mana <exp>")
	public void experience(long experience) {
		DecimalFormat format = new DecimalFormat("#,###,###,###.###");
		getCurrentCommandIssuer().sendMessage(experience + " mana is level " + format.format(ExperienceUtil.getLevelFromExp(experience)));
	}

	@CommandAlias("mana")
	@CommandCompletion("@integer")
	@Syntax("/mana <level>L")
	public void level(@Single String argument) {
		if (!argument.matches("\\d+[lL]")) {
			showSyntax(getCurrentCommandIssuer(), getLastCommandOperationContext().getRegisteredCommand());
			return;
		}
		int level = Integer.parseInt(argument.substring(0, argument.length() - 1));
		DecimalFormat format = new DecimalFormat("#,###,###,###.###");
		getCurrentCommandIssuer().sendMessage(level + " mana is level " + format.format(ExperienceUtil.getExpFromLevel(level)));
	}

	@Subcommand("cost")
	@CommandCompletion("@none")
	@Syntax("/mana cost")
	public void cost(@Flags(CoreContexts.SELF) Player player) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		while (EasterlynCaptchas.isUsedCaptcha(hand)) {
			ItemStack oldHand = hand;
			hand = captchas.getItemByCaptcha(hand);
			if (hand == null || hand.isSimilar(oldHand)) {
				hand = oldHand;
				break;
			} else {
				hand.setAmount(hand.getAmount() * oldHand.getAmount());
			}
		}
		if (hand.getType() == Material.AIR) {
			player.sendMessage("Nothing in life is free.");
			return;
		}

		double worth;
		try {
			worth = EconomyUtil.getWorth(hand);
		} catch (ArithmeticException e) {
			player.sendMessage(e.getMessage());
			return;
		}

		DecimalFormat format = new DecimalFormat("#,###,###,###.###");
		player.sendMessage(ItemUtil.getItemName(hand) + (hand.getAmount() > 1 ? "x" + hand.getAmount() : "")
				+ " costs " + format.format(worth) + " mana ("
				+ format.format(ExperienceUtil.getLevelFromExp((long) worth)) + "L) to replicate.");
	}

}
