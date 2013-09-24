package co.sblock.Sblock.SblockEffects;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import co.sblock.Sblock.Sblock;

public class EffectManager {

	private final JavaPlugin plugin;
	
	public EffectManager() {
		plugin = Sblock.getInstance();
	}

	public ArrayList<String> scan(Player p)	{
		PlayerInventory pInv = p.getInventory();
		ItemStack[] iS = pInv.getContents();
		ItemStack[] iSA = pInv.getArmorContents();
		ArrayList<String> playerLore = new ArrayList<String>();
		ArrayList<String> tempLore = new ArrayList<String>();
		for(ItemStack i : iS)	{	//Inventory
			try {
				//System.out.println(i.getType().toString());
				if(i.hasItemMeta() && i.getItemMeta().hasLore())	{
					tempLore.addAll(i.getItemMeta().getLore());
					playerLore.addAll(i.getItemMeta().getLore());
				}
			} catch (NullPointerException e) {	//Catches empty inventory slots
			}
		}
		for(ItemStack i : iSA)	{	//Armor
			try {
				//System.out.println(i.getType().toString());
				if(i.hasItemMeta() && i.getItemMeta().hasLore())	{
					tempLore.addAll(i.getItemMeta().getLore());
					playerLore.addAll(i.getItemMeta().getLore());
				}
			} catch (NullPointerException e) {	//Catches empty armor slots
			}
		}
		for(String s: tempLore)	{
			if(!PassiveEffect.isValidEffect(s))	{
				playerLore.remove(playerLore.indexOf(s));
			}
		}
		if(EffectsModule.verbose && playerLore.size() > 0){
			plugin.getLogger().info(p.getName() + playerLore);
		}
		return playerLore;
	}
	
	
	public ArrayList<String> activeScan(Player p)	{
		ArrayList<String> lore = new ArrayList<String>();
		ArrayList<String> tempLore = new ArrayList<String>();
		ItemStack iS = p.getItemInHand();
			try {
				if(iS.hasItemMeta() && iS.getItemMeta().hasLore())	{
					lore.addAll(iS.getItemMeta().getLore());
					tempLore.addAll(iS.getItemMeta().getLore());
				}
			} catch (NullPointerException e) {	//Catches empty inventory slots
			}
			for(String s: tempLore)	{
				if(!ActiveEffect.isValidEffect(s))	{
					lore.remove(lore.indexOf(s));
				}
			}
		return lore;
	}
	
	public void applyActiveRightClickEffects(ArrayList<String> effects, Player p){
		ActiveEffect aE;
		for(String lore : effects)	{
			if(ActiveEffect.isValidEffect(lore))	{
				try {
					aE = ActiveEffect.valueOf(lore);
					aE.getRightClickEffect(p);
				} catch (IllegalArgumentException e) {
					for(ActiveEffect act : ActiveEffect.values())	{
						if(act.getLoreText().equalsIgnoreCase(lore))	{
							act.getRightClickEffect(p);
						}
					}
				}	
			}			
		}
	}
	public void applyActiveDamageEffects(ArrayList<String> effects, Player p, Player target){
		ActiveEffect aE;
		for(String lore : effects)	{
			if(ActiveEffect.isValidEffect(lore))	{
				try {
					aE = ActiveEffect.valueOf(lore);
					aE.getDamageEffect(p, target);
				} catch (IllegalArgumentException e) {
					for(ActiveEffect act : ActiveEffect.values())	{
						if(act.getLoreText().equalsIgnoreCase(lore))	{
							act.getDamageEffect(p, target);
						}
					}
				}	
			}			
		}
	}
	
	public void applyPassiveEffects(ArrayList<String> effects, Player p)	{
		PassiveEffect pE;
		for(String lore : effects)	{
			if(PassiveEffect.isValidEffect(lore))	{
				try {
					pE = PassiveEffect.valueOf(lore);
					pE.getEffect(p);
				} catch (IllegalArgumentException e) {
					for(PassiveEffect pass : PassiveEffect.values())	{
						if(pass.getLoreText().equalsIgnoreCase(lore))	{
							pass.getEffect(p);
						}
					}
				}	
			}			
		}
	}
}
