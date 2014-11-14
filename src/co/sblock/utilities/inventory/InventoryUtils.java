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

import net.minecraft.util.com.google.common.io.BaseEncoding;
import org.bukkit.ChatColor;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

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
	private static HashSet<ItemStack> uniques;

	private static HashMap<String, String> getItems() {
		if (items != null) {
			return items;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(Sblock.getInstance().getResource("items.tsv")))) {
			items = new HashMap<>();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] column = line.split("\t");
				items.put(column[0] + ":" + column[1], column[2]);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load items from items.tsv!", e);
		}
		return items;
	}

	@SuppressWarnings("deprecation")
	public static String getMaterialDataName(MaterialData m) {
		if (getItems().containsKey(m.getItemTypeId() + ":" + m.getData())) {
			return items.get(m.getItemTypeId() + ":" + m.getData());
		}
		return items.get(m.getItemTypeId() + ":" + 0);
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
