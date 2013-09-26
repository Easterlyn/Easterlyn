package co.sblock.Sblock.Utilities.Captcha;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.Sblock.Utilities.Sblogger;

public class CaptchaEventListener implements Listener	{

	@SuppressWarnings("deprecation")
	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent event)	{
		Player p = (Player)event.getWhoClicked();
		if(event.getClickedInventory().getType() == InventoryType.CRAFTING || event.getClickedInventory().getType() == InventoryType.WORKBENCH)	{
			p.sendMessage("if1 passed");
			if(event.getAction() == InventoryAction.PLACE_ALL || event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_SOME || event.getAction() == InventoryAction.SWAP_WITH_CURSOR)	{
				p.sendMessage("if2 passed");
				ItemStack item = event.getCursor();
				Sblogger.info("DEBUGGLES", "Bite me Adam");
				p.sendMessage(item.getType().toString());
				if(item.hasItemMeta())	{
					ItemMeta iM = item.getItemMeta();
					if(iM.hasDisplayName() && iM.getDisplayName().equalsIgnoreCase("Captchacard"))	{
						p.sendMessage("if3 passed");
						if(iM.hasLore() && !iM.getLore().contains("Blank"))	{
							p.sendMessage("if4 passed");
							ItemStack output = Captcha.captchaToItem(item);
							event.getInventory().setItem(0, output);
							p.updateInventory();
						}
					}
				}
			}
		}
	}
	/* 12
	 * 34
	 * 
	 * 123
	 * 456
	 * 789
	 */
}
