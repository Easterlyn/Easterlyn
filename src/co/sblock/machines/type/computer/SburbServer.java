package co.sblock.machines.type.computer;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * A Program allowing a Player to enable server mode.
 * 
 * @author Jikoo
 */
public class SburbServer extends Program {

	@Override
	public void openInventory(Player player, ItemStack clicked, boolean verified) {
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
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
	public ItemStack getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack getInstaller() {
		// TODO Auto-generated method stub
		return null;
	}

}
