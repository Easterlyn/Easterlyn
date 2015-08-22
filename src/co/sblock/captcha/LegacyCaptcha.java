package co.sblock.captcha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.BaseEncoding;

import co.sblock.utilities.InventoryUtils;

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

	/**
	 * Create a punchcard from a captchacard.
	 * <p>
	 * Good luck patching punched holes.
	 * 
	 * @param is the punchcard ItemStack
	 * 
	 * @return the unpunched captchacard
	 */
	public static ItemStack captchaToPunch(ItemStack is) {
		is = is.clone();
		if (Captcha.isBlankCaptcha(is)) {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("Punchcard");
			is.setItemMeta(im);
			return is;
		}
		for (String lore : is.getItemMeta().getLore()) {
			if (lore.startsWith(ChatColor.MAGIC.toString())) {
				// "New" "secret" unpunchable demarkation is serialized hex prepended by magic
				return is;
			}
		}
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Punchcard");
		List<String> newlore = new ArrayList<>();
		// If the captcha doesn't have the correct lore, we've got issues already.
		String lore0 = im.getLore().get(0);
		int space = lore0.indexOf(' ') + 1;
		String line = lore0.substring(0, space);
		String encoded = BaseEncoding.base16().encode(lore0.substring(space).getBytes());
		line += encoded.substring(encoded.length() > 8 ? encoded.length() - 8 : 0, encoded.length());
		newlore.add(line);
		for (int i = 1; i < im.getLore().size(); i++) {
			newlore.add(im.getLore().get(i));
		}
		im.setLore(newlore);
		is.setItemMeta(im);
		return is;
	}
}
