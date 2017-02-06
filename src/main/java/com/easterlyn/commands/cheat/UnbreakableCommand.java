package com.easterlyn.commands.cheat;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.users.UserRank;

import com.google.common.collect.ImmutableList;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * A (mostly for laughs) EasterlynCommand for setting unbreakable flags on an item.
 * 
 * @author Jikoo
 */
public class UnbreakableCommand extends EasterlynCommand {

	public UnbreakableCommand(Easterlyn plugin) {
		super(plugin, "traindon'tstop");
		this.setDescription("No brakes on this abuse caboose.");
		this.setPermissionLevel(UserRank.DENIZEN);
		this.setPermissionMessage("SEND HELP, THE BRAKES DON'T WORK! PLEASE!");
		this.setUsage("/traindon'tstop [choochoo|oshitthecops]");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		ItemStack hand = player.getInventory().getItemInMainHand();
		if (hand == null || hand.getType() == Material.AIR || hand.getType().getMaxDurability() == 0) {
			player.sendMessage(Language.getColor("good") + "Toot toot!");
			return true;
		}
		if (args.length > 0 && args[0].equals("oshitthecops")) {
			if (!hand.hasItemMeta()) {
				player.sendMessage(Language.getColor("good") + "The caboose is secure. I repeat, the caboose is secure.");
				return true;
			}
			ItemMeta handMeta = hand.getItemMeta();
			handMeta.setUnbreakable(false);
			handMeta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			hand.setItemMeta(handMeta);
			player.sendMessage(Language.getColor("good") + "The caboose is secure. I repeat, the caboose is secure.");
			return true;
		}
		ItemMeta handMeta = hand.getItemMeta();
		handMeta.setUnbreakable(true);
		if (args.length > 0 && args[0].equals("choochoo")) {
			handMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
		}
		hand.setItemMeta(handMeta);
		player.sendMessage(Language.getColor("good") + "CHOO FRIGGIN' CHOO.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}

}
