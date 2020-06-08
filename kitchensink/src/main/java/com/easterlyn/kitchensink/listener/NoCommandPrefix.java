package com.easterlyn.kitchensink.listener;

import com.comphenix.protocol.ProtocolLibrary;
import com.easterlyn.EasterlynCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class NoCommandPrefix implements Listener {

	public NoCommandPrefix(EasterlynCore plugin) {
		try {
			Class.forName("com.comphenix.protocol.ProtocolLibrary");
			ProtocolLibrary.getProtocolManager().addPacketListener(new com.easterlyn.kitchensink.listener.RestrictTabCompletion(plugin));
		} catch (NoClassDefFoundError | ClassNotFoundException ignored) {}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("easterlyn.commands.unfiltered") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon + 1));
		}
	}

}
