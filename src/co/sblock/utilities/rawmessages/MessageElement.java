package co.sblock.utilities.rawmessages;

import java.util.HashSet;
import java.util.LinkedList;

import org.bukkit.ChatColor;

/**
 * Used to create a /tellraw JSON String.
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
	private MessageClick clickEffect = null;
	private MessageHover hoverEffect = null;
	private LinkedList<MessageElement> messageElements= null;

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
				color = ",\"color\":\"" + colors[i].name().toLowerCase() + "\"";
			}
		}
		for (ChatColor c : applicableFormats) {
			formats.append(',').append('"').append(c.name().toLowerCase()).append('"').append(":\"true\"");
		}
	}

	public MessageElement addHoverEffect(MessageHover e) {
		this.hoverEffect = e;
		return this;
	}

	public MessageElement addClickEffect(MessageClick e) {
		this.clickEffect = e;
		return this;
	}

	public MessageElement addExtra(MessageElement... elements) {
		if (messageElements == null) {
			messageElements = new LinkedList<MessageElement>();
		}
		for (int i = 0; i < elements.length; i++) {
			messageElements.add(elements[i]);
		}
		return this;
	}

	public String getText() {
		StringBuilder sb = new StringBuilder(text);

		if (hoverEffect != null) {
			sb.append('(').append(hoverEffect.getData()).append(')');
		}

		if (clickEffect != null) {
			sb.append('(').append(clickEffect.getData()).append(')');
		}

		return sb.toString();
	}

	public String getConsoleFriendly() {
		StringBuilder sb = new StringBuilder(this.getText());
		if (messageElements == null) {
			return sb.toString();
		}
		for (int i = 0; i < messageElements.size(); i++) {
			sb.append(messageElements.get(i).getConsoleFriendly());
		}
		return sb.toString();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("{\"text\":\"");
		sb.append(text).append('"').append(color).append(formats);

		if (hoverEffect != null) {
			sb.append(hoverEffect.toString());
		}

		if (clickEffect != null) {
			sb.append(clickEffect.toString());
		}

		if (messageElements != null) {
			sb.append(",\"extra\":[");
			for (int i = 0; i < messageElements.size(); i++) {
				sb.append(messageElements.get(i)).append(',');
			}
			if (messageElements.size() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			sb.append(']');
		}

		sb.append("}");
		return sb.toString();
	}
}
