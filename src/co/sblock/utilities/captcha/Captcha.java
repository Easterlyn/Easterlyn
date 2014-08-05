package co.sblock.utilities.captcha;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.utilities.MachineType;
import co.sblock.module.Module;
import co.sblock.utilities.inventory.InventoryUtils;

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
		// Instantiate grist cost map
		CruxiteDowel.getGrist();

		// Add captcha recipe
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
	public static ItemStack itemToCaptcha(ItemStack item) {
		if (isCard(item)) {
			// prevents Captchadex funkiness
			return item;
		}
		ItemStack card = blankCaptchaCard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta iM = item.getItemMeta();
		ArrayList<String> cardLore = new ArrayList<String>();
		cardLore.add(iM.hasDisplayName() ? iM.getDisplayName() : item.getType().name());
		cardLore.add(String.valueOf(item.getType().name()));
		cardLore.add(String.valueOf(item.getDurability()));
		cardLore.add(String.valueOf(item.getAmount()));
		// Enchantments
		for (Entry<Enchantment, Integer> e : iM.getEnchants().entrySet()) {
			cardLore.add(':' + e.getKey().getName() + ';' + e.getValue());
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
	 * For testing purposes only, good luck patching punched holes.
	 * 
	 * @param is the punchcard ItemStack
	 * 
	 * @return the unpunched captchacard
	 */
	public static ItemStack captchaToPunch(ItemStack is) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Punchcard");
		List<String> newlore = new ArrayList<>();
		// If the captcha doesn't have lore, we've already got problems, not catching this NPE
		newlore.add(im.getLore().get(0));
		for (int i = 1; i < im.getLore().size(); i++) {
			newlore.add(ChatColor.MAGIC + im.getLore().get(i));
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
			}
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
			im.setDisplayName((String) data[0]);
			// Custom display names starting with ">" or ":" could break our parsing
			// or, worse, allow free illegal enchants. Can you say Sharpness 32767?
			data[0] = "No.";
		} else {
			im.setDisplayName(null);
		}
		List<String> itemLore = new ArrayList<String>();
		for (String s : data) {
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
		if (!isCard(card1) || (card2 != null && !isPunch(card2))) {
			return null;
		}
		ItemStack result = new ItemStack(Material.BOOK);
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

	/**
	 * @param event
	 */
	@SuppressWarnings("deprecation")
	public static void handleCaptcha(InventoryClickEvent event) {
		if (event.getAction().name().contains("HOTBAR")) {
			hotbarCaptcha(event);
			return;
		}
		if (!isBlankCaptcha(event.getCurrentItem())) {
			return;
		}
		ItemStack captcha = null;
		if (CruxiteDowel.expCost(event.getCursor()) == Integer.MAX_VALUE
				|| InventoryUtils.isUniqueItem(event.getCursor())) {
			// Invalid captcha objects
			if (!event.getCursor().isSimilar(MachineType.COMPUTER.getUniqueDrop())) {
				// Computers can (and should) be alchemized.
				return;
			} else {
				captcha = createLoreCard("Computer");
			}
		}
		Player p = (Player) event.getWhoClicked();
		if (captcha == null) {
			captcha = itemToCaptcha(event.getCursor());
		}
		event.setResult(Result.DENY);
		event.setCurrentItem(InventoryUtils.decrement(event.getCurrentItem(), 1));

		// attempt to add to inventory that contained the captchacards
		int leftover = InventoryUtils.getAddFailures(event.getClickedInventory().addItem(captcha));

		// attempt to add to other inventory if first inv was too full
		if (leftover > 0) {
			if (event.getRawSlot() == event.getView().convertSlot(event.getRawSlot())) {
				leftover = InventoryUtils.getAddFailures(event.getView().getBottomInventory().addItem(captcha));
			} else {
				leftover = InventoryUtils.getAddFailures(event.getView().getTopInventory().addItem(captcha));
			}
		}

		// If both are full, set cursor to captcha
		if (leftover > 0) {
			event.setCursor(captcha);
		} else {
			event.setCursor(null);
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

	private static void hotbarCaptcha(InventoryClickEvent event) {
		ItemStack hotbar = event.getView().getBottomInventory().getItem(event.getHotbarButton());
		ItemStack captcha;
		if (!isBlankCaptcha(hotbar) || event.getCurrentItem() == null
				|| event.getCurrentItem().getType() == Material.AIR
				|| CruxiteDowel.expCost(event.getCurrentItem()) == Integer.MAX_VALUE
				|| InventoryUtils.isUniqueItem(event.getCurrentItem())) {
			// Invalid captcha objects
			if (!event.getCursor().isSimilar(MachineType.COMPUTER.getUniqueDrop())) {
				// Computers can (and should) be alchemized.
				return;
			} else {
				captcha = createLoreCard("Computer");
			}
		}
		captcha = itemToCaptcha(event.getCurrentItem());
		event.setResult(Result.DENY);
		event.getView().getBottomInventory().setItem(event.getHotbarButton(), InventoryUtils.decrement(hotbar, 1));

		int leftover = InventoryUtils.getAddFailures(event.getView().getBottomInventory().addItem(captcha));
		event.setCurrentItem(null);
		if (leftover > 0) {
			leftover = InventoryUtils.getAddFailures(event.getView().getTopInventory().addItem(captcha));
		}
		if (leftover > 0) {
			event.getWhoClicked().getWorld().dropItem(event.getWhoClicked().getLocation(), captcha);
		}
	}

	@Override
	protected String getModuleName() {
		return "CaptchaCards";
	}
}
