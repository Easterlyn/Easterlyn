package com.easterlyn;

import com.easterlyn.captcha.CaptchaListener;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.event.Event;
import com.easterlyn.util.inventory.ItemUtil;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EasterlynCaptchas extends JavaPlugin {

	private static final String OLD_HASH_PREFIX = ChatColor.DARK_AQUA.toString() + ChatColor.YELLOW + ChatColor.LIGHT_PURPLE;
	private static final String HASH_PREFIX = ChatColor.LIGHT_PURPLE.toString();
	public static final String RECIPE_KEY = ItemUtil.UNIQUE_KEYED_PREFIX + "captcha_uncraft";

	private final LoadingCache<String, ItemStack> cache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.MINUTES)
			.removalListener((RemovalListener<String, ItemStack>) notification -> save(notification.getKey(), notification.getValue()))
			.build(new CacheLoader<>() {

				@Override
				public ItemStack load(@NotNull String hash) throws Exception {
					File captchaFolder = new File(getDataFolder(), "captcha");
					if (!captchaFolder.exists() && !captchaFolder.mkdirs()) {
						throw new FileNotFoundException();
					}
					File file = new File(captchaFolder, hash + ".nbt");
					if (!file.exists()) {
						throw new FileNotFoundException();
					}
					try (BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
						return ItemUtil.getAsItem(inputStream);
					}
				}

			});

	@Override
	public void onEnable() {
		getServer().getServicesManager().register(EasterlynCaptchas.class, this, this, ServicePriority.Normal);

		// Add the captchacard recipes
		for (int i = 1; i < 5; ++i) {
			ItemStack captchaItem = getBlankCaptchacard();
			captchaItem.setAmount(2 * i);
			ShapelessRecipe captchaRecipe = new ShapelessRecipe(new NamespacedKey(this, "captcha" + i), captchaItem);
			captchaRecipe.addIngredient(2 * i, Material.PAPER);
			getServer().addRecipe(captchaRecipe);
		}

		ShapelessRecipe uncaptchaRecipe = new ShapelessRecipe(new NamespacedKey(this, RECIPE_KEY), new ItemStack(Material.DIRT));
		uncaptchaRecipe.addIngredient(Material.BOOK);
		getServer().addRecipe(uncaptchaRecipe);

		// TODO allow crafting x + blank captcha to captcha

		RegisteredServiceProvider<EasterlynCore> registration = getServer().getServicesManager().getRegistration(EasterlynCore.class);
		if (registration != null) {
			register(registration.getProvider());
		}

		Event.register(PluginEnableEvent.class, event -> {
			if (event.getPlugin() instanceof EasterlynCore) {
				register((EasterlynCore) event.getPlugin());
			}
		}, this);

		// TODO quote matcher for {captcha:code}

		getServer().getPluginManager().registerEvents(new CaptchaListener(this), this);
	}

	/**
	 * Check if an ItemStack is a blank captchacard.
	 *
	 * @param item the ItemStack to check
	 *
	 * @return true if the ItemStack is a blank captchacard
	 */
	public static boolean isBlankCaptcha(@Nullable ItemStack item) {
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
	public static boolean isUsedCaptcha(@Nullable ItemStack item) {
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
	public boolean canNotCaptcha(@Nullable ItemStack item) {
		if (item == null || item.getType() == Material.AIR
				// Book meta is very volatile, no reason to allow creation of codes that will never be reused.
				|| item.getType() == Material.WRITABLE_BOOK
				|| item.getType() == Material.WRITTEN_BOOK
				// Knowledge book is specifically for usage, not for storage.
				|| item.getType() == Material.KNOWLEDGE_BOOK) {
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
	public static boolean isCaptcha(@Nullable ItemStack item) {
		if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) {
			return false;
		}
		ItemMeta meta = item.getItemMeta();
		return meta != null && meta.hasLore() && meta.hasDisplayName() && meta.getDisplayName().equals("Captchacard");
	}

	public boolean addCustomHash(@NotNull String hash, @NotNull ItemStack item) {
		if (getItemByHash(hash) != null) {
			return false;
		}
		cache.put(hash, item);
		return true;
	}

	public @NotNull static ItemStack getBlankCaptchacard() {
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
	public @Nullable ItemStack getCaptchaForItem(@NotNull ItemStack item) {
		return getCaptchaForHash(getHashByItem(item));
	}

	public @Nullable ItemStack getCaptchaForHash(@NotNull String hash) {
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
	public @Nullable ItemStack getItemByCaptcha(@Nullable ItemStack captcha) {
		if (captcha == null) {
			return null;
		}

		String hashFromCaptcha = getHashFromCaptcha(captcha);

		if (hashFromCaptcha != null) {
			ItemStack item = getItemByHash(hashFromCaptcha);
			if (item != null) {
				return item;
			}
		}

		captcha = captcha.clone();
		captcha.setAmount(1);
		return captcha;
	}

	public static @Nullable String getHashFromCaptcha(@Nullable ItemStack captcha) {
		if (captcha == null) {
			return null;
		}
		if (!isCaptcha(captcha)) {
			// Not a card.
			return null;
		}
		//noinspection ConstantConditions // Must have lore or isCaptcha would fail.
		return findHashIfPresent(captcha.getItemMeta().getLore());
	}

	private @Nullable ItemStack getItemByHash(@NotNull String hash) {
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
	private String getHashByItem(@NotNull ItemStack item) {
		item = item.clone();
		String itemHash = calculateHashForItem(item);
		this.save(itemHash, item);
		this.cache.put(itemHash, item);
		return itemHash;
	}

	@NotNull
	public String calculateHashForItem(@NotNull ItemStack item) {
		String itemString = new TextComponent(ItemUtil.getAsText(item)).toString();
		BigInteger hash = NumberUtil.md5(itemString);
		String itemHash = NumberUtil.getBase(hash, 62, 8);
		ItemStack captcha;
		while ((captcha = getItemByHash(itemHash)) != null && !captcha.equals(item)) {
			hash = hash.add(BigInteger.ONE);
			itemHash = NumberUtil.getBase(hash, 62, 8);
		}
		return itemHash;
	}

	public int convert(@NotNull Player player) {
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
				String internalHash = findHashIfPresent(storedItem.getItemMeta().getLore());
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
				if (captchas == null) {
					continue;
				}
				captchas.setAmount(amount);
				player.getInventory().setItem(i, captchas);
				conversions += amount;
			}
		}
		return conversions;
	}

	@Nullable
	private static String findHashIfPresent(List<String> lore) {
		for (String line : lore) {
			if (line.startsWith(HASH_PREFIX)) {
				line = line.substring(HASH_PREFIX.length());
			} else if (line.startsWith(OLD_HASH_PREFIX)) {
				line = line.substring(OLD_HASH_PREFIX.length());
			} else {
				continue;
			}
			if (line.matches("[0-9A-Za-z]{8,}")) {
				// Modern format
				return line;
			}
		}
		return null;
	}

	private void save(@NotNull String hash, @NotNull ItemStack item) {
		try {
			File captchaFolder = new File(getDataFolder(), "captcha");
			if (!captchaFolder.exists() && !captchaFolder.mkdirs()) {
				ReportableEvent.call("Unable to create captcha directory!");
				return;
			}
			File file = new File(captchaFolder, hash + ".nbt");
			if (file.exists()) {
				return;
			}
			ItemUtil.writeItemToFile(item, file);
		} catch (IOException e) {
			ReportableEvent.call("Caught IOException saving captcha:", e, 5);
		}
	}

	private void register(EasterlynCore plugin) {
		plugin.getCommandManager().getCommandCompletions().registerStaticCompletion("captcha", () -> {
			File captchaFolder = new File(getDataFolder(), "captcha");
			if (!captchaFolder.exists() && !captchaFolder.mkdirs()) {
				return Collections.emptyList();
			}
			String[] captchaHashes = captchaFolder.list();
			if (captchaHashes == null) {
				return Collections.emptyList();
			}
			return Arrays.asList(captchaHashes);
		});

		plugin.registerCommands(this, getClassLoader(), "com.easterlyn.captcha.command");
		ItemUtil.addUniqueCheck(EasterlynCaptchas::isCaptcha);
		plugin.getLocaleManager().addLocaleSupplier(this);
	}

}
