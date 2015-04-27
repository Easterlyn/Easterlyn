package co.sblock.utilities.inventory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.common.collect.HashMultimap;
import com.google.common.io.BaseEncoding;

import com.sun.corba.se.impl.orbutil.HexOutputStream;

import co.sblock.Sblock;
import co.sblock.machines.utilities.MachineType;
import co.sblock.utilities.captcha.Captcha;
import co.sblock.utilities.captcha.CruxiteDowel;

/**
 * A set of useful methods for inventory functions.
 * 
 * @author Jikoo
 */
public class InventoryUtils {

	private static HashMap<String, String> items;
	private static HashMultimap<String, String> itemsReverse;
	private static HashSet<ItemStack> uniques;
	private static final HashMap<Integer, String> potionEffects = new HashMap<>();

	static {
		potionEffects.put(0, "Mundane Potion");
		potionEffects.put(1, "Potion of Regeneration");
		potionEffects.put(2, "Potion of Swiftness");
		potionEffects.put(3, "Potion of Fire Resistance");
		potionEffects.put(4, "Potion of Poison");
		potionEffects.put(5, "Potion of Healing");
		potionEffects.put(6, "Potion of Night Vision");
		potionEffects.put(7, "Clear Potion");
		potionEffects.put(8, "Potion of Weakness");
		potionEffects.put(9, "Potion of Strength");
		potionEffects.put(10, "Potion of Slowness");
		potionEffects.put(11, "Potion of Leaping");
		potionEffects.put(12, "Potion of Harming");
		potionEffects.put(13, "Potion of Water Breathing");
		potionEffects.put(14, "Potion of Invisibility");
		potionEffects.put(15, "Thin Potion");
		potionEffects.put(16, "Awkward Potion");
		potionEffects.put(23, "Bungling Potion");
		potionEffects.put(31, "Debonair Potion");
		potionEffects.put(32, "Thick Potion");
		potionEffects.put(39, "Charming Potion");
		potionEffects.put(47, "Sparkling Potion");
		potionEffects.put(48, "Potent Potion");
		potionEffects.put(55, "Rank Potion");
		potionEffects.put(63, "Stinky Potion");
	}

