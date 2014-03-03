package co.sblock.Sblock.Utilities.RawMessages;

import java.util.HashSet;

import org.bukkit.ChatColor;

/**
 * Used to create a valid segment of a larger /tellraw JSON String.
 * <p>
 * Please note that while all Strings can include ChatColors and function,
 * the proper usage is to create a new MessageElement for each color.
 * 
 * @author Jikoo
 */
public class MessageElement {
	private String text;
	String color = new String();
	private StringBuilder formats = new StringBuilder();
	private MessageClickEffect clickEffect = null;
	private MessageHoverEffect hoverEffect = null;

	public MessageElement(String text) {
		this.text = text;
	}
	public MessageElement(String text, ChatColor... colors) {
		this.text = text;
		HashSet<ChatColor> applicableFormats = new HashSet<ChatColor>();
		for (int i = 0; i < colors.length; i++) {
			if (colors[i].isFormat()) {
				applicableFormats.add(colors[i]);
			} else {
				color = ",color:" + colors[i].name().toLowerCase();
			}
		}
		for (ChatColor c : applicableFormats) {
			formats.append(',').append(c.name().toLowerCase()).append(":true");
		}
	}

	public MessageElement addHoverEffect(MessageHoverEffect e) {
		this.hoverEffect = e;
		return this;
	}

	public MessageElement addClickEffect(MessageClickEffect e) {
		this.clickEffect = e;
		return this;
	}

	public String toString() {
		return new StringBuilder("{\"text\":\"").append(text).append('"').append(color)
				.append(formats).append(hoverEffect != null ? hoverEffect.toString() : new String())
				.append(clickEffect != null ? clickEffect.toString() : new String()).append("}").toString();
	}
}
