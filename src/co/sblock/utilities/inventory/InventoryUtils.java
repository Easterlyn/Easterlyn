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
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
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
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

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
			return "Potion";
		}
		return items.get(m.getId() + ":" + 0);
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
		return match;
	}

	public static String serializeItemStack(ItemStack is) {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
				BukkitObjectOutputStream bukkitOutputStream = new BukkitObjectOutputStream(outputStream)) {
			bukkitOutputStream.writeObject(is);
			bukkitOutputStream.close();
			String encoded = outputStream.toString();
			outputStream.close();
			return encoded;
		} catch (IOException e) {
			throw new RuntimeException("Unable to serialize ItemStack!", e);
		}
	}

	public static String serializeBase64ItemStack(ItemStack is) {
		return Base64Coder.encodeString(serializeItemStack(is));
	}

	public static String serializeIntoFormattingCodes(ItemStack is) {
		String hex;
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
			bukkitInputStream.close();
			inputStream.close();
			return decoded;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException("Unable to deserialize ItemStack!", e);
		}
	}

	public static ItemStack deserializeBase64ItemStack(String s) {
		return deserializeItemStack(Base64Coder.decodeString(s));
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

		// Leather armor color
		if (im instanceof LeatherArmorMeta) {
			LeatherArmorMeta meta = (LeatherArmorMeta) Bukkit.getItemFactory().getItemMeta(Material.LEATHER_CHESTPLATE);
			meta.setColor(((LeatherArmorMeta) im).getColor());
			cleanedItem.setItemMeta(meta);
		}

		// Fireworks/Firework stars
		if (im instanceof FireworkMeta && ((FireworkMeta) im).getEffectsSize() > 0) {
			FireworkMeta meta = (FireworkMeta) Bukkit.getItemFactory().getItemMeta(Material.FIREWORK);
			meta.addEffects(((FireworkMeta) im).getEffects());
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

		// Skulls
		if (im instanceof SkullMeta && ((SkullMeta) im).hasOwner()) {
			SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			meta.setOwner(((SkullMeta) im).getOwner());
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
}
