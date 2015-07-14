package co.sblock.utilities.messages;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.inventory.ItemStack;

import co.sblock.chat.Color;
import co.sblock.chat.channel.CanonNick;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import net.minecraft.server.v1_8_R3.NBTTagCompound;

/**
 * A modified version of vanilla's raw message creation.
 * 
 * @author Jikoo
 **/
public class JSONUtil {

	private static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?(([\\w-_]+\\.)+([a-zA-Z]{2,4}))((#|/)\\S*)?$");
	private static final Pattern CHANNEL_PATTERN = Pattern.compile("^(#[A-Za-z0-9]{0,})(\\W)?$");

	public static BaseComponent[] getJson(String message, CanonNick quirk) {
		if (message == null || message.isEmpty()) {
			return null;
		}
		BaseComponent[] components = fromLegacyText(message);
		for (int i = 0; i < components.length; i++) {
			TextComponent component = (TextComponent) components[i];
			if (component.getClickEvent() == null && quirk != null) {
				component.setColor(quirk.getColor());
				component.setText(quirk.applyQuirk(component.getText()));
			}
		}
		return components;
	}

	public static TextComponent[] fromLegacyText(String message) {
		ArrayList<BaseComponent> components = new ArrayList<BaseComponent>();
		StringBuilder builder = new StringBuilder();
		TextComponent component = new TextComponent();
		Matcher urlMatcher = URL_PATTERN.matcher(message);
		Matcher channelMatcher = CHANNEL_PATTERN.matcher(message);

		for (int i = 0; i < message.length(); i++) {
			char c = message.charAt(i);
			if (c == ChatColor.COLOR_CHAR) {
				i++;
				c = message.charAt(i);
				if (c >= 'A' && c <= 'Z') {
					c += 32;
				}
				ChatColor format = ChatColor.getByChar(c);
				if (format == null) {
					continue;
				}
				if (builder.length() > 0) {
					TextComponent old = component;
					component = new TextComponent(old);
					old.setText(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}
				switch (format) {
				case BOLD:
					component.setBold(true);
					break;
				case ITALIC:
					component.setItalic(true);
					break;
				case UNDERLINE:
					component.setUnderlined(true);
					break;
				case STRIKETHROUGH:
					component.setStrikethrough(true);
					break;
				case MAGIC:
					component.setObfuscated(true);
					break;
				case RESET:
					format = ChatColor.WHITE;
				default:
					component = new TextComponent();
					component.setColor(format);
					break;
				}
				continue;
			}
			int pos = message.indexOf(' ', i);
			if (pos == -1) {
				pos = message.length();
			}
			if (urlMatcher.region(i, pos).find()) { // Web link handling

				if (builder.length() > 0) {
					TextComponent old = component;
					component = new TextComponent(old);
					old.setText(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}

				TextComponent old = component;
				component = new TextComponent(old);
				String urlString = message.substring(i, pos);
				urlString = urlString.startsWith("http") ? urlString : "http://" + urlString;
				component.setText('[' + urlMatcher.group(2) + ']');
				component.setColor(ChatColor.BLUE);
				TextComponent[] hover = { new TextComponent(urlString) };
				hover[0].setColor(ChatColor.BLUE);
				component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
				component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, urlString));
				components.add(component);
				i += pos - i - 1;
				component = old;
				continue;
			}
			if (channelMatcher.region(i, pos).find()) { // Channel mentions

				if (builder.length() > 0) {
					TextComponent old = component;
					component = new TextComponent(old);
					old.setText(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}

				TextComponent old = component;
				component = new TextComponent(old);
				String channelString = channelMatcher.group(1);
				int end = channelMatcher.end(1);
				component.setText(message.substring(i, end));
				component.setColor(Color.GOOD_EMPHASIS);
				component.setUnderlined(true);
				component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT,
						TextComponent.fromLegacyText(Color.COMMAND + "/join " + Color.GOOD_EMPHASIS + channelString)));
				component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + channelString));
				components.add(component);

				if (end < pos) {
					component = new TextComponent(old);
					component.setText(message.substring(end, pos));
					components.add(component);
				}
				i += pos - i - 1;
				component = old;
				continue;
			}
			builder.append(c);
		}
		if (builder.length() > 0) {
			component.setText(builder.toString());
			components.add(component);
		}

		// The client will crash if the array is empty
		if (components.isEmpty()) {
			components.add(new TextComponent(""));
		}

		return components.toArray(new TextComponent[components.size()]);
	}

	public static TextComponent getItemText(ItemStack item) {
		net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		return new TextComponent(nmsStack.save(new NBTTagCompound()).toString());
	}

	public static HoverEvent getItemHover(ItemStack item) {
		return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[] { getItemText(item) });
	}
}
