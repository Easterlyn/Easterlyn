package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import com.easterlyn.EasterlynCaptchas;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.util.EconomyUtil;
import com.easterlyn.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemInformationCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;
	@Dependency
	EasterlynCaptchas captchas;

	@CommandAlias("iteminfo")
	@Description("{@@sink.module.iteminfo.description}")
	@CommandPermission("easterlyn.command.iteminfo")
	public void itemInfo(@Flags(CoreContexts.SELF) Player player) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getType() == Material.AIR) {
			core.getLocaleManager().sendMessage(player, "core.common.no_item");
			return;
		}

		BaseComponent component = StringUtil.getItemComponent(hand);
		component.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, StringUtil.getItemText(hand)));
		component = new TextComponent(component, new TextComponent(": "), new TextComponent(ChatColor.stripColor(hand.toString())));
		player.sendMessage(component);
		player.sendMessage("Hash: " + captchas.calculateHashForItem(hand));
		player.sendMessage("Mana: " + EconomyUtil.getWorth(hand));
	}

}
