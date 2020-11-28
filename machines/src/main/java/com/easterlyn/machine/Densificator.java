package com.easterlyn.machine;

import com.easterlyn.EasterlynMachines;
import com.easterlyn.util.Direction;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.Shape;
import com.easterlyn.util.inventory.Button;
import com.easterlyn.util.inventory.InventoryUtil;
import com.easterlyn.util.inventory.SimpleUI;
import com.easterlyn.util.wrapper.RecipeWrapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import org.jetbrains.annotations.NotNull;

/**
 * Condense items automatically.
 *
 * @author Jikoo
 */
public class Densificator extends Machine {

  private static final LoadingCache<Material, List<RecipeWrapper>> recipeCache;
  private static final EnumSet<Material> materialWhitelist;

  static {
    materialWhitelist = EnumSet.of(Material.GOLD_INGOT, Material.IRON_INGOT, Material.LEATHER);

    for (Material material : Material.values()) {
      if (material.isOccluding()) {
        materialWhitelist.add(material);
      }
    }

    recipeCache =
        CacheBuilder.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .build(
                new CacheLoader<>() {
                  @Override
                  public List<RecipeWrapper> load(@NotNull Material material) {
                    ArrayList<RecipeWrapper> list = new ArrayList<>();

                    Bukkit.recipeIterator()
                        .forEachRemaining(
                            recipe -> {
                              if (!(recipe instanceof ShapedRecipe
                                  || recipe instanceof ShapelessRecipe)) {
                                return;
                              }

                              if (!materialWhitelist.contains(recipe.getResult().getType())) {
                                return;
                              }

                              RecipeWrapper wrapper = new RecipeWrapper(recipe);

                              Map<EnumSet<Material>, Integer> ingredients =
                                  wrapper.getRecipeIngredients();
                              if (ingredients.size() != 1) {
                                return;
                              }

                              Map.Entry<EnumSet<Material>, Integer> ingredient =
                                  ingredients.entrySet().iterator().next();

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
                });
  }

  private final ItemStack drop;

  public Densificator(EasterlynMachines machines) {
    super(machines, new Shape(), "Densificator");

    getShape()
        .setVectorData(
            new Vector(0, 0, 0),
            new Shape.MaterialDataValue(Material.DROPPER)
                .withBlockData(Directional.class, Direction.SOUTH));
    getShape()
        .setVectorData(
            new Vector(0, 1, 0),
            new Shape.MaterialDataValue(Material.PISTON)
                .withBlockData(Directional.class, Direction.DOWN));

    this.drop = new ItemStack(Material.PISTON);
    GenericUtil.consumeAs(
        ItemMeta.class,
        drop.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Densificator");
          this.drop.setItemMeta(itemMeta);
        });

    ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(machines, "densificator"), drop);
    recipe.shape(" A ", "XYX", " Z ");
    recipe.setIngredient('A', Material.HOPPER);
    recipe.setIngredient('X', Material.PISTON);
    recipe.setIngredient('Y', Material.COMPARATOR);
    recipe.setIngredient('Z', Material.DROPPER);
    machines.getServer().addRecipe(recipe);
  }

  private int getDensificationMode(ConfigurationSection storage) {
    return storage.getInt("densification", 49);
  }

  private int adjustDensificationMode(ConfigurationSection storage, int difference) {
    int densification = getDensificationMode(storage) + difference;
    if (densification < 49 && densification > 9 && difference > 0
        || densification < 4 && difference < 0) {
      densification = 49;
    } else if (densification < 9 && difference < 0 || densification > 49 && difference > 0) {
      densification = 4;
    } else {
      densification = 9;
    }
    storage.set("densification", densification);
    return densification;
  }

  @Override
  public void handleInteract(
      @NotNull PlayerInteractEvent event, @NotNull ConfigurationSection storage) {
    super.handleInteract(event, storage);

    if (event.useInteractedBlock() == Event.Result.DENY
        || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
      return;
    }

    event.getPlayer().openInventory(getInventory(storage));
  }

  @Override
  public void handleOpen(@NotNull InventoryOpenEvent event, ConfigurationSection storage) {
    if (event.getInventory().getType() != InventoryType.DROPPER) {
      return;
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
        InventoryUtil.changeWindowName(player, getName());
      }
    }.runTask(getMachines());
  }

  @Override
  public void handleClick(@NotNull InventoryClickEvent event, ConfigurationSection storage) {
    if (event.getInventory().getType() == InventoryType.DROPPER) {
      updateLater(event.getView().getTopInventory(), storage);
    }
  }

  @Override
  public void handleDrag(@NotNull InventoryDragEvent event, ConfigurationSection storage) {
    this.updateLater(event.getView().getTopInventory(), storage);
  }

  @Override
  public void handleHopperMoveItem(
      @NotNull InventoryMoveItemEvent event, @NotNull ConfigurationSection storage) {
    this.updateLater(event.getDestination(), storage);
  }

