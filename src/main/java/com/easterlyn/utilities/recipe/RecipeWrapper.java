package com.easterlyn.utilities.recipe;

import com.google.common.base.Preconditions;
import net.minecraft.server.v1_13_R2.IRecipe;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.MinecraftKey;
import net.minecraft.server.v1_13_R2.RecipeItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.craftbukkit.v1_13_R2.util.CraftMagicNumbers;
import org.bukkit.inventory.Recipe;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper for NMS recipes.
 *
 * @author Jikoo
 */
public class RecipeWrapper {

	private final Map<EnumSet<Material>, Integer> ingredients;
	private final org.bukkit.inventory.ItemStack result;

	public RecipeWrapper(Recipe recipe) {
		Preconditions.checkArgument(recipe instanceof Keyed, "%s does not implement Keyed!", recipe.getClass());
		Keyed keyed = ((Keyed) recipe);
		IRecipe iRecipe = ((CraftServer) Bukkit.getServer()).getServer().getCraftingManager()
				.a(new MinecraftKey(keyed.getKey().getNamespace(), keyed.getKey().getKey()));

		ingredients = new HashMap<>();

		for (RecipeItemStack ingredient : iRecipe.e()) {
			ingredient.buildChoices();
			EnumSet<Material> materials = EnumSet.noneOf(Material.class);
			for (ItemStack itemStack : ingredient.choices) {
				Material material = CraftMagicNumbers.getMaterial(itemStack.getItem());
				if (material == null) {
					continue;
				}

				switch (material) {
					case AIR:
					case CAVE_AIR:
					case VOID_AIR:
						continue;
					default:
						break;
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
