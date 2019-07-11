package com.easterlyn.effect.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MessageType;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import com.easterlyn.EasterlynEffects;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.NumberUtil;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@CommandAlias("effects|fx")
@Description("Manage effects!")
@CommandPermission("easterlyn.command.effects")
@CommandRank(UserRank.ADMIN)
public class EffectCommand extends BaseCommand {

	@Dependency
	private EasterlynEffects effects;

	@Default
	@Private
	public void applyEffect(BukkitCommandIssuer issuer, int level, String effectName) {
		if (!issuer.isPlayer()) {
			issuer.sendMessage(MessageType.ERROR, MessageKeys.NOT_ALLOWED_ON_CONSOLE);
			return;
		}

		Player player = issuer.getPlayer();
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getType() == Material.AIR) {
			issuer.sendMessage("You must have an item in hand to apply effects to!");
			return;
		}

		ItemMeta meta;
		if (!hand.hasItemMeta()) {
			meta = Bukkit.getItemFactory().getItemMeta(hand.getType());
		} else {
			meta = hand.getItemMeta();
		}

		if (meta == null) {
			issuer.sendMessage("Item does not support meta.");
			return;
		}

		String loreString = ChatColor.GRAY + effectName + ' ' + NumberUtil.romanFromInt(level);

		if (effects.getEffectFromLore(loreString, true) == null) {
			issuer.sendMessage("Invalid effect " + effectName);
			return;
		}

		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}

		meta.setLore(effects.organizeEffectLore(lore, true, true, false, loreString));
		hand.setItemMeta(meta);
		issuer.sendMessage("Added " + loreString);
	}

}
