package co.sblock.utilities.rawmessages;

import org.bukkit.ChatColor;

/**
 * Puns. Such puns.
 * 
 * @author Jikoo
 */
public class MessageHalement extends MessageElement {

	public MessageHalement(String text) {
		super(new StringBuilder().append(ChatColor.WHITE).append('[').append(ChatColor.RED)
				.append('#').append(ChatColor.WHITE).append("] <").append(ChatColor.DARK_RED)
				.append("Lil Hal").append(ChatColor.WHITE).append("> ").append(ChatColor.RED)
				.append(text).toString(), ChatColor.RED);
	}
}
