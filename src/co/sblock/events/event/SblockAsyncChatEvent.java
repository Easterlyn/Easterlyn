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

	public SblockAsyncChatEvent(boolean async, Player who, Set<Player> players, Message message) {
		super(async, who, message.getMessage(), players);
		setFormat(message.getConsoleFormat());
		this.message = message;
	}

	@Override
	public void setMessage(String message) {
		// Ignore GP's caps filter, etc.
	}

	public Message getSblockMessage() {
		return message;
	}
}
