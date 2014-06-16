package co.sblock.effects;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.module.CommandDenial;
import co.sblock.module.CommandDescription;
import co.sblock.module.CommandListener;
import co.sblock.module.CommandPermission;
import co.sblock.module.CommandUsage;
import co.sblock.module.SblockCommand;

public class EffectsCommandListener implements CommandListener {

	@CommandDenial
	@CommandDescription("Root of all SburbEffects commands")
	@CommandPermission("group.horrorterror")
	@CommandUsage("/se <getlore/setlore/clearlore/applyeffects/verbose> <lore>")
	@SblockCommand
	public boolean se(CommandSender sender, String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		Player p = (Player) sender;
		ArrayList<String> lore = new ArrayList<String>();
		if (args[0].equalsIgnoreCase("getlore")) {
			if (p.getItemInHand().getItemMeta().hasLore()) {
				lore = (ArrayList<String>) p.getItemInHand().getItemMeta().getLore();
				p.sendMessage(lore.toString());
				return true;
			}
			p.sendMessage("Item has no lore!");
			return true;
		} else if (args[0].equalsIgnoreCase("setlore") && args[1] != null) {
			ItemMeta i = p.getItemInHand().getItemMeta();
			if (i.hasLore()) {
				lore.addAll(i.getLore());
			}
			lore.add(args[1]);
			i.setLore(lore);
			p.getItemInHand().setItemMeta(i);
			p.sendMessage("Lore added!");
			return true;
		} else if (args[0].equalsIgnoreCase("clearlore")) {
			ItemMeta i = p.getItemInHand().getItemMeta();
			i.setLore(null);
			p.sendMessage("Lore cleared!");
			return true;
//		} else if (args[0].equalsIgnoreCase("applyeffects") && !(args[1].equals(null))) {
//			Player target = Bukkit.getServer().getPlayer(args[1]);
//			ArrayList<String> playerLore = SblockEffects.getEffects().getEffectManager().passiveScan(target);
//			p.sendMessage(target.getName() + playerLore);
//			EffectManager.applyPassiveEffects(SblockUser.getUser(p.getName()));
//			return true;
		} else if (args[0].equalsIgnoreCase("verbose")) {
			SblockEffects.verbose = SblockEffects.verbose?false:true;
			p.sendMessage("Verbose mode = " + SblockEffects.verbose);
			return true;
		} else if (args[0].equalsIgnoreCase("list")) {
			sender.sendMessage(ChatColor.GREEN + "Active Effects:\n" +
					ActiveEffect.values().toString() + "\nPassive Effects:\n" + 
					PassiveEffect.values().toString());
			return true;
		}
		return false;
	}
}
