package com.easterlyn.util.wrapper;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.CraftServer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftMagicNumbers;
import org.bukkit.inventory.Recipe;

/**
 * Wrapper for NMS recipes.
 *
 * @author Jikoo
 */
public class RecipeWrapper {

  private final Map<EnumSet<Material>, Integer> ingredients;
  private final org.bukkit.inventory.ItemStack result;

  public RecipeWrapper(Recipe recipe) {
    Preconditions.checkArgument(
        recipe instanceof Keyed, "%s does not implement Keyed!", recipe.getClass());
    Keyed keyed = ((Keyed) recipe);
    Optional<? extends net.minecraft.world.item.crafting.Recipe<?>> recipeOptional =
        ((CraftServer) Bukkit.getServer())
            .getServer()
            .getRecipeManager()
            .byKey(new ResourceLocation(keyed.getKey().getNamespace(), keyed.getKey().getKey()));

    if (recipeOptional.isEmpty()) {
      ingredients = Collections.emptyMap();
      result = new org.bukkit.inventory.ItemStack(Material.AIR);
      return;
    }

    // TODO handle smithing recipe

    ingredients = new HashMap<>();

    for (Ingredient ingredient : recipeOptional.get().getIngredients()) {
      ingredient.dissolve();
      if (ingredient.itemStacks == null) {
        continue;
      }

      EnumSet<Material> materials = EnumSet.noneOf(Material.class);
      for (ItemStack itemStack : ingredient.itemStacks) {
        Material material = CraftMagicNumbers.getMaterial(itemStack.getItem());
        if (material == null || material.isAir()) {
          continue;
        }

        materials.add(material);
      }

      if (materials.isEmpty()) {
        continue;
      }

      ingredients.compute(materials, (materials1, integer) -> integer == null ? 1 : integer + 1);
    }

    result = recipe.getResult();
  }

  public Map<EnumSet<Material>, Integer> getRecipeIngredients() {
    return this.ingredients;
  }

  public org.bukkit.inventory.ItemStack getResult() {
    return this.result;
  }
}
