package com.easterlyn.kitchensink.listener;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class ColorSignText implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		String[] lines = event.getLines();
		for (int i = 0; i < lines.length; i++) {
			String line = lines[i];
			if (line == null || line.isEmpty()) {
				continue;
			}
			event.setLine(i, ChatColor.translateAlternateColorCodes('&', line));
		}
	}

}
