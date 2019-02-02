package com.easterlyn.captcha;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.effects.Effects;
import com.easterlyn.module.Module;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.JSONUtil;
import com.easterlyn.utilities.NumberUtils;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Module for Captchacards, Punchcards, and Totems.
 *
 * @author Jikoo, Dublek
 */
public class Captcha extends Module {

	private static final String HASH_PREFIX = ChatColor.DARK_AQUA.toString() + ChatColor.YELLOW + ChatColor.LIGHT_PURPLE;

	private final LoadingCache<String, ItemStack> cache;

	public Captcha(Easterlyn plugin) {
		super(plugin);
		this.cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
				.removalListener((RemovalListener<String, ItemStack>) notification -> save(notification.getKey(), notification.getValue()))
				.build(new CacheLoader<String, ItemStack>() {

					@Override
					public ItemStack load(String hash) throws Exception {
						File folder = new File(getPlugin().getDataFolder(), "captcha");
						if (!folder.exists()) {
							folder.mkdirs();
						}
						File file = new File(folder, hash);
						if (!file.exists()) {
							throw new FileNotFoundException();
						}
						try (BukkitObjectInputStream stream = new BukkitObjectInputStream(
								new FileInputStream(file))) {
							return ((ItemStack) stream.readObject()).clone();
						}
					}

				});
	}

	@Override
	protected void onEnable() {
		ManaMappings.getMana();

		// Add the Captcha recipe
		ItemStack captchaItem = getBlankCaptchacard();
		captchaItem.setAmount(3);
		ShapedRecipe captchaRecipe = new ShapedRecipe(new NamespacedKey(this.getPlugin(), "captcha"), captchaItem);
		captchaRecipe.shape("AA", "AA", "AA");
		captchaRecipe.setIngredient('A', Material.PAPER);
		Bukkit.addRecipe(captchaRecipe);

	}

	@Override
	protected void onDisable() {
		cache.invalidateAll();
	}

	@Override
	public boolean isRequired() {
		return false;
	}

	@Override
	public String getName() {
		return "Captcha";
	}

	public boolean addCustomHash(String hash, ItemStack item) {
		if (getItemByHash(hash) != null) {
			return false;
		}
		cache.put(hash, item);
		return true;
	}

