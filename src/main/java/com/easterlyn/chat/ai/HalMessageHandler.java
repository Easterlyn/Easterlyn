package com.easterlyn.chat.ai;

import java.util.Collection;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.message.Message;

import org.bukkit.entity.Player;

/**
 * A little abstraction for making new Hal functions cleaner.
 * 
 * @author Jikoo
 */
public abstract class HalMessageHandler {

	private final Easterlyn plugin;

	public HalMessageHandler(Easterlyn plugin) {
		this.plugin = plugin;
	}

	public Easterlyn getPlugin() {
		return plugin;
	}

	public abstract boolean handleMessage(Message message, Collection<Player> recipients);

}
