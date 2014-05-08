package co.sblock.utilities.rawmessages;

import org.bukkit.ChatColor;

import co.sblock.chat.ColorDef;

/**
 * Puns. Such puns.
 * 
 * @author Jikoo
 */
public class MessageHalement extends MessageElement {

	public MessageHalement(String text) {
		super(new StringBuilder().append(ColorDef.HAL).append(text).toString(), ChatColor.RED);
	}
}
