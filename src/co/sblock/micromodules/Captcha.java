package co.sblock.micromodules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.BaseEncoding;

import co.sblock.effects.Effects;
import co.sblock.machines.Machines;
import co.sblock.module.Module;
import co.sblock.utilities.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * @author Dublek, Jikoo
 */
public class Captcha extends Module {

	@Override
	protected void onEnable() {
		// Instantiate grist cost map
		CruxiteDowel.getGrist();

		// Add captcha recipe
		this.captchaCardRecipe();
	}

	@Override
	protected void onDisable() {}

	/**
	 * Converts an ItemStack into a Captchacard.
	 * 
	 * @param item the ItemStack to convert
	 * 
	 * @return the Captchacard representing by this ItemStack
	 */
	public static ItemStack itemToCaptcha(ItemStack item) {
		ItemStack card = blankCaptchaCard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> cardLore = new ArrayList<String>();
		StringBuilder name = new StringBuilder().append(ChatColor.DARK_AQUA).append(item.getAmount()).append(' ');
		if (meta.hasDisplayName() && !InventoryUtils.isMisleadinglyNamed(meta.getDisplayName(), item.getType(), item.getDurability())) {
			if (isCaptcha(item)) {
				name.append("Captcha of ").append(meta.getLore().get(0));
			} else {
				name.append(meta.getDisplayName());
			}
		} else {
			name.append(InventoryUtils.getMaterialDataName(item.getType(), item.getDurability()));
		}
		cardLore.add(name.toString());
		if (item.getType().getMaxDurability() > 0) {
			cardLore.add("Durability: " + ChatColor.DARK_AQUA + (item.getType().getMaxDurability() - item.getDurability())
					+ ChatColor.YELLOW + "/" + ChatColor.DARK_AQUA + item.getType().getMaxDurability());
		}
		StringBuilder serialization = new StringBuilder();
		if (CruxiteDowel.expCost(item) == Integer.MAX_VALUE) {
			cardLore.add(ChatColor.DARK_RED + "Unpunchable");
			serialization.append(ChatColor.MAGIC);
		}
		serialization.append(InventoryUtils.serializeIntoFormattingCodes(item));
		int start = 0;
		for (int i = 1024; start < serialization.length(); i += 1024) {
			if (i > serialization.length()) {
				i = serialization.length();
			}
			cardLore.add(serialization.substring(start, i));
			start = i;
		}
		cardMeta.setDisplayName("Captchacard");
		cardMeta.setLore(cardLore);
		card.setItemMeta(cardMeta);
		return card;
	}

