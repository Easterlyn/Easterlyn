package com.easterlyn.utilities;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.captcha.CruxiteDowel;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.Machine;
import com.google.common.collect.HashMultimap;
import io.netty.buffer.Unpooled;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.MerchantRecipe;
import net.minecraft.server.v1_12_R1.MerchantRecipeList;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import net.minecraft.server.v1_12_R1.PacketPlayOutCustomPayload;
import net.minecraft.server.v1_12_R1.PacketPlayOutSetSlot;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.banner.Pattern;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A set of useful methods for inventory functions.
 *
 * @author Jikoo
 */
public class InventoryUtils {

	public static final String ITEM_UNIQUE = ChatColor.DARK_PURPLE + "Unique";
	public static final String ITEM_EXCESSIVELY_ENCHANTABLE = ChatColor.DARK_BLUE.toString() + ChatColor.DARK_BLUE + ChatColor.DARK_BLUE;

	private static HashMap<String, String> items;
	private static HashMultimap<String, String> itemsReverse;
	private static HashSet<ItemStack> uniques;
	private static final HashMap<Integer, PotionType> legacyPotionIDs;

	static {
		legacyPotionIDs = new HashMap<>();
		legacyPotionIDs.put(1, PotionType.REGEN);
		legacyPotionIDs.put(2, PotionType.SPEED);
		legacyPotionIDs.put(3, PotionType.FIRE_RESISTANCE);
		legacyPotionIDs.put(4, PotionType.POISON);
		legacyPotionIDs.put(5, PotionType.INSTANT_HEAL);
		legacyPotionIDs.put(6, PotionType.NIGHT_VISION);
		legacyPotionIDs.put(8, PotionType.WEAKNESS);
		legacyPotionIDs.put(9, PotionType.STRENGTH);
		legacyPotionIDs.put(10, PotionType.SLOWNESS);
		legacyPotionIDs.put(11, PotionType.JUMP);
		legacyPotionIDs.put(12, PotionType.INSTANT_DAMAGE);
		legacyPotionIDs.put(13, PotionType.WATER_BREATHING);
		legacyPotionIDs.put(14, PotionType.INVISIBILITY);
	}

	public static ItemStack convertLegacyPotion(ItemStack item) {
		short durability = item.getDurability();

		if (durability < 1) {
			// May be a current potion, no API-safe way to check
			return item;
		}

		ItemStack potion = new ItemStack(((durability >> 14) & 1) == 1 ? Material.SPLASH_POTION : Material.POTION);

		PotionMeta potionMeta = (PotionMeta) item.getItemMeta();

		PotionType type = legacyPotionIDs.get(durability % 16);

		if (type == null) {
			potionMeta.setBasePotionData(new PotionData(PotionType.AWKWARD));
			potion.setItemMeta(potionMeta);
			return potion;
		}

		// This is a rare case I don't mind breaking
		potionMeta.removeCustomEffect(type.getEffectType());

		PotionData data = new PotionData(type,
				type.isExtendable() && ((durability >> 6) & 1) == 1,
				type.isUpgradeable() && ((durability >> 5) & 1) == 1);

		potionMeta.setBasePotionData(data);
		potion.setItemMeta(potionMeta);

		return potion;
	}

