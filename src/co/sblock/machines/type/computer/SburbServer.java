package co.sblock.machines.type.computer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;
import co.sblock.users.User;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * A Program allowing a Player to enable server mode.
 * 
 * @author Jikoo
 */
public class SburbServer extends Program {

	private final Users users;
	private final ItemStack icon;

	public SburbServer(Machines machines) {
		super(machines);
		this.users = machines.getPlugin().getModule(Users.class);
		icon = new ItemStack(Material.ENDER_PORTAL_FRAME);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "SburbServer");
		icon.setItemMeta(meta);
	}

	@Override
	public void execute(Player player, ItemStack clicked, boolean verified) {
		User user = users.getUser(player.getUniqueId());
		if (!user.isOnline()) {
			return;
		}
		if (!verified && !user.isServer()) {
			((Verification) Programs.getProgramByName("Verification")).openInventory(player, "SburbServer");
			return;
		}
		if (user.isServer()) {
			user.stopServerMode();
		} else {
			user.startServerMode();
		}
	}

	@Override
	public boolean isDefault() {
		return true;
	}

	@Override
	public ItemStack getIcon() {
		return icon;
	}

	@Override
	public ItemStack getInstaller() {
		return null;
	}

}
