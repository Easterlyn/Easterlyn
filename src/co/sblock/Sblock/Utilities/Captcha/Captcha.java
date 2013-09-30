package co.sblock.Sblock.Utilities.Captcha;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.Utilities.Sblogger;

public class Captcha extends Module	{
	
	private CaptchaCommandListener clistener = new CaptchaCommandListener();
	private static char div = '\u007c';

	@Override
	protected void onEnable() {
		this.captchaCardRecipe();
		this.registerCommands(clistener);
	}

	@Override
	protected void onDisable() {
		
	}
	
	public static ItemStack itemToCaptcha(ItemStack item)	{
		ItemStack card = blankCaptchaCard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta iM = item.getItemMeta();
		String name = "";
		if(iM.hasDisplayName())	{
			name = iM.getDisplayName();
		} else	{
			name = item.getType().toString();
		}
		int id = item.getTypeId();
		//MaterialData data = item.getData();	//Do I need this? How do I get data values?
		short dur = item.getDurability();
		int stack = item.getAmount();
		Map<Enchantment, Integer> ench = item.getEnchantments();
		List<String> lore;
		if(iM.hasLore())	{
			lore = iM.getLore();
		} else	{
			lore = new ArrayList<String>();
		}
		String captcha = name + div + id + div + dur + div + stack + div + ench + div + lore;
		ArrayList<String> cardLore = new ArrayList<String>();
		cardLore.add(captcha);
		cardMeta.setDisplayName("Captchacard");
		cardMeta.setLore(cardLore);
		card.setItemMeta(cardMeta);
		//Name(Default or custom)
		//id:data
		//durability
		//Stack size
		//Enchantments
		//Other lore
		return card;		
	}
	
	public static ItemStack captchaToItem(ItemStack card)	{
		List<String> cardLore = card.getItemMeta().getLore();
		String temp = cardLore.get(0);
		Sblogger.info("DEBUG", temp);
		String[] captchaCode = temp.split("\\|");
		StringBuilder print = new StringBuilder();
		for(String s : captchaCode)	{
				print.append(s).append(" ");
		}
		Sblogger.info("DEBUG", print.toString());
		ItemStack item = new ItemStack(Material.getMaterial(Integer.parseInt(captchaCode[1])));
		ItemMeta iM = card.getItemMeta();
		if(!captchaCode[0].equalsIgnoreCase(item.getType().toString()))	{
			iM.setDisplayName(captchaCode[0]);
		}
		else	{
			iM.setDisplayName(null);
		}
		item.setDurability(Short.parseShort(captchaCode[2]));
		item.setAmount(Integer.parseInt(captchaCode[3]));
		//item.addEnchantments()									//Figure out how to convert ench back into a map
		//Map<Enchantment, Integer> ench = item.getEnchantments();
/*		List<String> lore;
		iM.setLore(lore)
		String div = "|";
		String captcha = name + div + id + div + dur + div + stack + div + ench + div + lore;
		ArrayList<String> cardLore = new ArrayList<String>();*/
		//cardLore.add(captcha);
		iM.setLore(null);	//temp
		item.setItemMeta(iM);
		return item;
	}
	
	private static ItemStack blankCaptchaCard()	{
		ItemStack iS = new ItemStack(Material.PAPER);
		ItemMeta iM = iS.getItemMeta();
		iM.setDisplayName("Captchacard");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Blank");
		iM.setLore(lore);
		iS.setItemMeta(iM);
		return iS;
	}
	
	private void captchaCardRecipe()	{
		ShapedRecipe recipe = new ShapedRecipe(blankCaptchaCard());
		recipe.shape("AA", "AA", "AA");
		recipe.setIngredient('A', Material.PAPER);
		Sblock.getInstance().getServer().addRecipe(recipe);
	}

}
