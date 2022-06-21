package com.easterlyn;

import com.easterlyn.captcha.CaptchaListener;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.plugin.EasterlynPlugin;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.inventory.ItemUtil;
import com.github.jikoo.planarwrappers.util.StringConverters;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;
import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.ServicePriority;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A plugin adding bulk storage in the form of captchacards.
 *
 * <p>A captchacard is a single item representing up to a stack of a single other item.
 */
public class EasterlynCaptchas extends EasterlynPlugin {

  public static final String RECIPE_KEY = ItemUtil.UNIQUE_KEYED_PREFIX + "captcha_uncraft";
  public static final NamespacedKey KEY_SKIP_CONVERT =
      Objects.requireNonNull(
          StringConverters.toNamespacedKey("captcha:skip_convert"));
  private static final NamespacedKey KEY_BLANK =
      Objects.requireNonNull(
          StringConverters.toNamespacedKey("captcha:blank"));
  private static final NamespacedKey KEY_HASH =
      Objects.requireNonNull(
          StringConverters.toNamespacedKey("captcha:hash"));
  private static final int MAX_CAPTCHA_DEPTH = 2;
  // TODO colors -> global definitions
  public static final @NonNull ChatColor DARKER_PURPLE = ChatColor.of(new Color(85, 0, 85));
  public static final @NonNull ChatColor DARK_AQUA = ChatColor.of(new Color(0, 170, 170));
  public static final @NonNull ChatColor YELLOW = ChatColor.of(new Color(255, 255, 85));

  private final LoadingCache<String, ItemStack> cache =
      CacheBuilder.newBuilder()
          .expireAfterAccess(30, TimeUnit.MINUTES)
          .removalListener(
              (RemovalListener<String, ItemStack>) notification -> {
                if (notification.getKey() != null && notification.getValue() != null) {
                  save(notification.getKey(), notification.getValue());
                }
              })
          .build(
              new CacheLoader<>() {
                @Override
                public @NotNull ItemStack load(@NotNull String hash) throws Exception {
                  File captchaFolder = new File(getDataFolder(), "captcha");
                  if (!captchaFolder.exists() && !captchaFolder.mkdirs()) {
                    throw new FileNotFoundException();
                  }
                  File file = new File(captchaFolder, hash + ".nbt");
                  if (!file.exists()) {
                    throw new FileNotFoundException();
                  }
                  try (BufferedInputStream inputStream =
                      new BufferedInputStream(new FileInputStream(file))) {
                    return ItemUtil.getAsItem(inputStream);
                  }
                }
              });

  /**
   * Check if an ItemStack is a blank captchacard.
   *
   * @param item the ItemStack to check
   * @return true if the ItemStack is a blank captchacard
   */
  @Contract("null -> false")
  public static boolean isBlankCaptcha(@Nullable ItemStack item) {
    if (!isCaptcha(item)) {
      return false;
    }
    ItemMeta itemMeta = item.getItemMeta();
    return itemMeta != null
        && itemMeta.getPersistentDataContainer().has(KEY_BLANK, PersistentDataType.BYTE);
  }

  /**
   * Check if an ItemStack is a valid captchacard that has been used.
   *
   * @param item the ItemStack to check
   * @return true if the ItemStack is a captchacard
   */
  @Contract("null -> false")
  public static boolean isUsedCaptcha(@Nullable ItemStack item) {
    if (!isCaptcha(item)) {
      return false;
    }
    ItemMeta itemMeta = item.getItemMeta();
    return itemMeta != null
        && itemMeta.getPersistentDataContainer().has(KEY_HASH, PersistentDataType.STRING);
  }

  /**
   * Check if an ItemStack is a captchacard.
   *
   * @param item the ItemStack to check
   * @return true if the ItemStack is a captchacard
   */
  @Contract("null -> false")
  public static boolean isCaptcha(@Nullable ItemStack item) {
    if (item == null || item.getType() != Material.BOOK || !item.hasItemMeta()) {
      return false;
    }
    ItemMeta meta = item.getItemMeta();
    return meta != null
        && meta.hasLore()
        && meta.hasDisplayName()
        && "Captchacard".equals(meta.getDisplayName());
  }

  /**
   * Create a new blank captchacard.
   *
   * @return the new blank captchacard
   */
  public static @NotNull ItemStack getBlankCaptchacard() {
    ItemStack itemStack = new ItemStack(Material.BOOK);
    ItemMeta itemMeta = itemStack.getItemMeta();
    if (itemMeta != null) {
      itemMeta.setDisplayName("Captchacard");
      itemMeta.setLore(List.of("Blank"));
      itemMeta.getPersistentDataContainer().set(KEY_BLANK, PersistentDataType.BYTE, (byte) 0);
    }
    itemStack.setItemMeta(itemMeta);
    return itemStack;
  }

