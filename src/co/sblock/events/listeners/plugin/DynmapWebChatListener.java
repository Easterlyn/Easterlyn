package co.sblock.events.listeners.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.dynmap.DynmapWebChatEvent;

import co.sblock.commands.chat.AetherCommand;
import co.sblock.module.Dependency;

/**
 * Listener for chat sent via Dynmap.
 * 
 * @author Jikoo
 */
@Dependency("dynmap")
public class DynmapWebChatListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onDynmapWebChat(DynmapWebChatEvent event) {
		AetherCommand.sendAether(event.getName(), event.getMessage(), false);
		event.setProcessed();
	}
}