	private static HashMap<String, String> getItems() {
		if (items != null) {
			return items;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Sblock.getInstance().getResource("items.tsv")))) {
			items = new HashMap<>();
			itemsReverse = HashMultimap.create();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] column = line.split("\t");
				String id = column[0] + ":" + column[1];
				items.put(id, column[2]);
				itemsReverse.put(column[2], id);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load items from items.tsv!", e);
		}
		return items;
	}

	@SuppressWarnings("deprecation")
	public static String getMaterialDataName(Material m, short durability) {
		if (getItems().containsKey(m.getId() + ":" + durability)) {
			return items.get(m.getId() + ":" + durability);
		}
		if (m == Material.POTION) {
			return getPotionName(durability);
		}
		return items.get(m.getId() + ":" + 0);
	}

	private static String getPotionName(short durability) {
		StringBuilder potion = new StringBuilder();
		if (((durability >> 6) & 1) == 1) {
			potion.append("Extended ");
		}
		if (((durability >> 14) & 1) == 1) {
			potion.append("Splash ");
		}
		int remainder = durability % 64;
		if (potionEffects.containsKey(remainder)) {
			potion.append(potionEffects.get(remainder));
		} else {
			potion.append(potionEffects.get(remainder % 16));
		}
		if (((durability >> 5) & 1) == 1) {
			potion.append(" II");
		}
		return potion.toString();
	}

	@SuppressWarnings("deprecation")
	public static boolean isMisleadinglyNamed(String name, Material m, short durability) {
		getItems();
		String id = m.getId() + ":" + durability;
		boolean match = false;
		for (String storedId : itemsReverse.get(name)) {
			if (storedId.equals(id)) {
				return false;
			}
			match = true;
		}
		if (!match) {
			return name.matches("(\\w+ ){0,2}Potion of \\w+");
		}
		return match;
	}

	public static String serializeItemStack(ItemStack is) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(outputStream)) {
			bukkitOutputStream.writeObject(is);
			String encoded = outputStream.toString();
			return encoded;
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize ItemStack!", e);
		}
	}

	public static String serializeIntoFormattingCodes(ItemStack is) {
		String hex;
		// Using BaseEncoding to encode corrupts the stream header.
		try (StringWriter writer = new StringWriter();
				HexOutputStream hexOut = new HexOutputStream(writer);
				BukkitObjectOutputStream bukkitOut = new BukkitObjectOutputStream(hexOut)) {
			bukkitOut.writeObject(is);
			bukkitOut.close();
			hexOut.close();
			hex = writer.toString();
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize ItemStack!", e);
		}
		StringBuilder formatting = new StringBuilder();
		for (char c : hex.toCharArray()) {
			formatting.append(ChatColor.COLOR_CHAR).append(c);
		}
		return formatting.toString();
	}

	public static ItemStack deserializeItemStack(String s) {
		return deserializeItemStack(s.getBytes());
	}

	public static ItemStack deserializeItemStack(byte[] bytes) {
		try (ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
				BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(inputStream)) {
			ItemStack decoded = (ItemStack) bukkitInputStream.readObject();
			return decoded;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Unable to deserialize ItemStack!", e);
		}
	}

	public static ItemStack deserializeFromFormattingCodes(String s) {
		s = s.replace(String.valueOf(ChatColor.COLOR_CHAR), "").toUpperCase();
		return deserializeItemStack(BaseEncoding.base16().decode(s));
	}

	public static ItemStack cleanNBT(ItemStack is) {
		if (is == null) {
			return null;
		}

		ItemStack cleanedItem = new ItemStack(is.getType());
		// Why Bukkit doesn't have a constructor ItemStack(MaterialData) I don't know.
		cleanedItem.setData(is.getData());
		cleanedItem.setDurability(is.getDurability());
		cleanedItem.setAmount(is.getAmount());

		if (!is.hasItemMeta()) {
			return cleanedItem;
		}
		ItemMeta im = is.getItemMeta();

		// Banners
		if (im instanceof BannerMeta) {
			BannerMeta meta = (BannerMeta) Bukkit.getItemFactory().getItemMeta(Material.BANNER);
			meta.setBaseColor(((BannerMeta) im).getBaseColor());
			for (Pattern pattern : ((BannerMeta) im).getPatterns()) {
				meta.addPattern(pattern);
			}
			cleanedItem.setItemMeta(meta);
		}

		// Book and quill/Written books
		if (im instanceof BookMeta) {
			BookMeta meta = (BookMeta) Bukkit.getItemFactory().getItemMeta(Material.WRITTEN_BOOK);
			BookMeta bm = (BookMeta) im;
			if (bm.hasPages()) {
				meta.addPage(bm.getPages().toArray(new String[0]));
			}
			if (bm.hasAuthor()) {
				meta.setAuthor(bm.getAuthor());
			}
			if (bm.hasTitle()) {
				meta.setTitle(bm.getTitle());
			}
			cleanedItem.setItemMeta(meta);
		}

		// Enchanted books
		if (im instanceof EnchantmentStorageMeta) {
			EnchantmentStorageMeta meta = (EnchantmentStorageMeta) Bukkit.getItemFactory().getItemMeta(Material.ENCHANTED_BOOK);
			for (Map.Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) im).getStoredEnchants().entrySet()) {
				meta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
			}
			cleanedItem.setItemMeta(meta);
		}

		// Fireworks/Firework stars
		if (im instanceof FireworkMeta && ((FireworkMeta) im).getEffectsSize() > 0) {
			FireworkMeta meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(Material.FIREWORK);
			meta.addEffects(((FireworkMeta) im).getEffects());
			cleanedItem.setItemMeta(meta);
		}

		// Leather armor color
		if (im instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(Material.LEATHER_CHESTPLATE);
			meta.setColor(((LeatherArmorMeta) im).getColor());
			cleanedItem.setItemMeta(meta);
		}

		// MapMeta is handled by data value

		// Potions
		if (im instanceof PotionMeta && ((PotionMeta) im).hasCustomEffects()) {
			PotionMeta meta = (PotionMeta) Bukkit.getItemFactory().getItemMeta(Material.POTION);
			for (PotionEffect effect : ((PotionMeta) im).getCustomEffects()) {
				meta.addCustomEffect(effect, true);
			}
			cleanedItem.setItemMeta(meta);
		}

		// Repairable would preserve anvil tags on tools, we'll avoid that

		// Skulls
		if (im instanceof SkullMeta && ((SkullMeta) im).hasOwner()) {
			SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			meta.setOwner(((SkullMeta) im).getOwner());
			cleanedItem.setItemMeta(meta);
		}

		// Normal meta
		ItemMeta meta = cleanedItem.getItemMeta();

		if (im.hasDisplayName()) {
			meta.setDisplayName(im.getDisplayName());
		}

		if (im.hasEnchants()) {
			for (Map.Entry<Enchantment, Integer> entry : im.getEnchants().entrySet()) {
				meta.addEnchant(entry.getKey(), entry.getValue(), true);
			}
		}

		if (im.hasLore()) {
			meta.setLore(im.getLore());
		}

		cleanedItem.setItemMeta(meta);
		return cleanedItem;
	}

	public static HashSet<ItemStack> getUniqueItems() {
		if (uniques == null) {
			uniques = new HashSet<>();
			for (MachineType mt : MachineType.values()) {
				uniques.add(mt.getUniqueDrop());
			}
		}
		return uniques;
	}

	public static boolean isUniqueItem(ItemStack toCheck) {
		if (Captcha.isCaptcha(toCheck) || CruxiteDowel.isDowel(toCheck)) {
			return true;
		}

		for (ItemStack is : getUniqueItems()) {
			if (is.isSimilar(toCheck)) {
				return true;
			}
		}

		return false;
	}

	public static int getAddFailures(Map<Integer, ItemStack> failures) {
		int count = 0;
		for (ItemStack is : failures.values()) {
			count += is.getAmount();
		}
		return count;
	}

	/**
	 * Reduces an ItemStack by the given quantity. If the ItemStack would have a
	 * quantity of 0, returns null.
	 * 
	 * @param is the ItemStack to reduce
	 * @param amount the amount to reduce the ItemStack by
	 * 
	 * @return the reduced ItemStack
	 */
	public static ItemStack decrement(ItemStack is, int amount) {
		if (is == null) {
			return null;
		}
		if (is.getAmount() > amount) {
			is.setAmount(is.getAmount() - amount);
		} else {
			is = null;
		}
		return is;
	}

	/**
	 * Checks if there is space in the given Inventory to add the given ItemStack.
	 * 
	 * @param is the ItemStack
	 * @param inv the Inventory to check
	 * 
	 * @return true if the ItemStack can be fully added
	 */
	public static boolean hasSpaceFor(ItemStack is, Inventory inv) {
		if (is == null) {
			return true;
		}
		ItemStack toAdd = is.clone();
		for (ItemStack invStack : inv.getContents()) {
			if (invStack == null) {
				return true;
			}
			if (!invStack.isSimilar(toAdd)) {
				continue;
			}
			toAdd.setAmount(toAdd.getAmount() - toAdd.getMaxStackSize() + invStack.getAmount());
			if (toAdd.getAmount() <= 0) {
				return true;
			}
		}
		return false;
	}
}
