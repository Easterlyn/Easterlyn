package co.sblock.commands.info;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.DiscordPlayer;
import co.sblock.utilities.InventoryUtils;

/**
 * Better recipe lookup.
 * 
 * @author Jikoo
 */
public class RecipeCommand extends SblockCommand {

	public RecipeCommand(Sblock plugin) {
		super(plugin, "recipe");
		this.setDescription("Check a recipe");
		this.setUsage("/recipe <material> [page] - check https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		int recipeNumber = 1;
		boolean setRecipeNumber = false;
		if (args.length > 1) {
			try {
				recipeNumber = Integer.valueOf(args[args.length - 1]);
				setRecipeNumber = true;
			} catch (NumberFormatException e) {
				// No recipe number provided
			}
		}
		Pair<Material, Short> type = InventoryUtils.matchMaterial(StringUtils.join(args, ' ', 0, setRecipeNumber ? args.length - 1 : args.length));
		if (type == null) {
			return false;
		}
		ItemStack result = new ItemStack(type.getLeft(), 1, type.getRight());
		List<Recipe> recipes = Bukkit.getRecipesFor(result);
		String friendlyName = InventoryUtils.getMaterialDataName(type.getLeft(), type.getRight());
		if (recipes.isEmpty()) {
			sender.sendMessage(Color.BAD + "No recipes found for " + friendlyName);
			return true;
		}
		if (recipeNumber < 1 || recipeNumber > recipes.size()) {
			sender.sendMessage(Color.BAD + "Invalid recipe number! Only " + recipes.size() + " recipe(s) available.");
		}
		Recipe recipe = recipes.get(recipeNumber - 1);
		if (sender instanceof Player && !(sender instanceof DiscordPlayer)
				&& (recipe instanceof ShapedRecipe || recipe instanceof ShapelessRecipe)) {
			showInInventory((Player) sender, recipe, friendlyName);
		} else {
			showInText(sender, recipe, friendlyName);
		}
		return true;
	}

	private void showInText(CommandSender sender, Recipe recipe, String name) {
		// TODO implement
	}

	private void showInInventory(Player player, Recipe recipe, String name) {
		if (recipe instanceof ShapelessRecipe) {
			CraftingInventory inv = (CraftingInventory) Bukkit.createInventory(null, InventoryType.CRAFTING, name);
			for (ItemStack item : ((ShapelessRecipe) recipe).getIngredientList()) {
				inv.addItem(item);
			}
			inv.setResult(recipe.getResult());
			// TODO show player, ensure unremovable
			return;
		}
		if (recipe instanceof ShapedRecipe) {
			CraftingInventory inv = (CraftingInventory) Bukkit.createInventory(null, InventoryType.CRAFTING, name);
			ShapedRecipe shaped = (ShapedRecipe) recipe;
			for (int i = 0; i < 3; i++) {
				for (int j = 0; j < 3; j++) {
					if (shaped.getShape()[i].length() <= j) {
						continue;
					}
					// TODO check this
					ItemStack item = shaped.getIngredientMap().get(shaped.getShape()[i].charAt(j));
					if (item != null && item.getDurability() == Short.MAX_VALUE) {
						// Prevent checkerboard missing textures
						item.setDurability((short) 0);
					}
					inv.setItem(i * 3 + j, item);
				}
			}
			// TODO show player, ensure unremovable
		}
	}

}