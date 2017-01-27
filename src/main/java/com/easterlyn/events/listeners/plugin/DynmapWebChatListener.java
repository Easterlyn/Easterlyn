package com.easterlyn.events.listeners.plugin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.chat.AetherCommand;
import com.easterlyn.events.listeners.SblockListener;
import com.easterlyn.module.Dependency;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.dynmap.DynmapWebChatEvent;

/**
 * Listener for chat sent via Dynmap.
 * 
 * @author Jikoo
 */
@Dependency("dynmap")
public class DynmapWebChatListener extends SblockListener {

	private AetherCommand aether;

	public DynmapWebChatListener(Easterlyn plugin) {
		super(plugin);
		
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onDynmapWebChat(DynmapWebChatEvent event) {
		if (aether == null) {
			aether = (AetherCommand) getPlugin().getCommandMap().getCommand("sblock:aether");
		}
		aether.sendAether(null, event.getName(), event.getMessage(), false);
		event.setProcessed();
	}

}