  /**
   * Find the hash stored in the given captchacard.
   *
   * @param captcha the captchacard
   * @return the hash or {@code null}
   */
  @Contract("null -> null")
  public static @Nullable String getHashFromCaptcha(@Nullable ItemStack captcha) {
    if (captcha == null) {
      return null;
    }
    if (!isCaptcha(captcha)) {
      // Not a card.
      return null;
    }
    ItemMeta itemMeta = captcha.getItemMeta();
    if (itemMeta == null) {
      return null;
    }

    return itemMeta.getPersistentDataContainer().get(KEY_HASH, PersistentDataType.STRING);
  }

  @Override
  protected void enable() {
    getServer()
        .getServicesManager()
        .register(EasterlynCaptchas.class, this, this, ServicePriority.Normal);

    // Add the captchacard recipes
    for (int i = 1; i < 5; ++i) {
      ItemStack captchaItem = getBlankCaptchacard();
      captchaItem.setAmount(2 * i);
      ShapelessRecipe captchaRecipe =
          new ShapelessRecipe(new NamespacedKey(this, "captcha" + i), captchaItem);
      captchaRecipe.addIngredient(2 * i, Material.PAPER);
      captchaRecipe.setGroup("captcha");
      getServer().addRecipe(captchaRecipe);
    }

    ShapelessRecipe uncaptchaRecipe =
        new ShapelessRecipe(new NamespacedKey(this, RECIPE_KEY), new ItemStack(Material.DIRT));
    uncaptchaRecipe.addIngredient(Material.BOOK);
    getServer().addRecipe(uncaptchaRecipe);

    // TODO
    //  - allow crafting x + blank captcha to captcha
    //  - quote matcher for {captcha:code}

    getServer().getPluginManager().registerEvents(new CaptchaListener(this), this);
  }

  /**
   * Check if an ItemStack cannot be turned into a captchacard. The only items that cannot be put
   * into a captcha are other captchas of captchas, books, blocks with inventories, and unique
   * items.
   *
   * @param item the ItemStack to check
   * @return true if the ItemStack cannot be saved as a captchacard
   */
  public boolean canNotCaptcha(@Nullable ItemStack item) {
    if (item == null
        || item.getType() == Material.AIR
        // Book meta has high churn, no reason to allow creation of codes that will never be reused.
        || item.getType() == Material.WRITABLE_BOOK
        || item.getType() == Material.WRITTEN_BOOK
        // Knowledge book is specifically for usage, not for storage.
        || item.getType() == Material.KNOWLEDGE_BOOK) {
      return true;
    }
    if (item.hasItemMeta()) {
      ItemMeta meta = item.getItemMeta();
      if (meta instanceof BlockStateMeta
          && ((BlockStateMeta) meta).getBlockState() instanceof InventoryHolder) {
        return true;
      }
    }
    return ItemUtil.isUniqueItem(item)
        || isUsedCaptcha(item)
            && getCaptchaDepth(item) >= MAX_CAPTCHA_DEPTH;
  }

  /**
   * Calculate captcha depth. This is the number of times a captcha must be undone to arrive at the
   * original stored item.
   *
   * @param item the captchacard
   * @return the captcha depth
   */
  public int getCaptchaDepth(@Nullable ItemStack item) {
    if (!isUsedCaptcha(item)) {
      return 0;
    }
    int depth = 1;
    ItemStack newItem;
    while (isUsedCaptcha((newItem = getItemByCaptcha(item)))) {
      if (newItem.isSimilar(item)) {
        return depth;
      }
      ++depth;
      item = newItem;
    }
    return depth;
  }

  /**
   * Convert an ItemStack into a captchacard.
   *
   * @param item the ItemStack to convert
   * @return the captchacard representing by this ItemStack
   */
  public @Nullable ItemStack getCaptchaForItem(@NotNull ItemStack item) {
    return getCaptchaForHash(getHashByItem(item));
  }

  /**
   * Get a captchacard for the specified hash.
   *
   * @param hash the hash
   * @return the captchacard
   */
  public @Nullable ItemStack getCaptchaForHash(@NotNull String hash) {
    ItemStack item = getItemByHash(hash);

    // Item not stored.
    if (item == null || item.getType() == Material.AIR) {
      return null;
    }

    // Get a new blank card to manipulate.
    ItemStack card = getBlankCaptchacard();
    ItemMeta cardMeta = card.getItemMeta();
    ItemMeta meta = item.getItemMeta();
    if (cardMeta == null || meta == null) {
      return null;
    }

    PersistentDataContainer dataContainer = cardMeta.getPersistentDataContainer();
    // Remove blank card tag.
    dataContainer.remove(KEY_BLANK);
    // Add hash to card.
    dataContainer.set(KEY_HASH, PersistentDataType.STRING, hash);

    // Add display elements for users.
    ArrayList<String> cardLore = new ArrayList<>();

    // Expose hash for fun.
    cardLore.add(DARKER_PURPLE + hash);

    // Build an amount and name element.
    String text = DARK_AQUA.toString() + item.getAmount() + " ";

    List<String> existingLore = meta.getLore();
    if (isCaptcha(item) && existingLore != null && existingLore.size() >= 2) {
      // Use existing description to form X Captcha of Y.
      text += "Captcha of " + existingLore.get(1);
      cardLore.add(text);

      // Append existing description.
      if (existingLore.size() >= 3) {
        cardLore.addAll(existingLore.subList(2, cardLore.size()));
      }
    } else {
      cardLore.add(text + ItemUtil.getItemName(item));

      if (meta.hasDisplayName()) {
        cardLore.add(meta.getDisplayName());
      }

      if (item.getType().getMaxDurability() > 0 && meta instanceof Damageable) {
        cardLore.add(YELLOW + "Durability: "
            + DARK_AQUA + (item.getType().getMaxDurability() - ((Damageable) meta).getDamage())
            + YELLOW + "/" + DARK_AQUA + item.getType().getMaxDurability());
      }
    }

    cardMeta.setDisplayName("Captchacard");
    cardMeta.setLore(cardLore);
    card.setItemMeta(cardMeta);
    return card;
  }

