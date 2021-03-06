package com.easterlyn.util.wrapper;

import com.google.common.base.Preconditions;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.server.v1_16_R3.IRecipe;
import net.minecraft.server.v1_16_R3.ItemStack;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.RecipeItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftMagicNumbers;
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
    Optional<? extends IRecipe<?>> iRecipeOptional =
        ((CraftServer) Bukkit.getServer())
            .getServer()
            .getCraftingManager()
            .getRecipe(new MinecraftKey(keyed.getKey().getNamespace(), keyed.getKey().getKey()));

    if (iRecipeOptional.isEmpty()) {
      ingredients = Collections.emptyMap();
      result = new org.bukkit.inventory.ItemStack(Material.AIR);
      return;
    }

    // TODO handle smithing recipe

    ingredients = new HashMap<>();

    for (RecipeItemStack ingredient : iRecipeOptional.get().a()) {
      ingredient.buildChoices();
      EnumSet<Material> materials = EnumSet.noneOf(Material.class);
      for (ItemStack itemStack : ingredient.choices) {
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
