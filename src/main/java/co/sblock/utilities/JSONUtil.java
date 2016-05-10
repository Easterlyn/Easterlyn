package co.sblock.utilities;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.inventory.ItemStack;

import co.sblock.chat.Language;
import co.sblock.chat.channel.CanonNick;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;

import net.minecraft.server.v1_9_R2.NBTTagCompound;

import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;

/**
 * A modified version of vanilla's raw message creation.
 * 
 * @author Jikoo
 **/
public class JSONUtil {

	private static final Pattern CHANNEL_PATTERN = Pattern.compile("^(#[A-Za-z0-9]{0,15})([^A-Za-z0-9])?$");

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
		Matcher urlMatcher = TextUtils.URL_PATTERN.matcher(message);
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
				component.setText('[' + urlMatcher.group(2).toLowerCase() + ']');
				component.setColor(Language.getColor("link_color"));
				TextComponent[] hover = { new TextComponent(urlString) };
				hover[0].setColor(Language.getColor("link_color"));
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
				component.setColor(Language.getColor("link_channel"));
				component.setUnderlined(true);
				component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT,
						TextComponent.fromLegacyText(Language.getColor("command") + "/join " + Language.getColor("link_channel") + channelString)));
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

	public static TextComponent getItemComponent(ItemStack item) {
		net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		String name = null;
		if (nmsStack.getTag() != null && nmsStack.getTag().hasKeyOfType("display", 10)) {
			NBTTagCompound nbttagcompound = nmsStack.getTag().getCompound("display");
			if (nbttagcompound.hasKeyOfType("Name", 8)) {
				name = nbttagcompound.getString("Name");
			}
		}
		boolean named = name != null;
		if (!named) {
			name = nmsStack.getItem().a(nmsStack);
		}
		TextComponent component = new TextComponent(JSONUtil.fromLegacyText(nmsStack.getName()));
		for (int i = 0; i < component.getExtra().size(); i++) {
			BaseComponent baseExtra = component.getExtra().get(i);
			if (baseExtra.hasFormatting()) {
				break;
			}
			baseExtra.setColor(ChatColor.AQUA);
			if (named) {
				baseExtra.setItalic(true);
			}
		}
		component.setHoverEvent(getItemHover(nmsStack));
		return component;
	}

	public static TextComponent getItemText(ItemStack item) {
		return getItemText(CraftItemStack.asNMSCopy(item));
	}

	private static TextComponent getItemText(net.minecraft.server.v1_9_R2.ItemStack item) {
		return new TextComponent(item.save(new NBTTagCompound()).toString());
	}

	private static HoverEvent getItemHover(net.minecraft.server.v1_9_R2.ItemStack item) {
		return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[] { getItemText(item) });
	}
}
