package co.sblock.captcha;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;

import co.sblock.Sblock;
import co.sblock.effects.Effects;
import co.sblock.machines.Machines;
import co.sblock.module.Module;
import co.sblock.utilities.InventoryUtils;
import co.sblock.utilities.JSONUtil;
import co.sblock.utilities.NumberUtils;

import net.md_5.bungee.api.ChatColor;

/**
 * Module for Captchacards, Punchcards, and Totems.
 * 
 * @author Jikoo, Dublek
 */
public class Captcha extends Module {

	protected static final String HASH_PREFIX = ChatColor.DARK_AQUA.toString() + ChatColor.YELLOW + ChatColor.LIGHT_PURPLE;

	private final LoadingCache<String, ItemStack> cache;

	private Effects effects;
	private Machines machines;

	public Captcha(Sblock plugin) {
		super(plugin);
		this.cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
				.removalListener(new RemovalListener<String, ItemStack>() {

					@Override
					public void onRemoval(RemovalNotification<String, ItemStack> notification) {

						try {
							File folder = new File(getPlugin().getDataFolder(), "captcha");
							if (!folder.exists()) {
								folder.mkdirs();
							}
							File file = new File(folder, notification.getKey());
							if (file.exists()) {
							}
							try (BukkitObjectOutputStream stream = new BukkitObjectOutputStream(new FileOutputStream(file))) {
								stream.writeObject(notification.getValue());
							}
						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				})
				.build(new CacheLoader<String, ItemStack>() {

					@Override
					public ItemStack load(String hash) throws Exception {
						File folder = new File(getPlugin().getDataFolder(), "captcha");
						if (!folder.exists()) {
							folder.mkdirs();
						}
						File file = new File(folder, hash);
						if (!file.exists()) {
							return null;
						}
						try (BukkitObjectInputStream stream = new BukkitObjectInputStream(
								new FileInputStream(file))) {
							ItemStack item = (ItemStack) stream.readObject();
							if (item.getType() == Material.POTION) {
								item = InventoryUtils.convertLegacyPotion(item);
							}
							return item.clone();
						} catch (ClassNotFoundException e) {
							e.printStackTrace();
							return null;
						}
					}

				});
	}

	@Override
	protected void onEnable() {
		CruxiteDowel.getGrist();
		insertCaptchaRecipe();

		effects = getPlugin().getModule(Effects.class);
		machines = getPlugin().getModule(Machines.class);

		addCustomHash("00000000", machines.getMachineByName("PGO").getUniqueDrop());
	}

	public boolean addCustomHash(String hash, ItemStack item) {
		if (getItemByHash(hash) != null) {
			return false;
		}
		cache.put(hash, item);
		return true;
	}

	public String calculateHashFor(ItemStack item) {
		String itemString = JSONUtil.getItemText(item).toString();
		BigInteger hash = NumberUtils.md5(itemString);
		String itemHash = NumberUtils.getBase(hash, 62, 8);
		ItemStack captcha;
		while ((captcha = getItemByHash(itemHash)) != null && !captcha.equals(item)) {
			hash = hash.add(BigInteger.ONE);
			itemHash = NumberUtils.getBase(hash, 62, 8);
		}
		return itemHash;
	}

	public String getHashByItem(ItemStack item) {
		String itemHash = calculateHashFor(item);
		cache.put(itemHash, item);
		return itemHash;
	}

	public ItemStack getItemByHash(String hash) {
		try {
			return cache.get(hash);
		} catch (ExecutionException e) {
			e.printStackTrace();
			return null;
		}
	}

	public ItemStack getCaptchaFor(String hash) {
		ItemStack item = getItemByHash(hash);
		if (item == null) {
			return null;
		}
		ItemStack card = blankCaptchaCard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> cardLore = new ArrayList<String>();
		StringBuilder builder = new StringBuilder().append(ChatColor.DARK_AQUA).append(item.getAmount()).append(' ');
		if (isCaptcha(item)) {
			builder.append("Captcha of ").append(meta.getLore().get(0));
		} else if (meta.hasDisplayName() && !InventoryUtils.isMisleadinglyNamed(meta.getDisplayName(), item.getType(), item.getDurability())) {
			builder.append(meta.getDisplayName());
		} else {
			builder.append(InventoryUtils.getMaterialDataName(item.getType(), item.getDurability()));
		}
		cardLore.add(builder.toString());
		if (item.getType().getMaxDurability() > 0) {
			builder.delete(0, builder.length());
			builder.append(ChatColor.YELLOW).append("Durability: ").append(ChatColor.DARK_AQUA)
					.append(item.getType().getMaxDurability() - item.getDurability())
					.append(ChatColor.YELLOW).append("/").append(ChatColor.DARK_AQUA)
					.append(item.getType().getMaxDurability());
			cardLore.add(builder.toString());
		}
		builder.delete(0, builder.length());
		builder.append(HASH_PREFIX).append(hash);
		cardLore.add(builder.toString());
		cardMeta.setDisplayName("Captchacard");
		cardMeta.setLore(cardLore);
		card.setItemMeta(cardMeta);
		return card;
	}

	/**
	 * Creates a blank Captchacard
	 * 
	 * @return ItemStack
	 */
	public static ItemStack blankCaptchaCard() {
		ItemStack is = new ItemStack(Material.BOOK);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Captchacard");
		im.setLore(Arrays.asList("Blank"));
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Adds the Captchacard recipe.
	 * <p>
	 * To reduce player confusion when using commands like /recipe, all other recipes for the
	 * material are removed and then re-added so they take precedence.
	 */
	private void insertCaptchaRecipe() {

		// Remove and store existing recipes for the correct material
		List<Recipe> recipes = new ArrayList<Recipe>();
		Iterator<Recipe> iterator = Bukkit.recipeIterator();
		Material toRemove = blankCaptchaCard().getType();
		while (iterator.hasNext()) {
			Recipe next = iterator.next();
			if (next.getResult().getType() == toRemove) {
				recipes.add(next);
				iterator.remove();
			}
		}

		// Captcha recipe. Most excellent.
		ItemStack captchaItem = blankCaptchaCard();
		captchaItem.setAmount(3);
		ShapedRecipe captchaRecipe = new ShapedRecipe(captchaItem);
		captchaRecipe.shape("AA", "AA", "AA");
		captchaRecipe.setIngredient('A', Material.PAPER);
		Bukkit.addRecipe(captchaRecipe);

		// Re-add the pre-existing recipes so they have higher priority.
		Collections.reverse(recipes);
		for (Recipe recipe : recipes) {
			Bukkit.addRecipe(recipe);
		}
	}

	@Override
	protected void onDisable() {
		cache.invalidateAll();
	}

	@Override
	public String getName() {
		return "Captcha";
	}

	/**
	 * Converts an ItemStack into a Captchacard.
	 * 
	 * @param item the ItemStack to convert
	 * 
	 * @return the Captchacard representing by this ItemStack
	 */
	public ItemStack itemToCaptcha(ItemStack item) {
		if (item.isSimilar(machines.getMachineByName("Computer").getUniqueDrop())) {
			return createLorecard(ChatColor.GRAY + "Computer I");
		}
		return getCaptchaFor(getHashByItem(item));
	}

	/**
	 * Converts a Captchacard into an ItemStack. Also used for Punchcards and
	 * Cruxite Dowels.
	 * 
	 * @param card the Captchacard ItemStack
	 * 
	 * @return the ItemStack represented by this Captchacard
	 */
	public ItemStack captchaToItem(ItemStack card) {
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
	private ItemStack captchaToItem(ItemStack card, boolean loreCard) {
		if (card == null) {
			return null;
		}
		if ((!isCard(card) && !CruxiteDowel.isUsedDowel(card))
				|| !loreCard && card.getItemMeta().getDisplayName().equals("Lorecard")) {
			// Lore card and not being combined or not a captcha
			card = card.clone();
			card.setAmount(1);
			return card;
		}
		if (card.getItemMeta().getDisplayName().equals("Lorecard")) {
			ItemStack is = new ItemStack(Material.DIRT);
			ItemMeta im = is.getItemMeta();
			ArrayList<String> storedLore = new ArrayList<>(im.getLore());
			for (String lore : card.getItemMeta().getLore()) {
				// isCard checks if lore exists, this is fine.
				if (lore.length() < 1 || lore.charAt(0) != '>') {
					continue;
				}
				storedLore.add(lore.substring(1));
			}
			im.setLore(storedLore);
			is.setItemMeta(im);
			return is;
		}
		for (String lore : card.getItemMeta().getLore()) {
			if (!lore.startsWith(HASH_PREFIX)) {
				continue;
			}
			lore = lore.substring(HASH_PREFIX.length());
			if (!lore.matches("[0-9A-Za-z]{8,}")) {
				continue;
			}
			ItemStack item = getItemByHash(lore);
			if (item != null) {
				return item;
			}
		}
		card = card.clone();
		card.setAmount(1);
		return card;
	}

	/**
	 * Create a punchcard from a captchacard.
	 * <p>
	 * Good luck patching punched holes.
	 * 
	 * @param captcha the punchcard ItemStack
	 * 
	 * @return the unpunched captchacard
	 */
	public ItemStack captchaToPunch(ItemStack captcha) {
		if (!isCard(captcha)) {
			return captcha;
		}
		captcha = captcha.clone();
		if (Captcha.isBlankCaptcha(captcha)) {
			ItemMeta im = captcha.getItemMeta();
			im.setDisplayName("Punchcard");
			im.setLore(Arrays.asList(HASH_PREFIX + "00000000"));
			captcha.setItemMeta(im);
			return captcha;
		}
		ItemStack item = captchaToItem(captcha);
		if (isCaptcha(item) || CruxiteDowel.expCost(effects, item) == Integer.MAX_VALUE) {
			return captcha;
		}
		captcha = itemToCaptcha(item);
		ItemMeta im = captcha.getItemMeta();
		im.setDisplayName("Punchcard");
		captcha.setItemMeta(im);
		return captcha;
	}

	/**
	 * Check if an ItemStack is a valid blank Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a blank Captchacard
	 */
	public static boolean isBlankCaptcha(ItemStack is) {
		return isCaptcha(is) && is.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack is a valid Captchacard that has been used.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a Captchacard
	 */
	public static boolean isUsedCaptcha(ItemStack is) {
		return isCaptcha(is) && !is.getItemMeta().getLore().contains("Blank");
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
	 * Check if an ItemStack can be turned into a captchacard. The only items that cannot be put
	 * into a captcha are other captchas of captchas and unique Machine key items.
	 * 
	 * @param item the ItemStack to check
	 * @return
	 */
	public boolean canCaptcha(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return false;
		}
		// TODO lorecards and the active Computer effect
//		if (item.isSimilar(machines.getMachineByName("Computer").getUniqueDrop())) {
//			// Computers can (and should) be alchemized.
//			return true;
//		}
		for (ItemStack is : InventoryUtils.getUniqueItems(getPlugin())) {
			if (is.isSimilar(item)) {
				return false;
			}
		}
		return !CruxiteDowel.isDowel(item) && !isUsedCaptcha(item)
				|| !item.getItemMeta().getLore().get(0).matches("^(.3-?[0-9]+ Captcha of )+.+$");
	}

	/**
	 * Checks if an ItemStack is any Punchcard or Captchacard.
	 * 
	 * @param is the ItemStack to check
	 * 
	 * @return true if the ItemStack is a card
	 */
	public static boolean isCard(ItemStack is) {
		if (is == null || is.getType() != Material.BOOK || !is.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = is.getItemMeta();
		return meta.hasLore() && meta.hasDisplayName()
				&& (meta.getDisplayName().equals("Captchacard")
						|| meta.getDisplayName().equals("Punchcard")
						|| meta.getDisplayName().equals("Lorecard"));
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
	public ItemStack createCombinedPunch(ItemStack card1, ItemStack card2) {
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
			lore = effects.organizeEffectLore(item.getItemMeta().getLore(), false,
					false, true, item2.getItemMeta().getLore().toArray(new String[0]));
		} else {
			lore = effects.organizeEffectLore(item.getItemMeta().getLore(), false,
					false, true);
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
	public void handleCaptcha(InventoryClickEvent event) {
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

		if (!isBlankCaptcha(blankCaptcha) || !canCaptcha(toCaptcha) || isBlankCaptcha(toCaptcha)) {
			return;
		}

		ItemStack captcha = itemToCaptcha(toCaptcha);;
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
		((Player) event.getWhoClicked()).updateInventory();
	}

	/**
	 * Creates a lorecard with the given lore.
	 * 
	 * @param lore the lore to add to the lorecard
	 * @return the lorecard
	 */
	public static ItemStack createLorecard(String lore) {
		ItemStack card = new ItemStack(Material.BOOK);
		ItemMeta im = card.getItemMeta();
		ArrayList<String> loreList = new ArrayList<>();
		loreList.add('>' + lore);
		im.setLore(loreList);
		im.setDisplayName("Lorecard");
		card.setItemMeta(im);
		return card;
	}

}
