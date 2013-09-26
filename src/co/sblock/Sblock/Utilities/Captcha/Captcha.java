package co.sblock.Sblock.Utilities.Captcha;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.Sblock.Module;
import co.sblock.Sblock.Sblock;

public class Captcha extends Module	{
	
	private CaptchaCommandListener clistener = new CaptchaCommandListener();
	@Override
	protected void onEnable() {
		this.captchaCardRecipe();
		this.registerCommands(clistener);
	}

	@Override
	protected void onDisable() {
		
	}
	
	public static ItemStack itemToCaptcha(ItemStack item) {
		ItemStack card = blankCaptchaCard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta iM = item.getItemMeta();
		String name;
		if (iM.hasDisplayName())	{
			name = iM.getDisplayName();
		} else {
			name = item.getType().toString();
		}
		ArrayList<String> cardLore = new ArrayList<String>();
		cardLore.add(name);
		cardLore.add(String.valueOf(item.getTypeId()));
		cardLore.add(String.valueOf(item.getDurability()));
		cardLore.add(String.valueOf(item.getAmount()));
		// Enchantments
		StringBuilder enchants = new StringBuilder();
		if (iM.hasEnchants()) {
			for (Entry<Enchantment, Integer> e : iM.getEnchants().entrySet()) {
				enchants.append(":").append(e.getKey().getId()).append(";").append(e.getValue());
			}
		}
		if (enchants.length() > 0) {
			cardLore.add(enchants.toString());
		}
		// Lore
		if (iM.hasLore()) {
			for (String s : iM.getLore()) {
				cardLore.add(">" + s);
			}
		}
		cardMeta.setDisplayName("Captchacard");
		cardMeta.setLore(cardLore);
		card.setItemMeta(cardMeta);
		return card;
	}
	
	public static ItemStack captchaToItem(ItemStack card) {
		List<String> lore = card.getItemMeta().getLore();
		// Item: ID, quantity, data (damage)
		ItemStack is = new ItemStack(Material.getMaterial(Integer.valueOf(lore.get(1))),
				Integer.valueOf(lore.get(3)), Short.valueOf(lore.get(2)));

		ItemMeta im = is.getItemMeta();
		if (!lore.get(0).equals(is.getType().toString())) {
			im.setDisplayName(lore.get(0));
		} else {
			im.setDisplayName(null);
		}
		List<String> itemLore = new ArrayList<String>();
		for (String s : lore) {
			if (s.startsWith(":")) {
				// Enchantments line format
				String[] enchs = s.substring(1).split(":");
				for (String s1 : enchs) {
					String[] ench = s1.split(";");
					im.addEnchant(Enchantment.getById(Integer.parseInt(ench[0])),
							Integer.parseInt(ench[1]), true);
				}
			} else if (s.startsWith(">")) {
				// Lore lines format
				itemLore.add(s.substring(1));
			}
		}
		if (!itemLore.isEmpty()) {
			im.setLore(itemLore);
		}
		is.setItemMeta(im);
		return is;
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