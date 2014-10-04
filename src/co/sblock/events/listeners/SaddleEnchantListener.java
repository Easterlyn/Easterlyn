package co.sblock.events.listeners;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SaddleEnchantListener implements Listener
{

    @EventHandler
    public void onClick(InventoryClickEvent event)
    {
        Inventory toTest = event.getInventory();
        
        if (toTest.getType().equals(InventoryType.ANVIL))
        {
        	int playerClickLocation = event.getRawSlot();
        	
        	if (playerClickLocation == 1 || playerClickLocation == 0)
        	{
        		doCombine(toTest);
        	}
        }
    }
    
    private void doCombine(Inventory toTest)
    {
    	ItemStack firstSlot = toTest.getItem(0);
        ItemStack secondSlot = toTest.getItem(1);
        
        if (firstSlot == null || secondSlot == null)
        {
        	return;
        }
        
        ItemStack maybeSaddle = null;
        if (firstSlot.getType() == Material.ENCHANTED_BOOK &&
                secondSlot.getType() == Material.SADDLE)
        {
        	maybeSaddle = tryCombineBookSaddle(firstSlot, secondSlot);
        }
        
        if (firstSlot.getType() == Material.ENCHANTED_BOOK &&
                secondSlot.getType() == Material.SADDLE)
        {
        	maybeSaddle = tryCombineBookSaddle(secondSlot, firstSlot);
        }
        
        if (maybeSaddle != null)
        {
        	toTest.setItem(2, maybeSaddle);
        }
    }
    
    private ItemStack tryCombineBookSaddle(ItemStack book, ItemStack saddle)
    {
    	int fireAspectLevel = book.getEnchantmentLevel(Enchantment.FIRE_ASPECT);
    	
    	if (fireAspectLevel > 0)
    	{
    		ItemStack blazingSaddle = new ItemStack(Material.SADDLE, 1);
    		blazingSaddle.addEnchantment(Enchantment.FIRE_ASPECT, fireAspectLevel);
    		return blazingSaddle;
    	}
    	
    	return null;
    }
}
