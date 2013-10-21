package co.sblock.Sblock.Utilities.Captcha;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.Sblock.Module;

/**
 * @author Dublek, Jikoo
 */
public class Captcha extends Module {

	/** The <code>CaptchaCommandListener</code>. */
	private CaptchaCommandListener clistener = new CaptchaCommandListener();
	/** The <code>CaptchaEventListener</code>. */
	private CaptchaEventListener cEL = new CaptchaEventListener();

	/**
	 * @see Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		this.captchaCardRecipe();
		this.registerCommands(clistener);
		this.registerEvents(cEL);
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	protected void onDisable() {

	}

	/**
	 * Converts an <code>ItemStack</code> into a Captchacard.
	 * 
	 * @param item
	 *            the <code>ItemStack</code> to convert
	 * @return the Captchacard representing by this <code>ItemStack</code>
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack itemToCaptcha(ItemStack item) {
		ItemStack card = blankCaptchaCard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta iM = item.getItemMeta();
		String name;
		if (iM.hasDisplayName()) {
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
				enchants.append('\u003A').append(e.getKey().getId()).append('\u003B')
						.append(e.getValue());
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

	/**
	 * Converts a Captchacard into an <code>ItemStack</code>.
	 * 
	 * @param card
	 *            the Captchacard <code>ItemStack</code>
	 * @return the <code>ItemStack</code> represented by this Captchacard
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack captchaToItem(ItemStack card) {
		ArrayList<String> lore = (ArrayList<String>) card.getItemMeta().getLore();
		// Item: ID, quantity, data (damage)
		ItemStack is = new ItemStack(Material.getMaterial(Integer.valueOf(lore.get(1))),
				Integer.valueOf(lore.get(3)), Short.valueOf(lore.get(2)));

		ItemMeta im = is.getItemMeta();
		if (!lore.get(0).equals(is.getType().toString())) {
			im.setDisplayName(lore.get(0));
			// Custom display names starting with ">" or ":" could break our parsing
			// or, worse, allow free illegal enchants. Can you say Sharpness 32767?
			lore.set(0, "No.");
		} else {
			im.setDisplayName(null);
		}
		List<String> itemLore = new ArrayList<String>();
		for (String s : lore) {
			if (s.charAt(0) == '\u003A') {
				// Enchantments line format
				String[] enchs = s.substring(1).split(":");
				for (String s1 : enchs) {
					String[] ench = s1.split(";");
					im.addEnchant(Enchantment.getById(Integer.parseInt(ench[0])),
							Integer.parseInt(ench[1]), true);
				}
			} else if (s.charAt(0) == '\u003E') {
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

	/**
	 * Converts a Captchdex entry into an <code>ItemStack</code>.
	 * 
	 * @param data
	 *            the Captchadex page split at '\n' <code>ItemStack</code>
	 * @return the <code>ItemStack</code> represented by this Captchacard
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getCaptchaItem(String[] data) {
		ItemStack is = new ItemStack(Material.getMaterial(Integer.valueOf(data[1])),
				Integer.valueOf(data[3]), Short.valueOf(data[2]));
		ItemMeta im = is.getItemMeta();
		if (!data[0].equals(is.getType().toString())) {
			im.setDisplayName(data[0]);
			// Custom display names starting with ">" or ":" could break our parsing
			// or, worse, allow free illegal enchants. Can you say Sharpness 32767?
			data[0] = "No.";
		} else {
			im.setDisplayName(null);
		}
		List<String> itemLore = new ArrayList<String>();
		for (String s : data) {
			if (s.charAt(0) == '\u003A') {
				// Enchantments line format
				String[] enchs = s.substring(1).split(":");
				for (String s1 : enchs) {
					String[] ench = s1.split(";");
					im.addEnchant(Enchantment.getById(Integer.parseInt(ench[0])),
							Integer.parseInt(ench[1]), true);
				}
			} else if (s.charAt(0) == '\u003E') {
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

	/**
	 * Creates a blank Captchacard
	 * 
	 * @return ItemStack
	 */
	private static ItemStack blankCaptchaCard() {
		ItemStack iS = new ItemStack(Material.PAPER);
		ItemMeta iM = iS.getItemMeta();
		iM.setDisplayName("Captchacard");
		ArrayList<String> lore = new ArrayList<String>();
		lore.add("Blank");
		iM.setLore(lore);
		iS.setItemMeta(iM);
		return iS;
	}

	/**
	 * Adds the Captchacard recipe
	 */
	private void captchaCardRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(blankCaptchaCard());
		recipe.shape("AA", "AA", "AA");
		recipe.setIngredient('A', Material.PAPER);
		Bukkit.addRecipe(recipe);
	}

	/**
	 * Check if an <code>ItemStack</code> is a valid blank Captchacard.
	 * 
	 * @param is
	 *            the <code>ItemStack</code> to check
	 * @return true if the <code>ItemStack</code> is a blank Captchacard
	 */
	public static boolean isBlankCard(ItemStack is) {
		if (is.getType().equals(Material.PAPER) && is.hasItemMeta()
				&& is.getItemMeta().getDisplayName().equals("Captchacard")
				&& is.getItemMeta().hasLore() && is.getItemMeta().getLore().contains("Blank")) {
			return true;
		}
		return false;
	}

	/**
	 * Check if an <code>ItemStack</code> is a valid Captchacard.
	 * 
	 * @param is
	 *            the <code>ItemStack</code> to check
	 * @return true if the <code>ItemStack</code> is a Captchacard
	 */
	public static boolean isCaptchaCard(ItemStack is) {
		if (is.getType().equals(Material.PAPER) && is.hasItemMeta()
				&& is.getItemMeta().getDisplayName().equals("Captchacard")
				&& is.getItemMeta().hasLore() && !is.getItemMeta().getLore().contains("Blank")) {
			return true;
		}
		return false;
	}

	/**
	 * Check if an <code>ItemStack</code> is a valid Punchcard.
	 * 
	 * @param is
	 *            the <code>ItemStack</code> to check
	 * @return true if the <code>ItemStack</code> is a Punchcard
	 */
	public static boolean isPunchCard(ItemStack is) {
		if (is.getType().equals(Material.PAPER) && is.hasItemMeta()
				&& is.getItemMeta().getDisplayName().equals("Punchcard")) {
			return true;
		}
		return false;
	}

	/**
	 * Check if an <code>ItemStack</code> is a valid single Punchcard.
	 * 
	 * @param is
	 *            the <code>ItemStack</code> to check
	 * @return true if the <code>ItemStack</code> is a single Punchcard
	 */
	public static boolean isSinglePunchCard(ItemStack is) {
		if (is.getType().equals(Material.PAPER) && is.hasItemMeta()
				&& is.getItemMeta().getDisplayName().equals("Punchcard") && is.getAmount() == 1) {
			return true;
		}
		return false;
	}
}
