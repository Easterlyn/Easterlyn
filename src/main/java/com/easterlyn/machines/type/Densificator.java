package com.easterlyn.machines.type;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Language;
import com.easterlyn.machines.Machines;
import com.easterlyn.machines.type.computer.BadButton;
import com.easterlyn.machines.type.computer.BlockInventoryWrapper;
import com.easterlyn.machines.type.computer.GoodButton;
import com.easterlyn.machines.type.computer.Programs;
import com.easterlyn.machines.utilities.Direction;
import com.easterlyn.machines.utilities.Shape;
import com.easterlyn.micromodules.Protections;
import com.easterlyn.micromodules.protectionhooks.ProtectionHook;
import com.easterlyn.utilities.InventoryUtils;
import com.easterlyn.utilities.recipe.RecipeWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

/**
 * Condense items automatically!
 *
 * @author Jikoo
 */
public class Densificator extends Machine {

	private static final LoadingCache<Material, List<RecipeWrapper>> recipeCache;

	static {
		recipeCache = CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(
				new CacheLoader<Material, List<RecipeWrapper>>() {
					@Override
					public List<RecipeWrapper> load(@NotNull Material material) {
						ArrayList<RecipeWrapper> list = new ArrayList<>();

						Bukkit.recipeIterator().forEachRemaining(recipe -> {
							if (!(recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)) {
								return;
							}

							if (!recipe.getResult().getType().isOccluding()) {
								return;
							}

							RecipeWrapper wrapper = new RecipeWrapper(recipe);

							Map<EnumSet<Material>, Integer> ingredients = wrapper.getRecipeIngredients();
							if (ingredients.size() != 1) {
								return;
							}

							Map.Entry<EnumSet<Material>, Integer> ingredient = ingredients.entrySet().iterator().next();

							if (!ingredient.getKey().contains(material)) {
								return;
							}

							int quantity = ingredient.getValue();

							if (quantity != 4 && quantity != 9) {
								return;
							}

							list.add(wrapper);
						});

						return list;
					}
				}
		);
	}

	private final Protections protections;
	private final ItemStack drop;

	public Densificator(Easterlyn plugin, Machines machines) {
		super(plugin, machines, new Shape(), "Densificator");
		this.protections = plugin.getModule(Protections.class);

		getShape().setVectorData(new Vector(0, 0, 0),
				new Shape.MaterialDataValue(Material.DROPPER).withBlockData(Directional.class, Direction.SOUTH));
		getShape().setVectorData(new Vector(0, 1, 0),
				new Shape.MaterialDataValue(Material.PISTON).withBlockData(Directional.class, Direction.DOWN));

		this.drop = new ItemStack(Material.PISTON);
		InventoryUtils.consumeAs(ItemMeta.class, drop.getItemMeta(), itemMeta -> {
			itemMeta.setDisplayName(ChatColor.WHITE + "Densificator");
			this.drop.setItemMeta(itemMeta);
		});
	}

	private int getDensificationMode(ConfigurationSection storage) {
		return storage.getInt("densification", 49);
	}

	public int adjustDensificationMode(ConfigurationSection storage, int difference) {
		int densification = getDensificationMode(storage) + difference;
		if (densification < 49 && densification > 9  && difference > 0 || densification < 4 && difference < 0) {
			densification = 49;
		} else if (densification < 9  && difference < 0 || densification >  49 && difference > 0) {
			densification = 4;
		} else {
			densification = 9;
		}
		storage.set("densification", densification);
		return densification;
	}

