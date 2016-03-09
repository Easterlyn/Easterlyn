package co.sblock.commands.info;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.captcha.CruxiteDowel;
import co.sblock.commands.SblockCommand;
import co.sblock.effects.Effects;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for printing information about an item.
 * 
 * @author Jikoo
 */
public class ItemInformationCommand extends SblockCommand {

	public ItemInformationCommand(Sblock plugin) {
		super(plugin, "iteminfo");
		this.setDescription("Serializes item in main hand and prints the result.");
		this.setUsage("/iteminfo");
		this.setPermissionLevel("felt");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		ItemStack hand = ((Player) sender).getInventory().getItemInMainHand();
		if (hand == null) {
			sender.sendMessage("NULL");
			return true;
		}
		sender.sendMessage(ChatColor.stripColor(hand.toString()));
		Sblock plugin = (Sblock) getPlugin();
		sender.sendMessage("Hash: " + plugin.getModule(Captcha.class).calculateHashFor(hand));
		sender.sendMessage("Grist: " + CruxiteDowel.expCost(plugin.getModule(Effects.class), hand));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
