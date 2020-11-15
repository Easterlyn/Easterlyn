package com.easterlyn.util;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.inventory.ItemUtil;
import com.easterlyn.util.wrapper.RecipeWrapper;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_16_R3.enchantments.CraftEnchantment;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.jetbrains.annotations.NotNull;

public class EconomyUtil {

	public static final double OVERPRICED_RATE = 2.18265;
	@SuppressWarnings("unused")
	public static final double NORMAL_RATE = 0.72755;
	public static final double UNDERPRICED_RATE = 0.24252;

	private EconomyUtil() {}

	private static final Set<Function<ItemStack, Double>> modifiers = new HashSet<>();
	private static Map<Material, Double> mappings;

	public static void addWorthModifier(Function<ItemStack, Double> function) {
		modifiers.add(function);
	}

	public static double getWorth(@NotNull ItemStack itemStack) throws ArithmeticException {
		// TODO should this have a cache?
		if (itemStack.getAmount() < 1) {
			throw new ArithmeticException("stack size < 1");
		}
		double cost = getMappings().getOrDefault(itemStack.getType(), Double.POSITIVE_INFINITY);
		if (cost == Double.POSITIVE_INFINITY) {
			// Item worth could not be calculated
			throw new ArithmeticException("material is invaluable");
		}

		ItemMeta itemMeta = itemStack.hasItemMeta() ? itemStack.getItemMeta() : null;

		if (itemMeta == null) {
			// No additional costs from meta, finish fast.
			return NumberUtil.multiplySafe(cost, itemStack.getAmount());
		}

		if (ItemUtil.isUniqueItem(itemStack)) {
			throw new ArithmeticException("item is unique");
		}

		if (itemStack.getEnchantments().size() > 0 || itemMeta.hasDisplayName() || itemMeta.hasLore() || itemMeta.isUnbreakable()) {
			switch (itemStack.getType()) {
				case DRAGON_BREATH:
				case EXPERIENCE_BOTTLE:
				case FIREWORK_STAR:
				case HONEY_BOTTLE:
				case PAPER:
					// Special case: items used for unique cards, slips, or objects.
					throw new ArithmeticException("item is unique");
				default:
					break;
			}
		}

		// In case of shulker boxes, etc. do not (yet) allow duplicating unless empty.
		if (itemMeta instanceof BlockStateMeta) {
			BlockState state = ((BlockStateMeta) itemMeta).getBlockState();
			if (state instanceof InventoryHolder) {
				for (ItemStack item : ((InventoryHolder) state).getInventory().getContents()) {
					//noinspection ConstantConditions - Array is not null, but individual elements may be.
					if (item != null && item.getType() != Material.AIR) {
						throw new ArithmeticException("item has additional items stored inside");
					}
				}
			}
		}

		if (itemMeta instanceof MapMeta) {
			MapMeta mapMeta = (MapMeta) itemMeta;
			if (mapMeta.hasLocalizedName()) {
				// Map is an exploration map.
				switch (mapMeta.getLocalizedName()) {
					case "filled_map.monument":
						// Monument map.
						cost = NumberUtil.addSafe(cost, 1200);
						break;
					case "filled_map.mansion":
						// Mansions are rarer than monuments, roughly 4/3 worth in vanilla.
						cost = NumberUtil.addSafe(cost, 1600);
						break;
					default:
						// Just in case.
						cost = NumberUtil.addSafe(cost, 2000);
						break;
				}
			}
		}

		if (itemMeta instanceof FireworkMeta) {
			FireworkMeta fireworkMeta = (FireworkMeta) itemMeta;
			double gunpowder = getMappings().get(Material.GUNPOWDER);
			cost = NumberUtil.addSafe(cost, Math.abs(fireworkMeta.getPower()) * gunpowder);
			for (FireworkEffect effect : fireworkMeta.getEffects()) {
				switch (effect.getType()) {
					case BALL_LARGE:
						cost = NumberUtil.addSafe(cost, getMappings().get(Material.FIRE_CHARGE));
						break;
					case STAR:
						cost = NumberUtil.addSafe(cost, getMappings().get(Material.GOLD_NUGGET));
						break;
					case BURST:
						cost = NumberUtil.addSafe(cost, getMappings().get(Material.FEATHER));
						break;
					case CREEPER:
						cost = NumberUtil.addSafe(cost, getMappings().get(Material.WITHER_SKELETON_SKULL));
						break;
					case BALL:
						// Default effect, no cost
					default:
						break;
				}
				if (effect.hasFlicker()) {
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.GLOWSTONE_DUST));
				}
				if (effect.hasTrail()) {
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.DIAMOND));
				}
				// Flat cost of 1 per color
				cost = NumberUtil.addSafe(cost, effect.getColors().size());
				cost = NumberUtil.addSafe(cost, effect.getFadeColors().size());
			}

			if (itemStack.getType() == Material.FIREWORK_ROCKET) {
				// Firework stars each require 1 gunpowder in addition to other components
				cost = NumberUtil.addSafe(cost, gunpowder * fireworkMeta.getEffects().size());
				// 3 fireworks per craft
				cost /= 3;
			}
		}

		if (itemMeta instanceof PotionMeta) {
			PotionMeta potionMeta = (PotionMeta) itemMeta;

			if (potionMeta.hasCustomEffects()) {
				// Custom potions are unsupported.
				throw new ArithmeticException("item has custom effects");
			}

			PotionData potionData = potionMeta.getBasePotionData();

			switch (potionData.getType()) {
				case WATER:
					break;
				case MUNDANE:
					// Sugar is the cheapest ingredient that creates mundane
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.SUGAR));
					break;
				case THICK:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.GLOWSTONE_DUST));
					break;
				case AWKWARD:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART));
					break;
				case INVISIBILITY:
					// Corrupted night vision
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.FERMENTED_SPIDER_EYE));
				case NIGHT_VISION:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.GOLDEN_CARROT));
					break;
				case JUMP:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.RABBIT_FOOT));
					break;
				case FIRE_RESISTANCE:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.MAGMA_CREAM));
					break;
				case SLOWNESS:
					// Corrupted speed/leaping, speed is cheaper
					//noinspection DuplicateBranchesInSwitch // Branches are not duplicates
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.FERMENTED_SPIDER_EYE));
				case SPEED:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.SUGAR));
					break;
				case WATER_BREATHING:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.PUFFERFISH));
					break;
				case INSTANT_HEAL:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.GLISTERING_MELON_SLICE));
					break;
				case INSTANT_DAMAGE:
					// Corrupted poison/instant health, poison is cheaper
					//noinspection DuplicateBranchesInSwitch // Branches are not duplicates
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.FERMENTED_SPIDER_EYE));
				case POISON:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.SPIDER_EYE));
					break;
				case REGEN:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.GHAST_TEAR));
					break;
				case STRENGTH:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.BLAZE_POWDER));
					break;
				case WEAKNESS:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.FERMENTED_SPIDER_EYE));
					break;
				case TURTLE_MASTER:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.TURTLE_HELMET));
					break;
				case SLOW_FALLING:
					cost = NumberUtil.addSafe(cost, getMappings().get(Material.NETHER_WART) + getMappings().get(Material.PHANTOM_MEMBRANE));
					break;
				case LUCK:
				case UNCRAFTABLE:
				default:
					throw new ArithmeticException("unsupported potion " + potionData.getType().name());
			}

			if (potionData.isExtended()) {
				cost = NumberUtil.addSafe(cost, getMappings().get(Material.REDSTONE));
			}

			if (potionData.isUpgraded()) {
				cost = NumberUtil.addSafe(cost, getMappings().get(Material.GLOWSTONE_DUST));
			}
		}

		if (itemMeta.hasEnchants()) {
			for (Map.Entry<Enchantment, Integer> entry : itemMeta.getEnchants().entrySet()) {
				double enchantCost = getEnchantCost(entry.getKey(), entry.getValue(), false);
				if (!(itemMeta instanceof Damageable)) {
					enchantCost = NumberUtil.multiplySafe(enchantCost, 4);
				}
				cost = NumberUtil.addSafe(cost, enchantCost);
			}
		}

		if (itemMeta instanceof EnchantmentStorageMeta) {
			for (Map.Entry<Enchantment, Integer> entry : ((EnchantmentStorageMeta) itemMeta).getStoredEnchants().entrySet()) {
				cost = NumberUtil.addSafe(cost, getEnchantCost(entry.getKey(), entry.getValue(), true));
			}
		}

		if (itemMeta.hasDisplayName()) {
			// Naming an unenchanted item in an anvil costs 1 additional level in 1.8. Since we're nice, a fixed cost.
			cost = NumberUtil.addSafe(cost, 15);
		}

		for (Function<ItemStack, Double> function : modifiers) {
			cost = NumberUtil.addSafe(cost, function.apply(itemStack));
		}

		cost = NumberUtil.multiplySafe(cost, itemStack.getAmount());

		if (cost <= 0) {
			ReportableEvent.call("Found ItemStack with worth < 0: " + ItemUtil.getAsText(itemStack));
			throw new ArithmeticException("item worth evaluated < 0");
		}
		return cost;
	}

	/**
	 * Gets a value for an enchantment.
	 * <p>
	 *     This is based on internal rarity values and may need updating between versions.
	 * </p>
	 * @param enchantment the Enchantment
	 * @param level the level of the Enchantment
	 * @param stored whether or not the Enchantment is in a book and not useable
	 * @return the cost of the enchantment
	 */
	private static double getEnchantCost(Enchantment enchantment, double level, boolean stored) {
		double enchantCost = net.minecraft.server.v1_16_R3.Enchantment.Rarity.COMMON.a() + 10
				- (enchantment instanceof CraftEnchantment ? ((CraftEnchantment) enchantment).getHandle().d().a()
				: net.minecraft.server.v1_16_R3.Enchantment.Rarity.UNCOMMON.a());
		enchantCost *= (stored ? 60 : 65);
		// Balance: Base cost on percentage of max level, not only current level
		enchantCost *= Math.pow(2D, Math.abs(level)) / Math.pow(2D, enchantment.getMaxLevel());
		if (enchantment.getKey().getKey().contains("curse")) {
			// Curses are also treasure, should be handled first.
			enchantCost /= 2.5;
		} else if (enchantment.isTreasure()) {
			// Rarer, increase cost.
			enchantCost *= 1.5;
		}
		return enchantCost;
	}

	public static Map<Material, Double> getMappings() {
		if (mappings != null) {
			return mappings;
		}

		mappings = createBaseWorth();

		for (String variant : new String[] { "BRAIN", "BUBBLE", "FIRE", "HORN", "TUBE" }) {
			mappings.put(Material.matchMaterial(String.format("%s_CORAL_FAN", variant)), 4D);
			mappings.put(Material.matchMaterial(String.format("%s_CORAL", variant)), 16D);
			mappings.put(Material.matchMaterial(String.format("%s_CORAL_BLOCK", variant)), 64D);
			mappings.put(Material.matchMaterial(String.format("DEAD_%s_CORAL_FAN", variant)), 2D);
			mappings.put(Material.matchMaterial(String.format("DEAD_%s_CORAL", variant)), 8D);
			mappings.put(Material.matchMaterial(String.format("DEAD_%s_CORAL_BLOCK", variant)), 32D);
		}

		// Fill from recipes
		for (Material material : Material.values()) {
			addRecipeCosts(material, new HashSet<>());
		}

		// Special cases
		mappings.put(Material.CHIPPED_ANVIL, mappings.get(Material.ANVIL) / 3 * 2);
		mappings.put(Material.DAMAGED_ANVIL, mappings.get(Material.ANVIL) / 3);
		mappings.put(Material.FILLED_MAP, mappings.get(Material.MAP));
		mappings.put(Material.FIREWORK_ROCKET, mappings.get(Material.PAPER));
		mappings.put(Material.FIREWORK_STAR, mappings.get(Material.GUNPOWDER));
		mappings.put(Material.POTION, mappings.get(Material.GLASS_BOTTLE) + 1);
		mappings.put(Material.SPLASH_POTION, mappings.get(Material.POTION) + mappings.get(Material.GUNPOWDER));
		mappings.put(Material.LINGERING_POTION, mappings.get(Material.SPLASH_POTION) + mappings.get(Material.DRAGON_BREATH));
		mappings.put(Material.TIPPED_ARROW, mappings.get(Material.LINGERING_POTION) / 8 + mappings.get(Material.ARROW));
		mappings.put(Material.COD_BUCKET, mappings.get(Material.COD) + mappings.get(Material.BUCKET));
		mappings.put(Material.PUFFERFISH_BUCKET, mappings.get(Material.PUFFERFISH) + mappings.get(Material.BUCKET));
		mappings.put(Material.SALMON_BUCKET, mappings.get(Material.SALMON) + mappings.get(Material.BUCKET));
		mappings.put(Material.TROPICAL_FISH_BUCKET, mappings.get(Material.TROPICAL_FISH) + mappings.get(Material.BUCKET));
		// TODO why are these not detecting properly
		mappings.put(Material.BONE_BLOCK, mappings.get(Material.BONE_MEAL) * 9);
		mappings.put(Material.DRIED_KELP_BLOCK, mappings.get(Material.DRIED_KELP_BLOCK) * 9);

		for (DyeColor color : DyeColor.values()) {
			mappings.put(Material.matchMaterial(color.name() + "_SHULKER_BOX"), mappings.get(Material.SHULKER_BOX));
			mappings.put(Material.matchMaterial(color.name() + "_CONCRETE"),
					mappings.get(Material.matchMaterial(color.name() + "_CONCRETE_POWDER")) + 3);
		}

		mappings.put(Material.DEBUG_STICK,
				mappings.get(Material.NETHER_STAR) + 2 * mappings.get(Material.END_ROD));

		mappings.remove(Material.EMERALD);
		mappings.remove(Material.EMERALD_BLOCK);
		mappings.remove(Material.EMERALD_ORE);
		mappings.remove(Material.LAPIS_LAZULI);
		mappings.remove(Material.LAPIS_BLOCK);
		mappings.remove(Material.LAPIS_ORE);

		mappings.entrySet().removeIf(entry -> entry.getValue() == Double.POSITIVE_INFINITY);

		return mappings;
	}

	private static Map<Material, Double> createBaseWorth() {
		Map<Material, Double> values = new HashMap<>();

		setWorth(values, 1D, Tag.LEAVES);
		setWorth(values, 5D, Tag.WOOL);
		// Tag.LOGS actually only contains stems and Tag.LOGS_THAT_BURN
		setWorth(values, 8D, Tag.ACACIA_LOGS, Tag.BIRCH_LOGS, Tag.DARK_OAK_LOGS, Tag.JUNGLE_LOGS, Tag.OAK_LOGS,
				Tag.SPRUCE_LOGS, Tag.CRIMSON_STEMS, Tag.WARPED_STEMS);
		setWorth(values, 70D, Tag.ITEMS_CREEPER_DROP_MUSIC_DISCS);

		for (Material material : Material.values()) {
			switch (material) {
				case BEETROOT_SEEDS:
				case CLAY_BALL:
				case DEAD_BUSH:
				case DIRT:
				case FERN:
				case GRAVEL:
				case GRASS:
				case MELON_SLICE:
				case POISONOUS_POTATO:
				case SAND:
				case WHEAT_SEEDS:
				case SNOWBALL:
					values.put(material, 1D);
					break;
				case ALLIUM:
				case AZURE_BLUET:
				case BAMBOO:
				case BEETROOT:
				case CACTUS:
				case CARROT:
				case CHORUS_FRUIT:
				case COBBLESTONE:
				case CORNFLOWER:
				case DANDELION:
				case KELP:
				case LILY_PAD:
				case MUSHROOM_STEM:
				case NETHER_BRICK:
				case ORANGE_TULIP:
				case PAPER:
				case PINK_TULIP:
				case POPPY:
				case RABBIT_HIDE:
				case RED_MUSHROOM:
				case RED_MUSHROOM_BLOCK:
				case RED_SAND:
				case RED_TULIP:
				case SEAGRASS:
				case SOUL_SAND:
				case SOUL_SOIL:
				case SUGAR_CANE:
				case SWEET_BERRIES:
				case VINE:
				case WHEAT:
				case WHITE_TULIP:
					values.put(material, 2D);
					break;
				case ANDESITE:
				case BROWN_MUSHROOM:
				case BROWN_MUSHROOM_BLOCK:
				case BLUE_ORCHID:
				case COCOA_BEANS:
				case CYAN_DYE:
				case DIORITE:
				case GRANITE:
				case LAPIS_LAZULI:
				case LILAC:
				case LILY_OF_THE_VALLEY:
				case NETHERRACK:
				case OXEYE_DAISY:
				case PEONY:
				case POTATO:
				case PURPLE_DYE:
				case ROSE_BUSH:
				case ROTTEN_FLESH:
				case STONE:
					values.put(material, 3D);
					break;
				case ARROW:
				case CHICKEN:
				case FEATHER:
				case LARGE_FERN:
				case TALL_GRASS:
				case SUNFLOWER:
					values.put(material, 4D);
					break;
				case CLAY:
				case FLINT:
				case RABBIT:
				case COD:
				case SALMON:
					values.put(material, 5D);
					break;
				case BAKED_POTATO:
				case CARVED_PUMPKIN:
				case EGG:
				case NETHER_BRICKS:
				case PUMPKIN:
					values.put(material, 6D);
					break;
				case COOKED_CHICKEN:
				case MUTTON:
				case BEEF:
				case REDSTONE:
				case SEA_PICKLE:
				case STRING:
					values.put(material, 8D);
					break;
				case COOKED_COD:
				case COOKED_SALMON:
				case NETHER_WART:
				case PRISMARINE_SHARD:
					values.put(material, 9D);
					break;
				case END_STONE:
				case GLOWSTONE_DUST:
				case ICE:
				case LEATHER:
				case MELON:
				case MOSSY_COBBLESTONE:
				case PORKCHOP:
				case TROPICAL_FISH:
				case PUFFERFISH:
				case SLIME_BALL:
				case BLACK_STAINED_GLASS:
				case BLUE_STAINED_GLASS:
				case BROWN_STAINED_GLASS:
				case CYAN_STAINED_GLASS:
				case GRAY_STAINED_GLASS:
				case GREEN_STAINED_GLASS:
				case LIGHT_BLUE_STAINED_GLASS:
				case LIGHT_GRAY_STAINED_GLASS:
				case LIME_STAINED_GLASS:
				case MAGENTA_STAINED_GLASS:
				case ORANGE_STAINED_GLASS:
				case PINK_STAINED_GLASS:
				case PURPLE_STAINED_GLASS:
				case RED_STAINED_GLASS:
				case WHITE_STAINED_GLASS:
				case YELLOW_STAINED_GLASS:
					values.put(material, 10D);
					break;
				case APPLE:
				case BONE:
				case COAL:
				case COOKED_BEEF:
				case GOLD_NUGGET:
				case RABBIT_FOOT:
				case SPIDER_EYE:
				case INK_SAC:
				case EXPERIENCE_BOTTLE: // 11 exp to fill a bottle, bottle worth roughly 1
					values.put(material, 12D);
					break;
				case BLACK_TERRACOTTA:
				case BLUE_TERRACOTTA:
				case BROWN_TERRACOTTA:
				case CYAN_TERRACOTTA:
				case GRAY_TERRACOTTA:
				case GREEN_TERRACOTTA:
				case LIGHT_BLUE_TERRACOTTA:
				case LIGHT_GRAY_TERRACOTTA:
				case LIME_TERRACOTTA:
				case MAGENTA_TERRACOTTA:
				case ORANGE_TERRACOTTA:
				case PINK_TERRACOTTA:
				case PURPLE_TERRACOTTA:
				case RED_TERRACOTTA:
				case WHITE_TERRACOTTA:
				case YELLOW_TERRACOTTA:
				case TERRACOTTA:
					values.put(material, 13D);
					break;
				case COOKED_PORKCHOP:
					values.put(material, 14D);
					break;
				case ACACIA_SAPLING:
				case BIRCH_SAPLING:
				case CHORUS_FLOWER:
				case DARK_OAK_SAPLING:
				case JUNGLE_SAPLING:
				case OAK_SAPLING:
				case SADDLE:
				case SPRUCE_SAPLING:
					values.put(material, 16D);
					break;
				case GUNPOWDER:
				case MAP: // Not crafted, right click
				case MYCELIUM:
				case PODZOL:
					values.put(material, 20D);
					break;
				case ENCHANTED_BOOK:
					values.put(material, 25D);
					break;
				case PACKED_ICE:
					values.put(material, 28D);
					break;
				case BLAZE_ROD:
				case GRASS_BLOCK:
					values.put(material, 30D);
					break;
				case BLACK_BANNER:
				case BLUE_BANNER:
				case BROWN_BANNER:
				case CYAN_BANNER:
				case GRAY_BANNER:
				case GREEN_BANNER:
				case LIGHT_BLUE_BANNER:
				case LIGHT_GRAY_BANNER:
				case LIME_BANNER:
				case MAGENTA_BANNER:
				case ORANGE_BANNER:
				case PINK_BANNER:
				case PURPLE_BANNER:
				case RED_BANNER:
				case WHITE_BANNER:
				case YELLOW_BANNER:
				case COBWEB:
				case GHAST_TEAR:
				case PHANTOM_MEMBRANE:
				case PRISMARINE_CRYSTALS:
					values.put(material, 35D);
					break;
				case QUARTZ:
				case DRAGON_BREATH:
					values.put(material, 37D);
					break;
				case IRON_INGOT:
					values.put(material, 41D);
					break;
				case COAL_ORE:
				case NETHER_QUARTZ_ORE:
					values.put(material, 44D);
					break;
				case IRON_ORE:
					// Special case disks - also in dungeon loot
				case MUSIC_DISC_13:
				case MUSIC_DISC_CAT:
				case NAUTILUS_SHELL:
					values.put(material, 50D);
					break;
				case OBSIDIAN:
				case REDSTONE_ORE:
					values.put(material, 81D);
					break;
				case ENDER_PEARL:
				case WITHER_ROSE:
					values.put(material, 90D);
					break;
				case GOLD_INGOT:
					values.put(material, 108D);
					break;
				case GOLD_ORE:
				case NETHER_GOLD_ORE:
				case LAVA_BUCKET:
				case MILK_BUCKET:
				case WATER_BUCKET:
					values.put(material, 138D);
					break;
				case DIAMOND:
					values.put(material, 167D);
					break;
				case DIAMOND_ORE:
					values.put(material, 187D);
					break;
				case IRON_HORSE_ARMOR:
					values.put(material, 261D);
					break;
				case GLOBE_BANNER_PATTERN:
					values.put(material, 300D);
					break;
				case NAME_TAG:
					values.put(material, 405D);
					break;
				case CHAINMAIL_BOOTS:
					values.put(material, 600D);
					break;
				case GOLDEN_HORSE_ARMOR:
					values.put(material, 663D);
					break;
				case CHAINMAIL_HELMET:
				case SCUTE:
				case SHULKER_SHELL:
					values.put(material, 750D);
					break;
				case DIAMOND_HORSE_ARMOR:
				case TOTEM_OF_UNDYING:
				case WET_SPONGE:
					values.put(material, 1000D);
					break;
				case CHAINMAIL_LEGGINGS:
					values.put(material, 1050D);
					break;
				case CHAINMAIL_CHESTPLATE:
					values.put(material, 1200D);
					break;
				case BELL:
					values.put(material, 1336D);
					break;
				case DRAGON_HEAD:
				case WITHER_SKELETON_SKULL:
					values.put(material, 3000D);
					break;
				case ELYTRA:
				case TRIDENT:
					values.put(material, 3142D);
					break;
				case HEART_OF_THE_SEA:
					values.put(material, 5000D);
					break;
				case NETHER_STAR:
					values.put(material, 10000D);
					break;
				case PLAYER_HEAD:
				case CREEPER_HEAD:
				case ZOMBIE_HEAD:
				case SKELETON_SKULL:
					values.put(material, 16000D);
					break;
				case DRAGON_EGG:
					values.put(material, 32000D);
					break;
				// Unobtainable, don't bother searching recipes
				case AIR:
				case BARRIER:
				case BEDROCK:
				case COMMAND_BLOCK:
				case COMMAND_BLOCK_MINECART:
				case REPEATING_COMMAND_BLOCK:
				case CHAIN_COMMAND_BLOCK:
				case END_PORTAL:
				case END_PORTAL_FRAME:
				case SPAWNER:
					// Money
				case EMERALD:
				case EMERALD_BLOCK:
				case EMERALD_ORE:
				case LAPIS_BLOCK:
				case LAPIS_ORE:
					// Added later
				case POTION:
				case TIPPED_ARROW:
				case SPLASH_POTION:
				case LINGERING_POTION:
					// Duplicate via other means, not alchemy
				case WRITTEN_BOOK:
				case WRITABLE_BOOK:
					// TODO debug why this is registering as 432 (4x gold_ingot), some bad math somewhere
				case NETHERITE_INGOT:
					values.put(material, Double.POSITIVE_INFINITY);
				default:
					break;
			}
		}
		return values;
	}

	@SafeVarargs
	private static void setWorth(@NotNull Map<Material, Double> values, double value, @NotNull Tag<Material>... tags) {
		for (Tag<Material> tag : tags) {
			tag.getValues().forEach(material -> values.put(material, value));
		}
	}

	private static double addRecipeCosts(Material material, Set<Material> pastMaterials) {
		// Check if calculated already
		if (mappings.containsKey(material)) {
			return mappings.get(material);
		}

		// Check if mid-calculation
		if (pastMaterials.contains(material)) {
			return Double.POSITIVE_INFINITY;
		}

		// Create a new list for sub-elements
		pastMaterials = new HashSet<>(pastMaterials);
		// Add to mid-calc list
		pastMaterials.add(material);

		double minimum = Double.POSITIVE_INFINITY;

		nextRecipe: for (Recipe bukkitRecipe : Bukkit.getRecipesFor(new ItemStack(material))) {
			ItemStack result = bukkitRecipe.getResult();
			int amount = result.getAmount();
			if (amount < 1) {
				continue;
			}

			RecipeWrapper recipe = new RecipeWrapper(bukkitRecipe);

			if (recipe.getResult().getType() != material) {
				ReportableEvent.call("improper wrap of recipe: " + ((Keyed) bukkitRecipe).getKey().toString() + " type: " + bukkitRecipe.getClass().getSimpleName());
				continue;
			}

			if (recipe.getRecipeIngredients().isEmpty()) {
				continue;
			}

			double newMinimum = 0;

			for (Map.Entry<EnumSet<Material>, Integer> ingredient : recipe.getRecipeIngredients().entrySet()) {
				if (ingredient.getValue() == null || ingredient.getValue() < 1) {
					continue nextRecipe;
				}

				double bestMaterialPrice = Double.POSITIVE_INFINITY;
				for (Material potential : ingredient.getKey()) {
					if (pastMaterials.contains(potential)) {
						continue;
					}

					double potentialValue = addRecipeCosts(potential, pastMaterials);

					if (potential.name().endsWith("_BUCKET")) {
						// Buckets are not consumed in crafting.
						// Iron ingots are 41, hardcoded bucket value here to avoid a lot of extra code to prevent issues
						potentialValue -= 123 * ingredient.getValue();
					}

					bestMaterialPrice = Math.min(bestMaterialPrice, potentialValue);
				}

				try {
					bestMaterialPrice = NumberUtil.multiplySafe(bestMaterialPrice, ingredient.getValue());
					newMinimum = NumberUtil.addSafe(newMinimum, bestMaterialPrice);
				} catch (ArithmeticException ignored) {}
			}

			if (newMinimum <= 0 || newMinimum == Double.POSITIVE_INFINITY) {
				continue;
			}

			// Hardcoded value per smelt. Coal burns for 1600 ticks, which for furnaces yields 8 smelts.
			// 1.5 cost per smelt * 200 ticks per smelt = 300 / cook time cost per smelt.
			// Campfires do not consume fuel.
			if (bukkitRecipe instanceof CookingRecipe && !(bukkitRecipe instanceof CampfireRecipe)) {
				try {
					newMinimum = NumberUtil.addSafe(newMinimum, 300D / ((CookingRecipe<?>) bukkitRecipe).getCookingTime());
				} catch (ArithmeticException e) {
					continue;
				}
			}

			if (newMinimum == Double.POSITIVE_INFINITY) {
				continue;
			}

			newMinimum /= amount;
			if (newMinimum < minimum) {
				minimum = newMinimum;
			}
		}

		// No value = no make.
		if (minimum <= 0) {
			minimum = Double.POSITIVE_INFINITY;
		}

		// Map and return.
		mappings.put(material, minimum);
		return minimum;
	}

}
