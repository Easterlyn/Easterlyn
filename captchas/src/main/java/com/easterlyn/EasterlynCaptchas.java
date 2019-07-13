package com.easterlyn;

import com.easterlyn.captcha.CaptchaListener;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.event.SimpleListener;
import com.easterlyn.util.inventory.ItemUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasterlynCaptchas extends JavaPlugin {

	private static final String HASH_PREFIX = ChatColor.DARK_AQUA.toString() + ChatColor.YELLOW + ChatColor.LIGHT_PURPLE;
	private static final String RECIPE_KEY = ItemUtil.UNIQUE_KEYED_PREFIX + "captcha_uncraft";

	private final LoadingCache<String, ItemStack> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
			.removalListener((RemovalListener<String, ItemStack>) notification -> save(notification.getKey(), notification.getValue()))
			.build(new CacheLoader<String, ItemStack>() {

				@Override
				public ItemStack load(@NotNull String hash) throws Exception {
					File file = new File(EasterlynCaptchas.this.getDataFolder().getPath() + File.separator + "captcha", hash);
					if (!file.exists()) {
						throw new FileNotFoundException();
					}
					try (BukkitObjectInputStream stream = new BukkitObjectInputStream(
							new FileInputStream(file))) {
						return ((ItemStack) stream.readObject()).clone();
					}
				}

			});

	@Override
	public void onEnable() {
		getServer().getServicesManager().register(EasterlynCaptchas.class, this, this, ServicePriority.Normal);

		// Add the captchacard recipes
		ItemStack captchaItem = getBlankCaptchacard();
		ShapelessRecipe captchaRecipe = new ShapelessRecipe(new NamespacedKey(this, "captcha1"), captchaItem);
		captchaRecipe.addIngredient(2, Material.PAPER);
		getServer().addRecipe(captchaRecipe);
		captchaItem = captchaItem.clone();
		captchaItem.setAmount(2);
		captchaRecipe = new ShapelessRecipe(new NamespacedKey(this, "captcha2"), captchaItem);
		captchaRecipe.addIngredient(4, Material.PAPER);
		getServer().addRecipe(captchaRecipe);
		captchaItem = captchaItem.clone();
		captchaItem.setAmount(3);
		captchaRecipe = new ShapelessRecipe(new NamespacedKey(this, "captcha3"), captchaItem);
		captchaRecipe.addIngredient(6, Material.PAPER);
		getServer().addRecipe(captchaRecipe);
		captchaItem = captchaItem.clone();
		captchaItem.setAmount(4);
		captchaRecipe = new ShapelessRecipe(new NamespacedKey(this, "captcha4"), captchaItem);
		captchaRecipe.addIngredient(8, Material.PAPER);
		getServer().addRecipe(captchaRecipe);

		captchaRecipe = new ShapelessRecipe(new NamespacedKey(this, RECIPE_KEY), new ItemStack(Material.DIRT));
		captchaRecipe.addIngredient(Material.BOOK);
		getServer().addRecipe(captchaRecipe);

		// TODO all listeners -> CaptchaListener
		PrepareItemCraftEvent.getHandlerList().register(new SimpleListener<>(PrepareItemCraftEvent.class, event -> {
			if (event.getRecipe() instanceof Keyed
					&& ((Keyed) event.getRecipe()).getKey().getKey().equals(RECIPE_KEY)) {
				for (ItemStack itemStack : event.getInventory().getMatrix()) {
					if (itemStack == null || itemStack.getType() == Material.AIR) {
						continue;
					}
					if (!isUsedCaptcha(itemStack)) {
						event.getInventory().setResult(ItemUtil.AIR);
					} else {
						event.getInventory().setResult(getItemByCaptcha(itemStack));
					}
					return;
				}
			}
		}, this));

		CraftItemEvent.getHandlerList().register(new SimpleListener<>(CraftItemEvent.class, event -> {
			if (event.getRecipe() instanceof Keyed
					&& ((Keyed) event.getRecipe()).getKey().getKey().equals(RECIPE_KEY)) {
				for (ItemStack itemStack : event.getInventory().getMatrix()) {
					if (itemStack == null || itemStack.getType() == Material.AIR) {
						continue;
					}
					if (!isUsedCaptcha(itemStack)) {
						event.setCurrentItem(ItemUtil.AIR);
					} else {
						event.setCurrentItem(getItemByCaptcha(itemStack));
					}
					return;
				}
			}
		}, this));

		PrepareItemEnchantEvent.getHandlerList().register(new SimpleListener<>(PrepareItemEnchantEvent.class, event -> {
			if (isCaptcha(event.getItem())) {
				event.setCancelled(true);
			}
		}, this));

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		PluginEnableEvent.getHandlerList().register(new SimpleListener<>(PluginEnableEvent.class, event -> {
			if (event.getPlugin() instanceof EasterlynCore) {
				register((EasterlynCore) event.getPlugin());
			}
		}, this));

		getServer().getPluginManager().registerEvents(new CaptchaListener(this), this);
	}

	/**
	 * Check if an ItemStack is a blank captchacard.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a blank captchacard
	 */
	public static boolean isBlankCaptcha(ItemStack item) {
		//noinspection ConstantConditions // Must have lore or isCaptcha would fail.
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
		//noinspection ConstantConditions // Must have lore or isCaptcha would fail.
		return isCaptcha(item) && !item.getItemMeta().getLore().contains("Blank");
	}

	/**
	 * Check if an ItemStack cannot be turned into a captchacard. The only items that cannot be put
	 * into a captcha are other captchas of captchas, books, blocks with inventories, and unique items.
	 *
	 * @param item the ItemStack to check
	 * @return true if the ItemStack cannot be saved as a captchacard
	 */
	public boolean canNotCaptcha(ItemStack item) {
		if (item == null || item.getType() == Material.AIR
				/* Book meta is very volatile, no reason to allow creation of codes that will never be reused. */
				|| item.getType() == Material.WRITABLE_BOOK
				|| item.getType() == Material.WRITTEN_BOOK) {
			return true;
		}
		if (item.hasItemMeta()) {
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof BlockStateMeta && ((BlockStateMeta) meta).getBlockState() instanceof InventoryHolder) {
				return true;
			}
		}
		//noinspection ConstantConditions // Must have lore or isUsedCaptcha would fail.
		return ItemUtil.isUniqueItem(item) || isUsedCaptcha(item) && item.getItemMeta().getLore().get(0).matches("^(.3-?[0-9]+ Captcha of )+.+$");
	}

	/**
	 * Checks if an ItemStack is a captchacard.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a captchacard
	 */
	private static boolean isCaptcha(ItemStack item) {
		if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasLore() && meta.hasDisplayName() && meta.getDisplayName().equals("Captchacard");
	}

	public boolean addCustomHash(String hash, ItemStack item) {
		if (getItemByHash(hash) != null) {
			return false;
		}
		cache.put(hash, item);
		return true;
	}

	@NotNull
	public static ItemStack getBlankCaptchacard() {
		ItemStack itemStack = new ItemStack(Material.BOOK);
		ItemMeta itemMeta = Objects.requireNonNull(itemStack.getItemMeta());
		itemMeta.setDisplayName("Captchacard");
		itemMeta.setLore(Collections.singletonList("Blank"));
		itemStack.setItemMeta(itemMeta);
		return itemStack;
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

	@Nullable
	public ItemStack getCaptchaForHash(@NotNull String hash) {
		ItemStack item = getItemByHash(hash);
		if (item == null || item.getType() == Material.AIR) {
			return null;
		}
		ItemStack card = getBlankCaptchacard();
		ItemMeta cardMeta = card.getItemMeta();
		ItemMeta meta = item.getItemMeta();
		if (cardMeta == null || meta == null) {
			return null;
		}
		ArrayList<String> cardLore = new ArrayList<>();
		StringBuilder builder = new StringBuilder().append(ChatColor.DARK_AQUA).append(item.getAmount()).append(' ');
		if (isCaptcha(item)) {
			//noinspection ConstantConditions // Must have lore or isCaptcha would fail.
			builder.append("Captcha of ").append(meta.getLore().get(0));
		} else if (meta.hasDisplayName() && !ItemUtil.isMisleadinglyNamed(meta.getDisplayName(), item.getType())) {
			builder.append(meta.getDisplayName());
		} else {
			builder.append(ItemUtil.getItemName(item));
		}
		cardLore.add(builder.toString());
		if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable) {
			builder.delete(0, builder.length());
			builder.append(ChatColor.YELLOW).append("Durability: ").append(ChatColor.DARK_AQUA)
					.append(item.getType().getMaxDurability() - ((Damageable) meta).getDamage())
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
	 * Converts a captchacard into an ItemStack. Also used for punchcards and
	 * cruxite dowels.
	 *
	 * @param captcha the captchacard ItemStack
	 *
	 * @return the ItemStack represented by this captchacard
	 */
	public ItemStack getItemByCaptcha(ItemStack captcha) {
		if (captcha == null) {
			return null;
		}
		if (!isCaptcha(captcha)) {
			// Not a card.
			captcha = captcha.clone();
			captcha.setAmount(1);
			return captcha;
		}
		//noinspection ConstantConditions // Must have lore or isCaptcha would fail.
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

	@Nullable
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

	@NotNull
	private String getHashByItem(ItemStack item) {
		item = item.clone();
		String itemHash = calculateHashForItem(item);
		this.cache.put(itemHash, item);
		this.save(itemHash, item);
		return itemHash;
	}

	@NotNull
	private String calculateHashForItem(ItemStack item) {
		String itemString = new TextComponent(StringUtil.getItemText(item)).toString();
		BigInteger hash = NumberUtil.md5(itemString);
		String itemHash = NumberUtil.getBase(hash, 62, 8);
		ItemStack captcha;
		while ((captcha = getItemByHash(itemHash)) != null && !captcha.equals(item)) {
			hash = hash.add(BigInteger.ONE);
			itemHash = NumberUtil.getBase(hash, 62, 8);
		}
		return itemHash;
	}

	public int convert(Player player) {
		int conversions = 0;
		for (int i = 0; i < player.getInventory().getSize(); i++) {
			ItemStack is = player.getInventory().getItem(i);
			if (!isUsedCaptcha(is)) {
				continue;
			}
			//noinspection ConstantConditions // Must have lore or isUsedCaptcha would fail.
			String hash = findHashIfPresent(is.getItemMeta().getLore());
			if (hash == null) {
				continue;
			}
			ItemStack storedItem = this.getItemByHash(hash);
			if (storedItem == null) {
				continue;
			}
			if (isUsedCaptcha(storedItem)) {
				// Properly convert contents of double captchas
				int amount = storedItem.getAmount();
				//noinspection ConstantConditions // Must have lore or isUsedCaptcha would fail.
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
				if (storedItem == null) {
					continue;
				}
				storedItem.setAmount(amount);
			}
			String newHash = this.getHashByItem(storedItem);
			if (!newHash.equals(hash)) {
				int amount = is.getAmount();
				ItemStack captchas = getCaptchaForItem(storedItem);
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

	private void save(String hash, ItemStack item) {
		try {
			File file = new File(getDataFolder().getPath() + File.separator + "captcha", hash);
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

	private void register(EasterlynCore plugin) {
		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.captcha.command");
		ItemUtil.addUniqueCheck(EasterlynCaptchas::isCaptcha);
	}

}