  /**
   * Convert a captchacard into an ItemStack.
   *
   * @param captcha the captchacard ItemStack
   * @return the ItemStack represented by this captchacard
   */
  @Contract("null -> null")
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

  /**
   * Get an item by hash. Uses cache, loading from disk as necessary.
   *
   * @param hash the hash to get an item for
   * @return the item or {@code null} if the item has not been saved
   */
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

  private @NotNull String getHashByItem(@NotNull ItemStack item) {
    item = item.clone();
    String itemHash = calculateHashForItem(item);
    this.save(itemHash, item);
    this.cache.put(itemHash, item);
    return itemHash;
  }

  /**
   * Calculate a hash for an item.
   *
   * <p>In the event of a conflict, the backing hash number will increment until a unique hash is
   * found.
   *
   * @param item the item to calculate a hash for
   * @return the calculated hash
   */
  public @NotNull String calculateHashForItem(@NotNull ItemStack item) {
    String itemString = ItemUtil.getAsText(item);
    BigInteger hash = NumberUtil.md5(itemString);
    String itemHash = NumberUtil.getBase(hash, 62, 8);
    ItemStack captcha;
    while ((captcha = getItemByHash(itemHash)) != null && !captcha.equals(item)) {
      hash = hash.add(BigInteger.ONE);
      itemHash = NumberUtil.getBase(hash, 62, 8);
    }
    return itemHash;
  }

  /**
   * Convert captchacard hashes to a newer format. Used to fix identical items not stacking after
   * changes to NBT serialization.
   *
   * @param player the player whose inventory should be checked
   * @return the number of captchacards affected
   */
  public int convert(@NotNull Player player) {
    int conversions = 0;
    AbstractSequentialList<Integer> depthAmounts = new LinkedList<>();
    for (int i = 0; i < player.getInventory().getSize(); i++) {
      ItemStack baseItem = player.getInventory().getItem(i);
      if (!isUsedCaptcha(baseItem)) {
        continue;
      }

      ItemMeta baseMeta = baseItem.getItemMeta();
      if (baseMeta == null) {
        continue;
      }

      if (baseMeta.getPersistentDataContainer().has(KEY_SKIP_CONVERT, PersistentDataType.BYTE)) {
        continue;
      }

      String originalHash = baseMeta
          .getPersistentDataContainer()
          .get(KEY_HASH, PersistentDataType.STRING);

      if (originalHash == null) {
        continue;
      }

      depthAmounts.clear();
      depthAmounts.add(baseItem.getAmount());

      String hash = originalHash;
      ItemStack storedItem;
      // Fully de-captcha stored item.
      while (isUsedCaptcha((storedItem = getItemByHash(hash)))) {
        ItemMeta storedMeta = storedItem.getItemMeta();
        if (storedMeta == null) {
          break;
        }
        hash = storedMeta
            .getPersistentDataContainer()
            .get(KEY_HASH, PersistentDataType.STRING);
        if (hash == null) {
          break;
        }
        depthAmounts.add(storedItem.getAmount());
      }

      // If stored item is null, final captcha is invalid. Ignore.
      if (storedItem == null) {
        continue;
      }

      ListIterator<Integer> depthIterator = depthAmounts.listIterator(depthAmounts.size());
      // Fully re-captcha stored item.
      while (depthIterator.hasPrevious()) {
        storedItem = getCaptchaForItem(storedItem);

        // Problem creating new captcha, ignore.
        if (storedItem == null) {
          break;
        }

        storedItem.setAmount(depthIterator.previous());
      }

      if (storedItem == null) {
        continue;
      }

      ItemMeta newMeta = storedItem.getItemMeta();

      if (newMeta == null) {
        continue;
      }

      String newHash = newMeta
          .getPersistentDataContainer()
          .get(KEY_HASH, PersistentDataType.STRING);
      if (!originalHash.equals(newHash)) {
        player.getInventory().setItem(i, storedItem);
        conversions += storedItem.getAmount();
      }
    }
    return conversions;
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

  @Override
  protected void register(@NotNull EasterlynCore plugin) {
    plugin
        .getCommandManager()
        .getCommandCompletions()
        .registerStaticCompletion(
            "captcha",
            () -> {
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
