package com.easterlyn.chat.ai;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.message.Message;
import org.bukkit.entity.Player;

import java.util.Collection;

/**
 * A little abstraction for making new Hal functions cleaner.
 *
 * @author Jikoo
 */
public abstract class HalMessageHandler {

	private final Easterlyn plugin;

	HalMessageHandler(Easterlyn plugin) {
		this.plugin = plugin;
	}

	public Easterlyn getPlugin() {
		return plugin;
	}

	public abstract boolean handleMessage(Message message, Collection<Player> recipients);

}
