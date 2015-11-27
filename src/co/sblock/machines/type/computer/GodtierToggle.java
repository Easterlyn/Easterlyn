package co.sblock.machines.type.computer;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import co.sblock.effects.Effects;
import co.sblock.effects.effect.BehaviorGodtier;
import co.sblock.effects.effect.Effect;
import co.sblock.machines.Machines;
import co.sblock.users.User;
import co.sblock.users.UserAspect;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * @author Jikoo
 */
public class GodtierToggle extends Program {

	private final Effects effects;
	private final Users users;
	private final ItemStack icon, icoff;

	public GodtierToggle(Machines machines) {
		super(machines);
		this.effects = machines.getPlugin().getModule(Effects.class);
		this.users = machines.getPlugin().getModule(Users.class);
		Wool wool = new Wool();
		wool.setColor(DyeColor.LIME);
		icon = wool.toItemStack();
		icon.setAmount(1);

		wool.setColor(DyeColor.RED);
		icoff = wool.toItemStack();
		icoff.setAmount(1);

		ItemMeta meta = Bukkit.getItemFactory().getItemMeta(Material.WOOL);
		meta.setDisplayName(ChatColor.YELLOW + "Toggle " + ChatColor.GREEN + "ON"
				+ ChatColor.YELLOW + " or " + ChatColor.RED + "OFF");
		icon.setItemMeta(meta);
		icoff.setItemMeta(meta);
	}

	@Override
	protected void execute(Player player, ItemStack clicked, boolean verified) {
		if (!clicked.hasItemMeta() || !clicked.getItemMeta().hasLore()
				|| clicked.getItemMeta().getLore().isEmpty()) {
			return;
		}
		User user = users.getUser(player.getUniqueId());
		String effectName = clicked.getItemMeta().getLore().get(0).replaceFirst("..toggle ", "");
		Effect effect = effects.getEffect(effectName);
		if (effect == null) {
			return;
		}
		ItemStack newClicked;
		if (clicked.getData().equals(icon.getData())) {
			user.removeGodtierEffect(effect);
			newClicked = getIcon(effect, user.getUserAspect(), false);
		} else if (clicked.getData().equals(icoff.getData())) {
			
			newClicked = getIcon(effect, user.getUserAspect(), user.addGodtierEffect(effect));
		} else {
			return;
		}
		Inventory top = player.getOpenInventory().getTopInventory();
		for (int i = 0; i < top.getSize(); i++) {
			if (clicked.isSimilar(top.getItem(i))) {
				top.setItem(i, newClicked);
				break;
			}
		}
		player.updateInventory();
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
