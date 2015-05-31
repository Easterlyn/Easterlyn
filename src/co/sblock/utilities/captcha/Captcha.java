package co.sblock.utilities.captcha;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import com.google.common.io.BaseEncoding;

import co.sblock.machines.utilities.MachineType;
import co.sblock.module.Module;
import co.sblock.utilities.inventory.InventoryUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * @author Dublek, Jikoo
 */
public class Captcha extends Module {

	/**
	 * @see Module#onEnable()
	 */
	@Override
	protected void onEnable() {
		// Instantiate grist cost map
		CruxiteDowel.getGrist();

		// Add captcha recipe
		this.captchaCardRecipe();
	}

	/**
	 * @see Module#onDisable()
	 */
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
		if (card.getItemMeta().getLore().contains("Lorecard")) {
			// Specialty items cannot be uncaptcha'd.
			return card;
		}
		return getCaptchaItem(card.getItemMeta().getLore().toArray(new String[0]));
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
		if (isBlankCaptcha(is)) {
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("Punchcard");
			is.setItemMeta(im);
			return is;
		}
		is = convert(is);
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
	 * Converts a Captchdex entry into an ItemStack.
	 * 
	 * @param data the Captchadex page split at '\n' ItemStack
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	@SuppressWarnings("deprecation")
	public static ItemStack getCaptchaItem(String[] data) {
		ItemStack is;
		ItemMeta im;
		if (data[0].equals("Lorecard")) {
			is = createLoreCard(data[1]);
			im = is.getItemMeta();
			ArrayList<String> lore = new ArrayList<>(im.getLore());
			for (int i = 2; i < data.length; i++) {
				lore.add(data[i]);
			}
			im.setLore(lore);
			is.setItemMeta(im);
			return is;
		}
		if (data[0].equals("Blank")) {
			return MachineType.PERFECTLY_GENERIC_OBJECT.getUniqueDrop();
		}
		for (int j = 1; j < data.length; j++) {
			if (data[j].startsWith(ChatColor.MAGIC.toString())) {
				data[j] = data[j].substring(2);
				break;
			}
		}
		if (data[0].startsWith(ChatColor.DARK_AQUA.toString())) {
			// New serialization format
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
		}
		try {
			is = new ItemStack(Material.getMaterial(Integer.valueOf(data[1])),
					Integer.valueOf(data[3]), Short.valueOf(data[2]));
		} catch (NumberFormatException e) {
			is = new ItemStack(Material.getMaterial(data[1]),
					Integer.valueOf(data[3]), Short.valueOf(data[2]));
		}
		im = is.getItemMeta();
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
			if (s.isEmpty()) {
				continue;
			}
			if (s.charAt(0) == ':') {
				// Enchantments line format
				String[] enchs = s.substring(1).split(":");
				for (String s1 : enchs) {
					String[] ench = s1.split(";");
					try {
						im.addEnchant(Enchantment.getById(Integer.parseInt(ench[0])),
								Integer.parseInt(ench[1]), true);
					} catch (NumberFormatException e) {
						im.addEnchant(Enchantment.getByName(ench[0]),
								Integer.parseInt(ench[1]), true);
					}
				}
			} else if (s.charAt(0) == '>') {
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
		ItemStack iS = new ItemStack(Material.BOOK);
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
		return is != null && (is.getType() == Material.PAPER || is.getType() == Material.BOOK)
				&& is.hasItemMeta() && is.getItemMeta().hasDisplayName() && is.getItemMeta().hasLore()
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
		if (!isCard(card1)) {
			return null;
		}
		if (isCaptcha(card1)) {
			if (card2 != null) {
				return null;
			}
			return captchaToPunch(card1);
		}
		ItemStack item = captchaToItem(card1);
		ArrayList<String> lore = new ArrayList<>();
		lore.addAll(item.getItemMeta().getLore());
		if (card2 != null) {
			lore.addAll(captchaToItem(card2).getItemMeta().getLore());
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
		if (toCaptcha.isSimilar(MachineType.COMPUTER.getUniqueDrop())) {
			// Computers can (and should) be alchemized.
			captcha = createLoreCard("Computer");
		} else {
			for (ItemStack is : InventoryUtils.getUniqueItems()) {
				if (is.isSimilar(toCaptcha)) {
					return;
				}
			}
		}
		if (isUsedCaptcha(toCaptcha) && toCaptcha.getItemMeta().getLore().get(0).matches("^(..-?[0-9]+ Captcha of )+..-?[0-9]+ .+$")) {
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

	public static int convert(Player player) {
		int conversions = 0;
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack is = player.getInventory().getItem(i);
			if (!Captcha.isUsedCaptcha(is)) {
				continue;
			}
			if (is.getItemMeta().getLore().get(0).startsWith(ChatColor.DARK_AQUA.toString())) {
				continue;
			}
			ItemStack captchas = Captcha.itemToCaptcha(Captcha.captchaToItem(is));
			captchas.setAmount(is.getAmount());
			conversions += is.getAmount();
			player.getInventory().setItem(i, captchas);
		}
		return conversions;
	}

	private static ItemStack convert(ItemStack is) {
		if (!Captcha.isUsedCaptcha(is)) {
			return is;
		}
		if (is.getItemMeta().getLore().get(0).startsWith(ChatColor.DARK_AQUA.toString())) {
			return is;
		}
		ItemStack captchas = Captcha.itemToCaptcha(Captcha.captchaToItem(is));
		captchas.setAmount(is.getAmount());
		return captchas;
	}

	@Override
	protected String getModuleName() {
		return "Sblock Captcha";
	}
}
