package co.sblock.events.listeners.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import org.dynmap.DynmapWebChatEvent;

import co.sblock.commands.chat.AetherCommand;
import co.sblock.utilities.messages.Slack;

/**
 * Listener for chat sent via Dynmap.
 * 
 * @author Jikoo
 */
public class DynmapWebChatListener implements Listener {

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onDynmapWebChat(DynmapWebChatEvent event) {
		AetherCommand.sendAether(event.getName(), event.getMessage());
		Slack.getInstance().postMessage(event.getName(), event.getMessage(), true);
		event.setProcessed();
	}
}