	private static HashMap<String, String> getItems() {
		if (items != null) {
			return items;
		}
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(
				Bukkit.getPluginManager().getPlugin("Easterlyn").getResource("items.csv")))) {
			items = new HashMap<>();
			itemsReverse = HashMultimap.create();
			String line;
			while ((line = reader.readLine()) != null) {
				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}
				String[] row = line.split(",");
				String id = row[1] + ":" + row[2];
				items.put(id, row[3]);
				itemsReverse.put(row[3], id);
			}
		} catch (IOException e) {
			throw new RuntimeException("Could not load items from items.csv!", e);
		}
		return items;
	}

	public static String getItemName(ItemStack item) {
		Material material = item.getType();
		short durability = item.getDurability();
		if (material.getMaxDurability() > 0) {
			// Degradable item
			durability = 0;
		}
		String key = material.name() + ":" + durability;
		String name = null;
		if (getItems().containsKey(key)) {
			name = items.get(key);
		}
		/*
		 * This special case is mostly for banners. While their color is affected by their data
		 * value, it can actually be completely overwritten by the base color in the tile entity NBT
		 * tag. Because of that, we can't specify banner color based on data value.
		 */
		if (name == null) {
			key = material.name() + ":0";
			if (getItems().containsKey(key)) {
				name = items.get(key);
			}
		}
		if (name == null) {
			// Even special-cased materials should have an entry.
			return TextUtils.getFriendlyName(material);
		}
		if (material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION
				|| material == Material.TIPPED_ARROW) {
			if (!item.hasItemMeta()) {
				return name;
			}
			ItemMeta meta = item.getItemMeta();
			if (meta instanceof PotionMeta) {
				return TextUtils.getFriendlyName(material) + " of " + getPotionName((PotionMeta) meta);
			}
			return name;
		} else if (material == Material.MONSTER_EGG) {
			return name;
			// TODO Spigot's API does not yet support this.
//			if (!item.hasItemMeta()) {
//				return name;
//			}
//			ItemMeta meta = item.getItemMeta();
//			if (meta instanceof SpawnEggMeta) {
//				return TextUtils.getFriendlyName(((SpawnEggMeta) meta).getEntityType().name()) + name;
//			}
		}
		return name;
	}

	private static String getPotionName(PotionMeta meta) {
		PotionData base;
		try {
			base = meta.getBasePotionData();
		} catch (IllegalArgumentException e) {
			// This can be thrown by Spigot when converting a valid potion with odd data values.
			return "Questionable Validity";
		}
		if (base.getType() != PotionType.UNCRAFTABLE) {
			StringBuilder name = new StringBuilder();
			if (base.isExtended()) {
				name.append("Extended ");
			}
			name.append(TextUtils.getFriendlyName(base.getType()));
			if (base.isUpgraded()) {
				name.append(" II");
			}
			return name.toString();
		}
		if (!meta.hasCustomEffects()) {
			return "No Effect";
		}
		if (meta.getCustomEffects().size() > 1) {
			return "Multiple Effects";
		}
		PotionEffect effect = meta.getCustomEffects().get(0);
		PotionEffectType type = effect.getType();
		boolean extended = !type.isInstant() && effect.getDuration() > 3600 * type.getDurationModifier();
		StringBuilder name = new StringBuilder();
		if (extended) {
			name.append("Extended ");
		}
		name.append(TextUtils.getFriendlyName(type.getName()));
		if (effect.getAmplifier() > 0) {
			// Effect power is 0-indexed
			name.append(' ').append(NumberUtils.romanFromInt(effect.getAmplifier() + 1));
		}
		return name.toString();
	}

	public static boolean isMisleadinglyNamed(String name, Material material, short durability) {
		getItems();
		String id = material.name() + ":" + durability;
		boolean match = false;
		for (String storedId : itemsReverse.get(name)) {
			if (storedId.equals(id)) {
				return false;
			}
			match = true;
		}
		return match;
	}

	@SuppressWarnings("deprecation")
	public static Pair<Material, Short> matchMaterial(String search) {
		String[] matData = search.split(":");
		if (matData[0].length() < 2) {
			// Too short strings will always result in "air"
			throw new IllegalArgumentException("Search string must be 2 characters minimum.");
		}

		Material material = null;
		Short durability = null;

		if (matData.length > 1) {
			try {
				durability = Short.parseShort(matData[1]);
			} catch (NumberFormatException e) {}
		}

		try {
			material = Material.getMaterial(Integer.parseInt(matData[0]));
			return new ImmutablePair<>(material, durability != null ? durability : 0);
		} catch (NumberFormatException e) {}

		boolean durabilitySet = durability != null;
		if (!durabilitySet) {
			durability = 0;
		}

		int matchLevel = Integer.MAX_VALUE;
		matData[0] = matData[0].replace('_', ' ').toLowerCase();
		for (Entry<String, String> entry : getItems().entrySet()) {
			int current = StringUtils.getLevenshteinDistance(matData[0], entry.getValue().toLowerCase());
			if (current < matchLevel) {
				matchLevel = current;
				String[] entryData = entry.getKey().split(":");
				material = Material.getMaterial(entryData[0]);
				if (!durabilitySet) {
					durability = Short.valueOf(entryData[1]);
				}
			}
			if (current == 0) {
				return new ImmutablePair<>(material, durability);
			}
		}
		// Allow more fuzziness for longer named items
		if (matchLevel < (3 + material.name().length() / 5)) {
			return new ImmutablePair<>(material, durability);
		}
		return null;
	}

	public static void nerfEnchants(InventoryView view) {
		for (int index = view.countSlots() - 1; index <= 0; --index) {
			view.setItem(index, nerfEnchants(view.getItem(index)));
		}
	}

	/**
	 * Nerf enchantments over vanilla max on anything other than a furnace.
	 *
	 * @param itemStack the ItemStack to be nerfed
	 * @return the nerfed ItemStack
	 */
	public static ItemStack nerfEnchants(ItemStack itemStack) {
		if (itemStack == null || itemStack.getType() == Material.FURNACE || !itemStack.hasItemMeta()) {
			return itemStack;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();

		if (itemMeta.hasLore()) {
			for (String lore : itemMeta.getLore()) {
				if (lore.startsWith(ITEM_EXCESSIVELY_ENCHANTABLE)) {
					return itemStack;
				}
			}
		}

		if (itemMeta instanceof EnchantmentStorageMeta) {
			EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) itemMeta;
			storageMeta.getStoredEnchants().forEach((enchantment, level) ->
					storageMeta.addStoredEnchant(enchantment, Math.min(level, 5), true));
		}

		itemMeta.getEnchants().forEach((enchantment, level) ->
				itemMeta.addEnchant(enchantment, Math.min(level, 5), true));

		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	public static ItemStack cleanNBT(ItemStack is) {
		if (is == null || !is.hasItemMeta()) {
			return is;
		}

		ItemStack cleanedItem = new ItemStack(is.getType());
		// Why Bukkit doesn't have a constructor ItemStack(MaterialData) I don't know.
		cleanedItem.setData(is.getData());
		cleanedItem.setDurability(is.getDurability());
		cleanedItem.setAmount(is.getAmount());
		ItemMeta im = is.getItemMeta();

		// Banners
		if (im instanceof BannerMeta) {
			BannerMeta meta = (BannerMeta) Bukkit.getItemFactory().getItemMeta(Material.BANNER);
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
				meta.setPages(bm.getPages());
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
			PotionMeta oldMeta = (PotionMeta) im;
			meta.setBasePotionData(oldMeta.getBasePotionData());
			for (PotionEffect effect : oldMeta.getCustomEffects()) {
				// Custom effects are fine, but amplifiers that are way too high are not
				if (effect.getAmplifier() < 5 && effect.getAmplifier() >= 0) {
					meta.addCustomEffect(effect, true);
				}
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

	public static HashSet<ItemStack> getUniqueItems(Easterlyn plugin) {
		if (uniques == null) {
			uniques = new HashSet<>();
			for (Machine machine : plugin.getModule(Machines.class).getMachinesByName().values()) {
				uniques.add(machine.getUniqueDrop());
			}
		}
		return uniques;
	}

	public static boolean isUniqueItem(Easterlyn plugin, ItemStack toCheck) {
		if (Captcha.isCard(toCheck) || CruxiteDowel.isDowel(toCheck)) {
			return true;
		}

		if (toCheck.hasItemMeta()) {
			ItemMeta meta = toCheck.getItemMeta();
			if (meta.hasLore() && meta.getLore().contains(ITEM_UNIQUE)) {
				return true;
			}
		}

		for (ItemStack is : getUniqueItems(plugin)) {
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
		if (is == null || is.getType() == Material.AIR) {
			return null;
		}
		if (is.getAmount() > amount) {
			is.setAmount(is.getAmount() - amount);
		} else {
			is = null;
		}
		return is;
	}

	public static void decrementHeldItem(PlayerInteractEvent event, int amount) {
		boolean main = isMainHand(event);
		PlayerInventory inv = event.getPlayer().getInventory();
		setHeldItem(inv, main, decrement(getHeldItem(inv, main), amount));
	}

	public static boolean isMainHand(PlayerInteractEvent event) {
		return event.getHand() == EquipmentSlot.HAND;
	}

	public static ItemStack getHeldItem(PlayerInteractEvent event) {
		return getHeldItem(event.getPlayer().getInventory(), isMainHand(event));
	}

	public static ItemStack getHeldItem(PlayerInventory inv, boolean mainHand) {
		return mainHand ? inv.getItemInMainHand() : inv.getItemInOffHand();
	}

	public static void setHeldItem(PlayerInventory inv, boolean mainHand, ItemStack item) {
		if (mainHand) {
			inv.setItemInMainHand(item);
		} else {
			inv.setItemInOffHand(item);
		}
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
		if (is == null || is.getType() == Material.AIR) {
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

	public static void updateWindowSlot(Player player, int slot) {
		if (!(player instanceof CraftPlayer)) {
			return;
		}
		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		nmsPlayer.playerConnection.sendPacket(
				new PacketPlayOutSetSlot(nmsPlayer.activeContainer.windowId, slot,
						nmsPlayer.activeContainer.getSlot(slot).getItem()));
	}

	public static void changeWindowName(Player player, String name) {
		Inventory top = player.getOpenInventory().getTopInventory();
		if (top == null) {
			return;
		}

		int slots = top.getSize();

		int containerCounter;
		try {
			Method method = player.getClass().getMethod("getHandle");
			Object nmsPlayer = method.invoke(player);
			Field field = nmsPlayer.getClass().getDeclaredField("containerCounter");
			field.setAccessible(true);
			containerCounter = (int) field.get(nmsPlayer);
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException
				| IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			e.printStackTrace();
			return;
		}
		String containerName;
		try {
			Method method = top.getClass().getMethod("getInventory");
			Object iinventory = method.invoke(top);
			method = iinventory.getClass().getMethod("getContainerName");
			containerName = (String) method.invoke(iinventory);
		} catch (NoSuchMethodException e) {
			// TODO Custom containers do not have a method getContainerName
			// Should probably mimic behavior and just fetch based on InventoryType
			containerName = "minecraft:container";
		} catch (SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassCastException e) {
			e.printStackTrace();
			containerName = "minecraft:container";
		}

		ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		PacketContainer packet = manager.createPacket(PacketType.Play.Server.OPEN_WINDOW);
		packet.getIntegers().write(0, containerCounter);
		packet.getStrings().write(0, containerName);
		packet.getChatComponents().write(0,
				WrappedChatComponent.fromJson("{\"text\": \"" + name + "\"}"));
		packet.getIntegers().write(1, slots);
		try {
			manager.sendServerPacket(player, packet);
			player.updateInventory();
		} catch (InvocationTargetException ex) {
			ex.printStackTrace();
		}
	}

	public static String getNameFromAnvil(InventoryView view) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return null;
		}
		try {
			Method method = view.getClass().getMethod("getHandle");
			Object nmsInventory = method.invoke(view);
			Field field = nmsInventory.getClass().getDeclaredField("l");
			field.setAccessible(true);
			return (String) field.get(nmsInventory);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void setAnvilExpCost(InventoryView view, int cost) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		try {
			Method method = view.getClass().getMethod("getHandle");
			Object nmsInventory = method.invoke(view);
			Field field = nmsInventory.getClass().getDeclaredField("a");
			field.set(nmsInventory, cost);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void updateAnvilExpCost(InventoryView view) {
		if (!(view.getTopInventory() instanceof AnvilInventory)) {
			return;
		}
		try {
			Method method = view.getClass().getMethod("getHandle");
			Object nmsInventory = method.invoke(view);
			Field field = nmsInventory.getClass().getDeclaredField("a");
			method = view.getPlayer().getClass().getMethod("getHandle");
			Object nmsPlayer = method.invoke(view.getPlayer());
			method = nmsPlayer.getClass().getMethod("setContainerData", nmsInventory.getClass().getSuperclass(), int.class, int.class);
			method.invoke(nmsPlayer, nmsInventory, 0, field.get(nmsInventory));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SafeVarargs
	public static void updateVillagerTrades(Player player, Triple<ItemStack, ItemStack, ItemStack>... recipes) {
		if (recipes == null || recipes.length == 0) {
			// Setting result in a villager inventory with recipes doesn't play nice clientside.
			// To make life easier, if there are no recipes, don't send the trade recipe packet.
			return;
		}

		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();

		if (nmsPlayer.activeContainer.getBukkitView().getType() != InventoryType.MERCHANT) {
			return;
		}

		MerchantRecipeList list = new MerchantRecipeList();
		for (Triple<ItemStack, ItemStack, ItemStack> recipe : recipes) {
			// The client can handle having empty results for recipes, but will crash upon removing the result.
			// To combat that, add a full empty recipe instead of a recipe with an empty result.
			// We can't just remove the recipe in case the client has changed to a higher number
			// recipe - it cannot handle a reduction below its current recipe number.
			boolean hasNoResult = recipe.getRight() == null || recipe.getRight().getType() == Material.AIR;
			list.add(new MerchantRecipe(hasNoResult ? net.minecraft.server.v1_12_R1.ItemStack.a : CraftItemStack.asNMSCopy(recipe.getLeft()),
					hasNoResult ? net.minecraft.server.v1_12_R1.ItemStack.a : CraftItemStack.asNMSCopy(recipe.getMiddle()),
							CraftItemStack.asNMSCopy(recipe.getRight())));
		}

		PacketDataSerializer out = new PacketDataSerializer(Unpooled.buffer());
		try {
			Field field = nmsPlayer.getClass().getDeclaredField("containerCounter");
			field.setAccessible(true);
			out.writeInt(field.getInt(nmsPlayer));
		} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
			return;
		}
		list.a(out);
		nmsPlayer.playerConnection.sendPacket(new PacketPlayOutCustomPayload("MC|TrList", out));
	}

	public static String recipeToText(Recipe recipe) {
		if (recipe instanceof FurnaceRecipe) {
			return String.format("SMELT: %s -> %s", itemToText(((FurnaceRecipe) recipe).getInput()), itemToText(recipe.getResult()));
		} else if (recipe instanceof ShapelessRecipe) {
			StringBuilder builder = new StringBuilder("SHAPELESS: ");
			for (ItemStack ingredient : ((ShapelessRecipe) recipe).getIngredientList()) {
				builder.append(itemToText(ingredient)).append(" + ");
			}
			builder.replace(builder.length() - 2, builder.length(), "-> ").append(recipe.getResult());
			return builder.toString();
		} else if (recipe instanceof ShapedRecipe) {
			StringBuilder builder = new StringBuilder("SHAPED:\n");
			Map<Character, String> mappings = new HashMap<>();
			int longestMapping = 3; // "AIR".length() == 3
			for (Map.Entry<Character, ItemStack> mapping : ((ShapedRecipe) recipe).getIngredientMap().entrySet()) {
				String newMapping = itemToText(mapping.getValue());
				longestMapping = Math.max(longestMapping, newMapping.length());
				mappings.put(mapping.getKey(), newMapping);
			}
			for (String line : ((ShapedRecipe) recipe).getShape()) {
				for (char character : line.toCharArray()) {
					builder.append('[');
					String mapping = mappings.getOrDefault(character, "AIR");
					double padding = (longestMapping - mapping.length()) / 2.0;
					double roundPadding = Math.floor(padding);
					for (int i = 0; i < roundPadding; i++) {
						builder.append(' ');
					}
					builder.append(mapping);
					// Post-pad additional space for odd numbers
					padding = Math.floor(padding + 0.5);
					for (int i = 0; i < padding; i++) {
						builder.append(' ');
					}
					builder.append("] ");
				}
				builder.delete(builder.length() - 1, builder.length());
				builder.append('\n');
			}
			builder.delete(builder.length() - 1, builder.length()).append(" -> ").append(itemToText(recipe.getResult()));
			return builder.toString();
		}
		return recipe.toString();
	}

	public static String itemToText(ItemStack item) {
		if (item == null || item.getType() == Material.AIR) {
			return "AIR";
		}
		StringBuilder builder = new StringBuilder();
		builder.append(item.getType().name());
		if (item.getDurability() != -1 && item.getDurability() != Short.MAX_VALUE) {
			builder.append(':').append(item.getDurability());
		}
		if (item.getAmount() != 1) {
			builder.append('x').append(item.getAmount());
		}
		return builder.toString();
	}

}