	/**
	 * Converts a Captchacard into an ItemStack. Also used for Punchcards and
	 * Cruxite Dowels.
	 * 
	 * @param card the Captchacard ItemStack
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	public static ItemStack captchaToItem(ItemStack card) {
		return captchaToItem(card, false);
	}

	/**
	 * Converts a Captchacard into an ItemStack. Also used for Punchcards and
	 * Cruxite Dowels.
	 * 
	 * @param card the Captchacard ItemStack
	 * @param loreCard true if Lorecards are to be converted to a combinable object.
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	private static ItemStack captchaToItem(ItemStack card, boolean loreCard) {
		if (card == null) {
			return null;
		}
		if (!loreCard && card.getItemMeta().getLore().contains("Lorecard")) {
			// Specialty items cannot be uncaptcha'd.
			card = card.clone();
			card.setAmount(1);
			return card;
		}
		try {
			String[] data = card.getItemMeta().getLore().toArray(new String[0]);
			if (data[0].equals("Lorecard")) {
				ItemStack is = new ItemStack(Material.DIRT);
				ItemMeta im = is.getItemMeta();
				ArrayList<String> lore = new ArrayList<>(im.getLore());
				for (String s : data) {
					if (s.length() < 1 || s.charAt(0) != '>') {
						continue;
					}
					lore.add(s.substring(1));
				}
				im.setLore(lore);
				is.setItemMeta(im);
				return is;
			}
			if (data[0].equals("Blank")) {
				return Machines.getMachineByName("PGO").getUniqueDrop();
			}
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
		if (isBlankCaptcha(is)) {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("Punchcard");
			is.setItemMeta(im);
			return is;
		}
		for (String lore : is.getItemMeta().getLore()) {
			if (lore.startsWith(ChatColor.MAGIC.toString())) {
				// New "secret" unpunchable demarkation is serialized hex prepended by magic
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

	/**
	 * Creates a blank Captchacard
	 * 
	 * @return ItemStack
	 */
	private static ItemStack blankCaptchaCard() {
		ItemStack is = new ItemStack(Material.BOOK);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Captchacard");
		im.setLore(Arrays.asList("Blank"));
		is.setItemMeta(im);
		return is;
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
		return isCard(is) && is.getItemMeta().getDisplayName().equals("Captchacard")
				&& is.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack is a valid Captchacard that has been used.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Captchacard
	 */
	public static boolean isUsedCaptcha(ItemStack is) {
		return isCard(is) && is.getItemMeta().getDisplayName().equals("Captchacard")
				&& !is.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack is a valid Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Captchacard
	 */
	public static boolean isCaptcha(ItemStack is) {
		return isCard(is) && is.getItemMeta().getDisplayName().equals("Captchacard");
	}


	/**
	 * Check if an ItemStack is a valid Punchcard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Punchcard
	 */
	public static boolean isPunch(ItemStack is) {
		return isCard(is) && is.getItemMeta().getDisplayName().equals("Punchcard");
	}

	/**
	 * Check if an ItemStack is a valid single Punchcard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a single Punchcard
	 */
	public static boolean isSinglePunch(ItemStack is) {
		return isPunch(is) && is.getAmount() == 1;
	}

	/**
	 * Checks if an ItemStack is any Punchcard or Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a card
	 */
	public static boolean isCard(ItemStack is) {
		return is != null && is.getType() == Material.BOOK && is.hasItemMeta()
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
		if (isCaptcha(card1)) {
			if (card2 != null) {
				return null;
			}
			ItemStack is = captchaToPunch(card1);
			if (card1.isSimilar(is)) {
				return null;
			}
			return is;
		}
		if (!isPunch(card1)) {
			return null;
		}
		if (isCaptcha(card2)) {
			card1 = card1.clone();
			card1.setAmount(1);
			return card1;
		}
		if (card2 != null && !isPunch(card2)) {
			return null;
		}
		ItemStack item = captchaToItem(card1);
		ItemStack item2 = captchaToItem(card2);
		List<String> lore;
		if (item2 != null && item2.hasItemMeta() && item2.getItemMeta().hasLore()) {
			lore = Effects.getInstance().organizeEffectLore(item.getItemMeta().getLore(), false,
					false, item2.getItemMeta().getLore().toArray(new String[0]));
		} else {
			lore = Effects.getInstance().organizeEffectLore(item.getItemMeta().getLore(), false,
					false);
		}
		ItemMeta im = item.getItemMeta();
		im.setLore(lore);
		item.setItemMeta(im);
		ItemStack result = captchaToPunch(itemToCaptcha(item));
		if (result.getItemMeta().getDisplayName().equals("Captchacard")) {
			return null;
		}
		return result;
	}

	@SuppressWarnings("deprecation")
	public static void handleCaptcha(InventoryClickEvent event) {
		boolean hotbar = event.getAction().name().contains("HOTBAR");
		ItemStack blankCaptcha;
		ItemStack toCaptcha;
		if (hotbar) {
			blankCaptcha = event.getView().getBottomInventory().getItem(event.getHotbarButton());
			toCaptcha = event.getCurrentItem();
		} else {
			blankCaptcha = event.getCurrentItem();
			toCaptcha = event.getCursor();
		}
		if (!isBlankCaptcha(blankCaptcha) || toCaptcha == null || toCaptcha.getType() == Material.AIR || isBlankCaptcha(toCaptcha)) {
			return;
		}
		ItemStack captcha = null;
		if (toCaptcha.isSimilar(Machines.getMachineByName("Computer").getUniqueDrop())) {
			// Computers can (and should) be alchemized.
			captcha = createLoreCard("Computer");
		} else {
			for (ItemStack is : InventoryUtils.getUniqueItems()) {
				if (is.isSimilar(toCaptcha)) {
					return;
				}
			}
		}
		if (isUsedCaptcha(toCaptcha) && toCaptcha.getItemMeta().getLore().get(0).matches("^(.3-?[0-9]+ Captcha of )+.+$")) {
			// Double captchas are fine, triple captchas hurt client
			return;
		}
		Player p = (Player) event.getWhoClicked();
		if (captcha == null) {
			captcha = itemToCaptcha(toCaptcha);
		}
		event.setResult(Result.DENY);

		// Decrement captcha stack
		if (hotbar) {
			event.getView().getBottomInventory().setItem(event.getHotbarButton(), InventoryUtils.decrement(blankCaptcha, 1));
			event.setCurrentItem(null);
		} else {
			event.setCurrentItem(InventoryUtils.decrement(blankCaptcha, 1));
			event.setCursor(null);
		}

		// Add to bottom inventory first
		int leftover = InventoryUtils.getAddFailures(event.getView().getBottomInventory().addItem(captcha));
		if (leftover > 0) {
			// Add to top, bottom was full.
			leftover = InventoryUtils.getAddFailures(event.getView().getTopInventory().addItem(captcha));
		}
		if (leftover > 0) {
			if (hotbar) {
				// Drop rather than delete (Items can be picked up before event completes, thanks Bukkit.)
				event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), captcha);
			} else {
				// Set cursor to captcha
				event.setCursor(captcha);
			}
		}
		p.updateInventory();
	}

	/**
	 * @param string
	 * @return
	 */
	public static ItemStack createLoreCard(String lore) {
		ItemStack card = new ItemStack(Material.BOOK);
		ItemMeta im = card.getItemMeta();
		ArrayList<String> loreList = new ArrayList<>();
		loreList.add("Lorecard");
		loreList.add('>' + lore);
		im.setLore(loreList);
		im.setDisplayName("Captchacard");
		card.setItemMeta(im);
		return card;
	}

	@Override
	protected String getModuleName() {
		return "Sblock Captcha";
	}
}
