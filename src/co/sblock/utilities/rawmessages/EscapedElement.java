package co.sblock.utilities.rawmessages;

import org.bukkit.ChatColor;

/**
 * Prevents... JSONinjection? Is that even a thing? Either way, disallow breaking chat.
 * 
 * @author Jikoo
 */
public class EscapedElement extends MessageElement {

    public EscapedElement(String text) {
        super(text.replace("\\", "\\\\").replace("\"", "\\\""));
    }

    public EscapedElement(String text, ChatColor... colors) {
        super(text.replace("\\", "\\\\").replace("\"", "\\\""), colors);
    }
}
