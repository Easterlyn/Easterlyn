package co.sblock.Sblock.SblockEffects;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

public class EffectsCommandListener implements CommandListener	{

	@SblockCommand(consoleFriendly = false, mergeLast = true)
	public boolean se(CommandSender sender, String text)	{
		String[] args = text.split(" ");
		Player p = (Player) sender;
		ArrayList<String> lore = new ArrayList<String>();
			if (sender.isOp())	{
				if(args[0].equalsIgnoreCase("getlore"))	{
					if(p.getItemInHand().getItemMeta().hasLore())	{
						lore = (ArrayList<String>) p.getItemInHand().getItemMeta().getLore();
						p.sendMessage(lore.toString());
						return true;
					}
					p.sendMessage("Item has no lore!");
					return true;
				}
				else if(args[0].equalsIgnoreCase("setlore") && !(args[1].equals(null)))	{
					ItemMeta i = p.getItemInHand().getItemMeta();
					if(i.hasLore())	{
						lore.addAll(i.getLore());
					}
					lore.add(args[1]);
					i.setLore(lore);
					p.getItemInHand().setItemMeta(i);
					return true;
				}
				else if(args[0].equalsIgnoreCase("clearlore"))	{
					ItemMeta i = p.getItemInHand().getItemMeta();
					i.setLore(null);
					return true;
				}
				else if(args[0].equalsIgnoreCase("applyeffects") && !(args[1].equals(null)))	{
					Player target = Bukkit.getServer().getPlayer(args[1]);
					//getLogger().info("Begin Scan");
					ArrayList<String> playerLore = EffectsModule.getInstance().getEffectManager().scan(target);
					//getLogger().info(target + "'s lore is " + playerLore);
					p.sendMessage(target.getName() + playerLore);
					//getLogger().info("Begin Application");
					EffectsModule.getInstance().getEffectManager().applyEffects(playerLore, target);
					return true;
				}
				else if(args[0].equalsIgnoreCase("verbose"))	{
					EffectsModule.verbose = EffectsModule.verbose?false:true;
					p.sendMessage("Verbose mode = " + EffectsModule.verbose);
					return true;
				}
			}	
		return false;
	}
}
