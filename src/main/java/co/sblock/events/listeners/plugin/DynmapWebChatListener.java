package co.sblock.events.listeners.plugin;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import org.dynmap.DynmapWebChatEvent;

import co.sblock.Sblock;
import co.sblock.commands.chat.AetherCommand;
import co.sblock.events.listeners.SblockListener;
import co.sblock.module.Dependency;

/**
 * Listener for chat sent via Dynmap.
 * 
 * @author Jikoo
 */
@Dependency("dynmap")
public class DynmapWebChatListener extends SblockListener {

	private AetherCommand aether;

	public DynmapWebChatListener(Sblock plugin) {
		super(plugin);
		
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onDynmapWebChat(DynmapWebChatEvent event) {
		if (aether == null) {
			aether = (AetherCommand) getPlugin().getCommandMap().getCommand("sblock:aether");
		}
		aether.sendAether(event.getName(), event.getMessage(), false);
		event.setProcessed();
	}
}
