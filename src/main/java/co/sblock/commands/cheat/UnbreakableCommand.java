package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Language;
import co.sblock.commands.SblockCommand;

/**
 * A (mostly for laughs) SblockCommand for setting unbreakable flags on an item.
 * 
 * @author Jikoo
 */
public class UnbreakableCommand extends SblockCommand {

	public UnbreakableCommand(Sblock plugin) {
		super(plugin, "traindon'tstop");
		this.setDescription("No brakes on this abuse caboose.");
		this.setUsage("/traindon'tstop [choochoo|oshitthecops]");
		this.setPermissionLevel("denizen");
		this.setPermissionMessage("SEND HELP, THE BRAKES DON'T WORK! PLEASE!");
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
			handMeta.spigot().setUnbreakable(false);
			handMeta.removeItemFlags(ItemFlag.HIDE_UNBREAKABLE);
			hand.setItemMeta(handMeta);
			player.sendMessage(Language.getColor("good") + "The caboose is secure. I repeat, the caboose is secure.");
			return true;
		}
		ItemMeta handMeta = hand.getItemMeta();
		handMeta.spigot().setUnbreakable(true);
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
