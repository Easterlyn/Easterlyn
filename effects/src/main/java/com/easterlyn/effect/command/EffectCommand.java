package com.easterlyn.effect.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Private;
import com.easterlyn.EasterlynEffects;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.NumberUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
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
	public void applyEffect(@Flags(CoreContexts.SELF) Player player, int level, String effectName) {
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand.getType() == Material.AIR) {
			player.sendMessage("You must be holding an item.");
			return;
		}

		ItemMeta meta = hand.getItemMeta();

		if (meta == null) {
			player.sendMessage("You must be holding an item.");
			return;
		}

		String loreString = ChatColor.GRAY + effectName + ' ' + NumberUtil.romanFromInt(level);

		if (effects.getEffectFromLore(loreString, true) == null) {
			player.sendMessage("Invalid effect " + effectName);
			return;
		}

		List<String> lore = meta.getLore();
		if (lore == null) {
			lore = new ArrayList<>();
		}

		meta.setLore(effects.organizeEffectLore(lore, true, true, false, loreString));
		hand.setItemMeta(meta);
		player.sendMessage("Added " + loreString);
	}

}
