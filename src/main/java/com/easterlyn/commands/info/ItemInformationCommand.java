package com.easterlyn.commands.info;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.effects.Effects;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

/**
 * EasterlynCommand for printing information about an item.
 * 
 * @author Jikoo
 */
public class ItemInformationCommand extends EasterlynCommand {

	public ItemInformationCommand(Easterlyn plugin) {
		super(plugin, "iteminfo");
		this.setDescription("Serializes item in main hand and prints the result.");
		this.setPermissionLevel(UserRank.FELT);
		this.setUsage("/iteminfo");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR) {
			sender.sendMessage(getLang().getValue("command.general.needItemInHand"));
			return true;
		}
		sender.sendMessage(ChatColor.stripColor(hand.toString()));
		Easterlyn plugin = (Easterlyn) getPlugin();
		sender.sendMessage("Hash: " + plugin.getModule(Captcha.class).calculateHashFor(hand));
		sender.sendMessage("Mana: " + CruxiteDowel.expCost(plugin.getModule(Effects.class), hand));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