  private void updateLater(final Inventory inventory, ConfigurationSection storage) {
    new BukkitRunnable() {
      @Override
      public void run() {
        update(inventory, storage);
      }
    }.runTask(getMachines());
  }

  private void update(Inventory inventory, ConfigurationSection storage) {
    if (inventory.getLocation() == null) {
      return;
    }
    Map<Material, Integer> contents = new HashMap<>();
    int densificationMode = this.getDensificationMode(storage);
    for (ItemStack stack : inventory.getContents()) {
      //noinspection ConstantConditions
      if (stack == null || stack.getType() == Material.AIR) {
        continue;
      }

      List<RecipeWrapper> recipes = recipeCache.getUnchecked(stack.getType());

      if (recipes.isEmpty()
          || densificationMode != 49
              && recipes.stream()
                  .map(
                      recipe ->
                          recipe.getRecipeIngredients().entrySet().iterator().next().getValue())
                  .noneMatch(integer -> integer == densificationMode)) {
        inventory.removeItem(stack);
        this.ejectItem(inventory.getLocation(), stack, storage);
        continue;
      }

      contents.compute(
          stack.getType(),
          (material, integer) -> integer == null ? stack.getAmount() : integer + stack.getAmount());
    }

    Material toDensify = null;
    RecipeWrapper recipe = null;
    int desiredDensification = densificationMode == 49 ? 9 : densificationMode;
    for (Map.Entry<Material, Integer> mapping : contents.entrySet()) {
      if (mapping.getValue() >= desiredDensification) {
        Optional<RecipeWrapper> firstRecipe =
            recipeCache.getUnchecked(mapping.getKey()).stream()
                .filter(
                    recipeWrapper ->
                        desiredDensification
                            == recipeWrapper
                                .getRecipeIngredients()
                                .entrySet()
                                .iterator()
                                .next()
                                .getValue())
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
      Optional<RecipeWrapper> firstRecipe =
          recipeCache.getUnchecked(toDensify).stream()
              .filter(
                  recipeWrapper ->
                      4
                          == recipeWrapper
                              .getRecipeIngredients()
                              .entrySet()
                              .iterator()
                              .next()
                              .getValue())
              .findFirst();
      if (firstRecipe.isEmpty()) {
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

    BlockState blockState =
        key.clone().add(Shape.getRelativeVector(facing, new Vector(0, 0, 1))).getBlock().getState();
    if (blockState instanceof InventoryHolder) {
      // TODO InventoryMoveItemEvent? Currently prevents instant chaining.
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

    // TODO BlockDispenseEvent
    if (key.getWorld() != null) {
      key.getWorld().dropItem(key, item).setVelocity(new Vector(motX, motY, motZ));
      key.getWorld().playSound(key, Sound.BLOCK_DISPENSER_DISPENSE, SoundCategory.BLOCKS, 1F, 1F);
      key.getWorld().playEffect(key, Effect.SMOKE, face);
      key.getWorld().playEffect(key, Effect.SMOKE, face);
    }
  }

  @Override
  public @NotNull ItemStack getUniqueDrop() {
    return this.drop;
  }

  private @NotNull Inventory getInventory(@NotNull ConfigurationSection storage) {
    SimpleUI ui = new SimpleUI(getMachines(), "Densificator Configuration");
    ItemStack itemStack1 = new ItemStack(Material.RED_WOOL);
    GenericUtil.consumeAs(
        ItemMeta.class,
        itemStack1.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Cycle Densification");
          itemStack1.setItemMeta(itemMeta);
        });
    ui.setButton(
        3,
        new Button(
            itemStack1,
            event -> {
              int amount = adjustDensificationMode(storage, -1);
              Button display = ui.getButton(4);
              if (display != null) {
                // Item is not cloned, this is fine.
                display.getItem().setAmount(amount);
              }
              ui.draw(event.getView().getTopInventory());
            }));
    ItemStack itemStack2 = new ItemStack(Material.CRAFTING_TABLE);
    GenericUtil.consumeAs(
        ItemMeta.class,
        itemStack2.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Densification Mode");
          itemMeta.setLore(
              Arrays.asList(
                  ChatColor.WHITE + "2x2 (4), 3x3 (9), or", ChatColor.WHITE + "3x3 AND 2x2 (49)"));
          itemStack2.setItemMeta(itemMeta);
        });
    ui.setButton(4, new Button(itemStack2, event -> {}));
    ItemStack itemStack3 = new ItemStack(Material.LIME_WOOL);
    GenericUtil.consumeAs(
        ItemMeta.class,
        itemStack3.getItemMeta(),
        itemMeta -> {
          itemMeta.setDisplayName(ChatColor.WHITE + "Cycle Densification");
          itemStack3.setItemMeta(itemMeta);
        });
    ui.setButton(
        5,
        new Button(
            itemStack3,
            event -> {
              int amount = adjustDensificationMode(storage, 1);
              Button display = ui.getButton(4);
              if (display != null) {
                display.getItem().setAmount(amount);
              }
              ui.draw(event.getView().getTopInventory());
            }));
    return ui.getInventory();
  }
}
