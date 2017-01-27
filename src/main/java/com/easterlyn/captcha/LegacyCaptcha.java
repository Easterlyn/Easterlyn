package com.easterlyn.captcha;

import com.easterlyn.utilities.InventoryUtils;

import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;

/**
 * Backwards compatibility for outdated captchacards.
 * 
 * @author Jikoo
 */
public class LegacyCaptcha {

	/**
	 * Converts a Captchacard into an ItemStack. Also used for Punchcards and
	 * Cruxite Dowels.
	 * 
	 * @param card the Captchacard ItemStack
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	protected static ItemStack captchaToItem(ItemStack card) {
		try {
			String[] data = card.getItemMeta().getLore().toArray(new String[0]);
			for (int j = 1; j < data.length; j++) {
				if (data[j].startsWith(ChatColor.MAGIC.toString())) {
					data[j] = data[j].substring(2);
					break;
				}
			}
			StringBuilder serialized = null;
			for (int i = 1; i < data.length; i++) {
				if (serialized == null && ChatColor.stripColor(data[i]).isEmpty()) {
					serialized = new StringBuilder(data[i]);
					continue;
				}
				if (serialized == null) {
					continue;
				}
				serialized.append(data[i]);
			}
			return InventoryUtils.deserializeFromFormattingCodes(serialized.toString());
		} catch (Exception e) {
			card = card.clone();
			card.setAmount(1);
			return card;
		}
	}

}
