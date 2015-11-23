package co.sblock.chat.ai;

import java.util.Collection;

import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.message.Message;

/**
 * A little abstraction for making new Hal functions cleaner.
 * 
 * @author Jikoo
 */
public abstract class HalMessageHandler {

	private final Sblock plugin;

	public HalMessageHandler(Sblock plugin) {
		this.plugin = plugin;
	}

	public Sblock getPlugin() {
		return plugin;
	}

	public abstract boolean handleMessage(Message message, Collection<Player> recipients);

}