	@Override
	public boolean handleInteract(PlayerInteractEvent event, ConfigurationSection storage) {
		Player player = event.getPlayer();
		// Allow sneaking players to cross or place blocks, but don't allow elevators to trigger redstone devices.
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK || player.isSneaking() || event.getClickedBlock() == null) {
			return false;
		}
		Location interacted = event.getClickedBlock().getLocation();
		Location key = getKey(storage);
		if (key.equals(interacted)) {
			return false;
		}
		for (ProtectionHook hook : protections.getHooks()) {
			if (!hook.canOpenChestsAt(player, interacted)) {
				player.sendMessage(Language.getColor("bad") + "You do not have permission to adjust densificators here!");
				return true;
			}
		}
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Inventory inventory = ((Computer) getMachines().getMachineByName("Computer")).getInventory();
			inventory = new BlockInventoryWrapper(inventory, key);
			inventory.setItem(5, ((GoodButton) Programs.getProgramByName("GoodButton"))
					.getIconFor(ChatColor.GREEN + "Cycle Densification"));
			ItemStack gauge = new ItemStack(Material.CRAFTING_TABLE);
			ItemMeta meta = gauge.getItemMeta();
			meta.setDisplayName(ChatColor.GOLD + "Crafting Mode");
			meta.setLore(Arrays.asList(ChatColor.WHITE + "2x2 (4), 3x3 (9), or",
					ChatColor.WHITE + "3x3 AND 2x2 (49)"));
			gauge.setItemMeta(meta);
			gauge.setAmount(getDensificationMode(storage));
			inventory.setItem(4, gauge);
			inventory.setItem(3, ((BadButton) Programs.getProgramByName("BadButton"))
					.getIconFor(ChatColor.RED + "Cycle Densification"));
			event.getPlayer().openInventory(inventory);
			InventoryUtils.changeWindowName(event.getPlayer(), "Densificator Configuration");
		}
		return true;
	}

	@Override
	public boolean handleOpen(InventoryOpenEvent event, ConfigurationSection storage) {
		if (event.getInventory().getType() != InventoryType.DROPPER) {
			return false;
		}

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!(event.getPlayer() instanceof Player)) {
					return;
				}
				Player player = (Player) event.getPlayer();
				if (!player.isOnline()) {
					return;
				}
				InventoryUtils.changeWindowName(player, getName());
			}
		}.runTask(getPlugin());
		return false;
	}

	@Override
	public boolean handleClick(InventoryClickEvent event, ConfigurationSection storage) {
		this.updateLater(event.getView().getTopInventory(), storage);
		return false;
	}

	@Override
	public boolean handleClick(InventoryDragEvent event, ConfigurationSection storage) {
		this.updateLater(event.getView().getTopInventory(), storage);
		return false;
	}

	@Override
	public boolean handleHopperMoveItem(InventoryMoveItemEvent event, ConfigurationSection storage) {
		this.updateLater(event.getDestination(), storage);
		return false;
	}

	private void updateLater(final Inventory inventory, ConfigurationSection storage) {
		new BukkitRunnable() {
			@Override
			public void run() {
				update(inventory, storage);
			}
		}.runTask(getPlugin());
	}

	private void update(Inventory inventory, ConfigurationSection storage) {
		// TODO
		if (inventory.getLocation() == null) {
			return;
		}
		Map<Material, Integer> contents = new HashMap<>();
		int densificationMode = this.getDensificationMode(storage);
		for (ItemStack stack : inventory.getContents()) {
			if (stack == null || stack.getType() == Material.AIR) {
				continue;
			}

			List<RecipeWrapper> recipes = recipeCache.getUnchecked(stack.getType());

			if (recipes.isEmpty() || densificationMode != 49 && recipes.stream()
					.map(recipe -> recipe.getRecipeIngredients().entrySet().iterator().next().getValue())
					.noneMatch(integer -> integer == densificationMode)) {
				inventory.removeItem(stack);
				this.ejectItem(inventory.getLocation(), stack, storage);
				continue;
			}

			contents.compute(stack.getType(), (material, integer) -> integer == null ? stack.getAmount() : integer + stack.getAmount());
		}

		Material toDensify = null;
		RecipeWrapper recipe = null;
		int desiredDensification = densificationMode == 49 ? 9 : densificationMode;
		for (Map.Entry<Material, Integer> mapping : contents.entrySet()) {
			if (mapping.getValue() >= desiredDensification) {
				Optional<RecipeWrapper> firstRecipe = recipeCache.getUnchecked(mapping.getKey()).stream()
						.filter(recipeWrapper -> desiredDensification == recipeWrapper.getRecipeIngredients().entrySet().iterator().next().getValue())
						.findFirst();
				if (firstRecipe.isPresent()) {
					toDensify = mapping.getKey();
					recipe = firstRecipe.get();
					break;
				}
			}
			if (toDensify == null && mapping.getValue() >= 4) {
				toDensify = mapping.getKey();
			}
		}

		if (toDensify == null) {
			return;
		}

		if (recipe == null) {
			if (densificationMode != 49) {
				return;
			}
			Optional<RecipeWrapper> firstRecipe = recipeCache.getUnchecked(toDensify).stream()
					.filter(recipeWrapper -> 4 == recipeWrapper.getRecipeIngredients().entrySet().iterator().next().getValue())
					.findFirst();
			if (!firstRecipe.isPresent()) {
				return;
			}
			recipe = firstRecipe.get();
		}

		int toRemove = recipe.getRecipeIngredients().entrySet().iterator().next().getValue();

		inventory.removeItem(new ItemStack(toDensify, toRemove));

		this.ejectItem(inventory.getLocation(), recipe.getResult().clone(), storage);
	}

	private void ejectItem(Location key, ItemStack item, ConfigurationSection storage) {
		Direction facing = this.getDirection(storage).getRelativeDirection(Direction.SOUTH);

		BlockState blockState = key.clone()
				.add(Shape.getRelativeVector(facing, new Vector(0, 0, 1))).getBlock().getState();
		if (blockState instanceof InventoryHolder) {
			// MACHINES InventoryMoveItemEvent
			if (((InventoryHolder) blockState).getInventory().addItem(item).size() == 0) {
				return;
			}
		}

		// Center block location
		key.add(Shape.getRelativeVector(facing, new Vector(0.5D, 0.5D, 1.5D)));
		BlockFace face = facing.toBlockFace();

		// See net.minecraft.server.DispenseBehaviorItem
		Random random = ThreadLocalRandom.current();
		double motionRandom = random.nextDouble() * 0.1D + 0.2D;
		// 0.007499999832361937D * 6
		double motX = face.getModX() * motionRandom + random.nextGaussian() * 0.044999998994171622D;
		double motY = 0.2000000029802322D + random.nextGaussian() * 0.044999998994171622D;
		double motZ = face.getModZ() * motionRandom + random.nextGaussian() * 0.044999998994171622D;

		// MACHINES BlockDispenseEvent
		if (key.getWorld() != null) {
			key.getWorld().dropItem(key, item).setVelocity(new Vector(motX, motY, motZ));
			key.getWorld().playSound(key, Sound.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1F, 1F);
			key.getWorld().playEffect(key, Effect.SMOKE, face);
			key.getWorld().playEffect(key, Effect.SMOKE, face);
		}
	}

	@Override
	public ItemStack getUniqueDrop() {
		return this.drop;
	}

}
