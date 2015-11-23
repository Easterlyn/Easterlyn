package co.sblock.machines.type.computer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import co.sblock.machines.Machines;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

import net.md_5.bungee.api.ChatColor;

/**
 * A Program allowing a Player to enable server mode.
 * 
 * @author Jikoo
 */
public class SburbServer extends Program {

	private final ItemStack icon;

	public SburbServer(Machines machines) {
		super(machines);
		icon = new ItemStack(Material.ENDER_PORTAL_FRAME);
		ItemMeta meta = icon.getItemMeta();
		meta.setDisplayName(ChatColor.GREEN + "SburbServer");
		icon.setItemMeta(meta);
	}

	@Override
	public void execute(Player player, ItemStack clicked, boolean verified) {
		OfflineUser user = Users.getGuaranteedUser(getMachines().getPlugin(), player.getUniqueId());
		if (!user.isOnline()) {
			return;
		}
		OnlineUser onUser = user.getOnlineUser();
		if (!verified && !onUser.isServer()) {
			((Verification) Programs.getProgramByName("Verification")).openInventory(player, "SburbServer");
			return;
		}
		if (onUser.isServer()) {
			onUser.stopServerMode();
		} else {
			onUser.startServerMode();
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
