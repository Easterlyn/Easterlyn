package co.sblock.machines.type.computer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Dye;

import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.users.UserAspect;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * @author Jikoo
 */
public class GodtierToggle extends Program {

	private final ItemStack icon, icoff;

	protected GodtierToggle() {
		Dye wool = new Dye(Material.WOOL);
		wool.setColor(DyeColor.LIME);
		icon = wool.toItemStack();

		wool.setColor(DyeColor.RED);
		icoff = wool.toItemStack();

		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.WOOL);
		meta.setDisplayName(ChatColor.GREEN + "Toggle");
		icon.setItemMeta(meta);
		icoff.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		// TODO Auto-generated method stub

	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	public ItemStack getIcon(Effect effect, UserAspect aspect, boolean enabled) {
		ItemStack stack = enabled ? icon.clone() : icoff.clone();
		ItemMeta meta = stack.getItemMeta();
		ArrayList<String> lore = new ArrayList<>();
		lore.add(ChatColor.WHITE + "toggle " + effect.getName());
		lore.addAll(((BehaviorGodtier) effect).getDescription(aspect));
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

}
