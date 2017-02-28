package com.easterlyn.events.event;

import java.util.Set;

import com.easterlyn.chat.message.Message;
import com.easterlyn.utilities.CollectionConversions;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Event wrapper allowing us to more easily manage our channeled chat system.
 * 
 * @author Jikoo
 */
public class EasterlynAsyncChatEvent extends AsyncPlayerChatEvent {

	private final Message message;
	private boolean globalCancelled;
	private final boolean checkSpam;

	public EasterlynAsyncChatEvent(boolean async, Player who, Message message) {
		this(async, who, message, true);
	}

	public EasterlynAsyncChatEvent(boolean async, Player who, Message message, boolean checkSpam) {
		this(async, who, CollectionConversions.toSet(message.getChannel().getListening(),
				uuid -> Bukkit.getPlayer(uuid)), message, checkSpam);
	}

	public EasterlynAsyncChatEvent(boolean async, Player who, Set<Player> players, Message message) {
		this(async, who, players, message, true);
	}

	public EasterlynAsyncChatEvent(boolean async, Player who, Set<Player> players, Message message, boolean checkSpam) {
		super(async, who, message.getRawMessage(), players);
		setFormat(message.getConsoleFormat());
		this.message = message;
		this.checkSpam = checkSpam;
		this.globalCancelled = false;
	}

	public boolean checkSpam() {
		return checkSpam;
	}

	public Message getEasterlynMessage() {
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
