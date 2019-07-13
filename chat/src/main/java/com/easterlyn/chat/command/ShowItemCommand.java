package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.user.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("me")
public class ShowItemCommand extends BaseCommand {

	@CommandAlias("show|showitem")
	@Description("Show off for your friends!")
	@CommandPermission("easterlyn.command.show")
	public void showItem(@Flags("self") User sender, @Flags("listening") Channel channel) {
		Player player = sender.getPlayer();
		if (player == null) {
			sender.sendMessage("Player data not loaded!");
			return;
		}

		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getItemMeta() == null || (!hand.getItemMeta().hasDisplayName() && hand.getEnchantments().isEmpty())) {
			sender.sendMessage("Item to show off must be named or enchanted. Your main hand is neither.");
			return;
		}

		new UserChatEvent(sender, channel, "shows off {ITEM:" + player.getInventory().getHeldItemSlot() + "}", true).send();
	}

}
