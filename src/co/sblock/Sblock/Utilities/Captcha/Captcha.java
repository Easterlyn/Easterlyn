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

	/** The CaptchaCommandListener. */
	private CaptchaCommandListener clistener = new CaptchaCommandListener();

	/**
	 * @see Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		this.captchaCardRecipe();
		this.registerCommands(clistener);
	}

	/**
	 * @see Module#onDisable()
	 */
	@Override
	protected void onDisable() {

	}

	/**
	 * Converts an ItemStack into a Captchacard.
	 * 
	 * @param item the ItemStack to convert
	 * 
	 * @return the Captchacard representing by this ItemStack
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
	 * Converts a Captchacard into an ItemStack.
	 * 
	 * @param card the Captchacard ItemStack
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	public static ItemStack captchaToItem(ItemStack card) {
		return getCaptchaItem(card.getItemMeta().getLore().toArray(new String[0]));
	}

	/**
	 * Converts a Captchdex entry into an ItemStack.
	 * 
	 * @param data the Captchadex page split at '\n' ItemStack
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getCaptchaItem(String[] data) {
		ItemStack is = new ItemStack(Material.getMaterial(Integer.valueOf(data[1])),
				Integer.valueOf(data[3]), Short.valueOf(data[2]));
		ItemMeta im = is.getItemMeta();
		if (!data[0].equals(is.getType().toString())) {
			im.setDisplayName((String) data[0]);
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
	 * Adds the Captchacard recipe.
	 */
	private void captchaCardRecipe() {
		ShapedRecipe recipe = new ShapedRecipe(blankCaptchaCard());
		recipe.shape("AA", "AA", "AA");
		recipe.setIngredient('A', Material.PAPER);
		Bukkit.addRecipe(recipe);
	}

	/**
	 * Check if an ItemStack is a valid blank Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a blank Captchacard
	 */
	public static boolean isBlankCaptcha(ItemStack is) {
		return is != null && is.getType() == Material.PAPER
				&& is.hasItemMeta() && is.getItemMeta().hasDisplayName()
				&& is.getItemMeta().getDisplayName().equals("Captchacard")
				&& is.getItemMeta().hasLore() && is.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack is a valid Captchacard that has been used.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Captchacard
	 */
	public static boolean isUsedCaptcha(ItemStack is) {
		return is != null && is.getType() == Material.PAPER
				&& is.hasItemMeta() && is.getItemMeta().hasDisplayName()
				&& is.getItemMeta().getDisplayName().equals("Captchacard")
				&& is.getItemMeta().hasLore() && !is.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack is a valid Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Captchacard
	 */
	public static boolean isCaptcha(ItemStack is) {
		return is != null && is.getType() == Material.PAPER && is.hasItemMeta()
				&& is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()
				&& is.getItemMeta().getDisplayName().equals("Captchacard");
	}


	/**
	 * Check if an ItemStack is a valid Punchcard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Punchcard
	 */
	public static boolean isPunch(ItemStack is) {
		return is != null && is.getType() == Material.PAPER && is.hasItemMeta()
				&& is.getItemMeta().hasLore() && is.getItemMeta().hasDisplayName()
				&& is.getItemMeta().getDisplayName().equals("Punchcard");
	}

	/**
	 * Check if an ItemStack is a valid single Punchcard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a single Punchcard
	 */
	public static boolean isSinglePunch(ItemStack is) {
		return is.getType() == Material.PAPER && is.hasItemMeta()
				&& is.getItemMeta().hasLore() && is.getItemMeta().hasDisplayName()
				&& is.getItemMeta().getDisplayName().equals("Punchcard") && is.getAmount() == 1;
	}

	/**
	 * Checks if an ItemStack is any Punchcard or Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a card
	 */
	public static boolean isCard(ItemStack is) {
		return is != null && is.getType() == Material.PAPER && is.hasItemMeta()
				&& is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()
				&& (is.getItemMeta().getDisplayName().equals("Captchacard")
						|| is.getItemMeta().getDisplayName().equals("Punchcard"));
	}

	/**
	 * Creates a Punchcard from one or two cards.
	 * If card2 is null, creates a clone of card1.
	 * card2 must be a Punchcard or null.
	 * 
	 * @param card1 the first card
	 * @param card2 the second card, or null to clone card1
	 * 
	 * @return the ItemStack created or null if invalid cards are provided
	 */
	public static ItemStack createCombinedPunch(ItemStack card1, ItemStack card2) {
		if (!isCard(card1) || (card2 != null && !isPunch(card2))) {
			return null;
		}
		ItemStack result = new ItemStack(Material.PAPER);
		ArrayList<String> lore = new ArrayList<>();
		lore.addAll(card1.getItemMeta().getLore());
		if (card2 != null) {
			for (String s : card2.getItemMeta().getLore()) {
				if (s.length() > 1 && s.charAt(0) == '>') {
					lore.add(s);
				}
			}
		}
		ItemMeta im = result.getItemMeta();
		im.setLore(lore);
		im.setDisplayName("Punchcard");
		result.setItemMeta(im);
		return result;
	}
}
