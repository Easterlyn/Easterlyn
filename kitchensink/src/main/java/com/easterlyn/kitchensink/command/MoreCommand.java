package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("more")
@Description("Add to your stack in hand.")
@CommandPermission("easterlyn.command.more")
public class MoreCommand extends BaseCommand {

	@CommandAlias("more")
	@Default
	@Description("Add to your stack in hand.")
	@Syntax("/more [count] with item in hand")
	@CommandCompletion("@none")
	public void more(@Flags(CoreContexts.SELF) Player player) {
		more(player, 0);
	}

	@CommandAlias("more")
	@Description("Add to your stack in hand.")
	@Syntax("/more [count] with item in hand")
	@CommandCompletion("@integer")
	public void more(@Flags(CoreContexts.SELF) Player player, int count) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getType() == Material.AIR) {
			getCommandHelp().showHelp();
			return;
		}
		if (count < 1 || hand.getAmount() + count > 64) {
			hand.setAmount(64);
		} else {
			hand.setAmount(hand.getAmount() + count);
		}
		player.sendMessage("Item in hand amount set to " + hand.getAmount());
	}

}
