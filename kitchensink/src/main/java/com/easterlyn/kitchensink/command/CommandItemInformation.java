package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandItemInformation extends BaseCommand {

	@Dependency
	EasterlynCaptchas captchas;

	@CommandAlias("iteminfo")
	@Description("Get item serialization info.")
	@CommandPermission("easterlyn.command.iteminfo")
	@CommandRank(UserRank.MODERATOR)
	public void itemInfo(@Flags(CoreContexts.SELF) Player player) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getType() == Material.AIR) {
			player.sendMessage("You must be holding an item!");
			return;
		}

		BaseComponent component = StringUtil.getItemComponent(hand);
		component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, StringUtil.getItemText(hand)));
		component = new TextComponent(component, new TextComponent(": "), new TextComponent(ChatColor.stripColor(hand.toString())));
		player.sendMessage(component);
		player.sendMessage("Hash: " + captchas.calculateHashForItem(hand));
		player.sendMessage("Mana: " + EconomyUtil.getWorth(hand));
	}

}
