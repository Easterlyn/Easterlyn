package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@CommandAlias("me")
public class ShowItemCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("show|showitem")
	@Description("{@@chat.commands.me.show.description}")
	@CommandPermission("easterlyn.command.show")
	public void showItem(@Flags(CoreContexts.SELF) User sender, @Flags(CoreContexts.ONLINE) Channel channel) {
		Player player = sender.getPlayer();
		if (player == null) {
			core.getLocaleManager().sendMessage(getCurrentCommandIssuer().getIssuer(), "chat.commands.me.show.error.no_player");
			return;
		}

		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getItemMeta() == null || (!hand.getItemMeta().hasDisplayName() && hand.getEnchantments().isEmpty())) {
			core.getLocaleManager().sendMessage(player, "chat.commands.me.show.error.not_special");
			return;
		}

		new UserChatEvent(sender, channel, "shows off {ITEM:" + player.getInventory().getHeldItemSlot() + "}", true).send();
	}

}
