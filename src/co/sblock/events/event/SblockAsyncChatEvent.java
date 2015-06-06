package co.sblock.events.event;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import co.sblock.chat.message.Message;

/**
 * Event wrapper allowing us to more easily manage our channeled chat system.
 * 
 * @author Jikoo
 */
public class SblockAsyncChatEvent extends AsyncPlayerChatEvent {

	private final Message message;
	private boolean globalCancelled;

	public SblockAsyncChatEvent(boolean async, Player who, Set<Player> players, Message message) {
		super(async, who, message.getMessage(), players);
		setFormat(message.getConsoleFormat());
		this.message = message;
		this.globalCancelled = false;
	}

	@Override
	public void setMessage(String message) {
		// Ignore GP's caps filter, etc.
	}

	public Message getSblockMessage() {
		return message;
	}

	@Override
	public void setCancelled(boolean cancel) {
		super.setCancelled(cancel);
		globalCancelled = false;
	}

	public void setGlobalCancelled(boolean cancelled) {
		if (cancelled && !isCancelled()) {
			globalCancelled = true;
			super.setCancelled(true);
			return;
		}
		globalCancelled = false;
	}

	public boolean isGlobalCancelled() {
		return globalCancelled;
	}
}
