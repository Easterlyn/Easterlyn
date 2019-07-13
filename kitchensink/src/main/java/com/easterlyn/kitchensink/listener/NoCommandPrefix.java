package com.easterlyn.kitchensink.listener;

import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class NoCommandPrefix implements Listener {

	public NoCommandPrefix() {
		PermissionUtil.addParent("easterlyn.commands.unfiltered", UserRank.MODERATOR.getPermission());
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("easterlyn.commands.unfiltered") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon + 1));
		}
		// TODO: prevent tab completion with ProtocolLib
	}

}
