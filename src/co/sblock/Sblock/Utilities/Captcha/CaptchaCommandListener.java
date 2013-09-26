package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

public class CaptchaCommandListener implements CommandListener	{
	
	@SblockCommand(consoleFriendly = false)
	public boolean captcha(CommandSender sender)	{
		if(sender.isOp())	{
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			p.getInventory().remove(item);
			p.getInventory().addItem(Captcha.itemToCaptcha(item));
			return true;
		}
		return false;
	}
	
	@SblockCommand(consoleFriendly = false)
	public boolean uncaptcha(CommandSender sender)	{
		if(sender.isOp())	{
			Player p = (Player) sender;
			ItemStack item = p.getItemInHand();
			if(item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equalsIgnoreCase("captchacard"))	{
				p.getInventory().clear(p.getInventory().getHeldItemSlot());
				p.getInventory().addItem(Captcha.captchaToItem(item));
			}			
			return true;
		}
		return false;
	}

}