	public String calculateHashForItem(ItemStack item) {
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

	private String getHashByItem(ItemStack item) {
		item = item.clone();
		String itemHash = calculateHashForItem(item);
		this.cache.put(itemHash, item);
		this.save(itemHash, item);
		return itemHash;
	}

	private void save(String hash, ItemStack item) {
		try {
			File folder = new File(getPlugin().getDataFolder(), "captcha");
			if (!folder.exists()) {
				folder.mkdirs();
			}
			File file = new File(folder, hash);
			if (file.exists()) {
				return;
			}
			try (BukkitObjectOutputStream stream = new BukkitObjectOutputStream(new FileOutputStream(file))) {
				stream.writeObject(item);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private ItemStack getItemByHash(String hash) {
		try {
			return cache.get(hash).clone();
		} catch (ExecutionException e) {
			if (!(e.getCause() instanceof FileNotFoundException)) {
				e.printStackTrace();
			}
			return null;
		}
	}

	/**
	 * Creates a blank captchacard
	 *
	 * @return the blank captchacard ItemStack
	 */
	public static ItemStack getBlankCaptchacard() {
		ItemStack is = new ItemStack(Material.BOOK);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("Captchacard");
		im.setLore(Collections.singletonList("Blank"));
		is.setItemMeta(im);
		return is;
	}

	/**
	 * Creates a blank cruxite dowel.
	 *
	 * @return the blank dowel ItemStack
	 */
	public static ItemStack getBlankDowel() {
		ItemStack dowel = new ItemStack(Material.NETHER_BRICK);
		ItemMeta meta = dowel.getItemMeta();
		meta.setDisplayName(ChatColor.WHITE + "Cruxite Totem");
		meta.setLore(Collections.singletonList(Captcha.HASH_PREFIX + "00000000"));
		dowel.setItemMeta(meta);
		return dowel;
	}

	public ItemStack getCaptchaForHash(String hash) {
		ItemStack item = getItemByHash(hash);
		if (item == null || item.getType() == Material.AIR) {
			return null;
		}
		ItemStack card = getBlankCaptchacard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta meta = item.getItemMeta();
		ArrayList<String> cardLore = new ArrayList<>();
		StringBuilder builder = new StringBuilder().append(Language.getColor("emphasis.neutral")).append(item.getAmount()).append(' ');
		if (isCaptcha(item)) {
			builder.append("Captcha of ").append(meta.getLore().get(0));
		} else if (meta.hasDisplayName() && !InventoryUtils.isMisleadinglyNamed(meta.getDisplayName(), item.getType(), item.getDurability())) {
			builder.append(meta.getDisplayName());
		} else {
			builder.append(InventoryUtils.getItemName(item));
		}
		cardLore.add(builder.toString());
		if (item.getType().getMaxDurability() > 0) {
			builder.delete(0, builder.length());
			builder.append(Language.getColor("neutral")).append("Durability: ").append(Language.getColor("emphasis.neutral"))
					.append(item.getType().getMaxDurability() - item.getDurability())
					.append(Language.getColor("neutral")).append("/").append(Language.getColor("emphasis.neutral"))
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
	 * Converts an ItemStack into a captchacard.
	 *
	 * @param item the ItemStack to convert
	 *
	 * @return the captchacard representing by this ItemStack
	 */
	public ItemStack getCaptchaForItem(ItemStack item) {
		return getCaptchaForHash(getHashByItem(item));
	}

	/**
	 * Converts a captchacard into an ItemStack. Also used for punchcards and
	 * cruxite dowels.
	 *
	 * @param captcha the captchacard ItemStack
	 *
	 * @return the ItemStack represented by this captchacard
	 */
	public ItemStack getItemForCaptcha(ItemStack captcha) {
		if (captcha == null) {
			return null;
		}
		if (!isCaptcha(captcha) && !isPunch(captcha)) {
			// Not a card.
			captcha = captcha.clone();
			captcha.setAmount(1);
			return captcha;
		}
		for (String lore : captcha.getItemMeta().getLore()) {
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
		captcha = captcha.clone();
		captcha.setAmount(1);
		return captcha;
	}

	/**
	 * Converts a captchacard into a punchcard.
	 * <p>
	 * Good luck patching punched holes.
	 *
	 * @param captcha the captchacard ItemStack
	 *
	 * @return the punched captchacard
	 */
	public ItemStack getPunchForCaptcha(ItemStack captcha, Effects effects) {
		if (!isCaptcha(captcha)) {
			return captcha;
		}
		captcha = captcha.clone();
		if (Captcha.isBlankCaptcha(captcha)) {
			return captcha;
		}
		ItemStack item = getItemForCaptcha(captcha);
		if (isCaptcha(item) || ManaMappings.expCost(effects, item) == Integer.MAX_VALUE) {
			return captcha;
		}
		captcha = getCaptchaForItem(item);
		ItemMeta im = captcha.getItemMeta();
		im.setDisplayName("Punchcard");
		captcha.setItemMeta(im);
		return captcha;
	}

	/**
	 * Converts a punchcard into a totem.
	 *
	 * @param punch the punchcard ItemStack
	 *
	 * @return the totem
	 */
	public ItemStack getTotemForPunch(ItemStack punch) {
		ItemStack dowel = getBlankDowel();
		if (!isPunch(punch)) {
			return dowel;
		}
		ItemMeta im = dowel.getItemMeta();
		im.setLore(punch.getItemMeta().getLore());
		dowel.setItemMeta(im);
		return dowel;
	}

	/**
	 * Check if an ItemStack is a blank captchacard.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a blank captchacard
	 */
	public static boolean isBlankCaptcha(ItemStack item) {
		return isCaptcha(item) && item.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack is a valid captchacard that has been used.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a captchacard
	 */
	public static boolean isUsedCaptcha(ItemStack item) {
		return isCaptcha(item) && !item.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack can be turned into a captchacard. The only items that cannot be put
	 * into a captcha are other captchas of captchas and unique Machine key items.
	 *
	 * @param item the ItemStack to check
	 * @return true if the ItemStack can be saved as a captchacard
	 */
	public boolean canNotCaptcha(ItemStack item) {
		if (item == null || item.getType() == Material.AIR
				/* Book meta is very volatile, no reason to allow creation of codes that will never be reused. */
				|| item.getType() == Material.WRITABLE_BOOK
				|| item.getType() == Material.WRITTEN_BOOK) {
			return true;
		}
		for (ItemStack is : InventoryUtils.getUniqueItems(getPlugin())) {
			if (is.isSimilar(item)) {
				return true;
			}
		}
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof BlockStateMeta && ((BlockStateMeta) meta).getBlockState() instanceof InventoryHolder) {
				return true;
			}
		}
		return isUsedCaptcha(item) && item.getItemMeta().getLore().get(0).matches("^(.3-?[0-9]+ Captcha of )+.+$");
	}

	/**
	 * Checks if an ItemStack is a captchacard.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a captchacard
	 */
	public static boolean isCaptcha(ItemStack item) {
		if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		return meta.hasLore() && meta.hasDisplayName() && meta.getDisplayName().equals("Captchacard");
	}

	/**
	 * Checks if an ItemStack is a punchcard.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a punchcard
	 */
	public static boolean isPunch(ItemStack item) {
		if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		return meta.hasLore() && meta.hasDisplayName() && meta.getDisplayName().equals("Punchcard");
	}

	/**
	 * Checks if an ItemStack is a dowel.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a dowel
	 */
	public static boolean isDowel(ItemStack item) {
		if (item == null || item.getType() != Material.NETHER_BRICK || !item.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		return meta.hasLore() && meta.hasDisplayName()
				&& meta.getDisplayName().equals(ChatColor.WHITE + "Cruxite Totem");
	}

	@SuppressWarnings("deprecation")
	public void handleCaptcha(InventoryClickEvent event) {
		if (!this.isEnabled()) {
			return;
		}
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

		if (!isBlankCaptcha(blankCaptcha) || canNotCaptcha(toCaptcha) || isBlankCaptcha(toCaptcha)) {
			return;
		}

		ItemStack captcha = getCaptchaForItem(toCaptcha);
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

	public int convert(Player player) {
		int conversions = 0;
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack is = player.getInventory().getItem(i);
			if (!Captcha.isUsedCaptcha(is)) {
				continue;
			}
			String hash = findHashIfPresent(is.getItemMeta().getLore());
			if (hash == null) {
				continue;
			}
			ItemStack storedItem = this.getItemByHash(hash);
			if (storedItem == null) {
				continue;
			}
			if (Captcha.isUsedCaptcha(storedItem)) {
				// Properly convert contents of double captchas
				int amount = storedItem.getAmount();
				String internalHash = this.findHashIfPresent(storedItem.getItemMeta().getLore());
				if (internalHash == null) {
					continue;
				}
				ItemStack convertedItem = this.getItemByHash(internalHash);
				if (convertedItem == null) {
					continue;
				}
				String newInternalHash = this.getHashByItem(convertedItem);
				storedItem = this.getCaptchaForHash(newInternalHash);
				storedItem.setAmount(amount);
			}
			String newHash = this.getHashByItem(storedItem);
			if (!newHash.equals(hash)) {
				int amount = is.getAmount();
				ItemStack captchas = this.getCaptchaForItem(storedItem);
				captchas.setAmount(amount);
				player.getInventory().setItem(i, captchas);
				conversions += amount;
			}
		}
		return conversions;
	}

	private String findHashIfPresent(List<String> lore) {
		for (String line : lore) {
			if (!line.startsWith(HASH_PREFIX)) {
				continue;
			}
			line = line.substring(HASH_PREFIX.length());
			if (line.matches("[0-9A-Za-z]{8,}")) {
				// Modern format
				return line;
			}
		}
		return null;
	}

}
