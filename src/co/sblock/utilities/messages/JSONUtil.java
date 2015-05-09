package co.sblock.utilities.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import co.sblock.chat.channel.CanonNick;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * A modified version of vanilla's raw message creation.
 * 
 * @author Jikoo
 **/
public class JSONUtil {
	private static final Pattern LINK_PATTERN = Pattern.compile("((https?://)?(([\\w-_]+\\.)+([a-zA-Z]{2,4}))((#|/)\\S*)?)(\\s|\\z)");

	public static BaseComponent[] getJson(String message, CanonNick quirk) {
		if (message == null || message.isEmpty()) {
			return null;
		}
		BaseComponent[] components = TextComponent.fromLegacyText(message);
		for (int i = 0; i < components.length; i++) {
			TextComponent component = (TextComponent) components[i];
			if (component.getClickEvent() != null) {
				// Link element
				Matcher match = LINK_PATTERN.matcher(component.getText());
				if (match.find()) {
					component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT,
							TextComponent.fromLegacyText(ChatColor.BLUE + component.getText())));
					component.setText(new StringBuilder().append('[').append(match.group(3)).append(']').toString());
					component.setColor(ChatColor.BLUE);
				} else {
					// Default link matcher creates links for Strings such as "okay...fine"
					component.setClickEvent(null);
				}
			} else if (quirk != null) {
				component.setColor(quirk.getColor());
				component.setText(quirk.applyQuirk(message));
			}
		}
		return components;
	}

	public static BaseComponent clone(BaseComponent component) {
		TextComponent clone;
		if (component instanceof TextComponent) {
			clone = new TextComponent(((TextComponent) component).getText());
		} else {
			clone = new TextComponent();
		}
		clone.setColor(component.getColorRaw());
		clone.setBold(component.isBoldRaw());
		clone.setItalic(component.isItalicRaw());
		clone.setUnderlined(component.isUnderlinedRaw());
		clone.setStrikethrough(component.isStrikethroughRaw());
		clone.setObfuscated(component.isObfuscatedRaw());
		clone.setClickEvent(component.getClickEvent());
		clone.setHoverEvent(component.getHoverEvent());
		if (component.getExtra() == null)
			return clone;
		for (BaseComponent basecomponent : component.getExtra()) {
			clone.addExtra(clone(basecomponent));
		}
		return clone;
	}
}
