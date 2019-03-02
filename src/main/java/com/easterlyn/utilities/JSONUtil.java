package com.easterlyn.utilities;

import com.easterlyn.chat.Language;
import com.easterlyn.utilities.TextUtils.MatchedURL;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A modified version of vanilla's raw message creation.
 *
 * @author Jikoo
 **/
public class JSONUtil {

	private static final Pattern CHANNEL_PATTERN = Pattern.compile("^(#[A-Za-z0-9]{0,15})([^A-Za-z0-9])?$");

	public static TextComponent[] fromLegacyText(String message) {
		ArrayList<BaseComponent> components = new ArrayList<>();
		StringBuilder builder = new StringBuilder();
		TextComponent component = new TextComponent();
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
			MatchedURL url = TextUtils.matchURL(message.substring(i, pos));
			if (url != null) { // Web link handling

				if (builder.length() > 0) {
					TextComponent old = component;
					component = new TextComponent(old);
					old.setText(builder.toString());
					builder = new StringBuilder();
					components.add(old);
				}

				TextComponent old = component;
				component = new TextComponent(old);
				component.setText('[' + url.getPath() + ']');
				component.setColor(Language.getColor("link_color"));
				TextComponent[] hover = { new TextComponent(url.getFullURL()) };
				hover[0].setColor(Language.getColor("link_color"));
				component.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
				component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url.getFullURL()));
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

		//noinspection SuspiciousToArrayCall
		return components.toArray(new TextComponent[0]);
	}

	public static TextComponent getItemComponent(ItemStack item) {
		net.minecraft.server.v1_13_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		boolean named = nmsStack.hasName();
		TextComponent component = new TextComponent(JSONUtil.fromLegacyText(nmsStack.getName().getText()));
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

	public static String getItemText(ItemStack item) {
		return getItemText(CraftItemStack.asNMSCopy(item));
	}

	private static String getItemText(net.minecraft.server.v1_13_R2.ItemStack item) {
		return item.save(new NBTTagCompound()).toString();
	}

	private static HoverEvent getItemHover(net.minecraft.server.v1_13_R2.ItemStack item) {
		return new HoverEvent(HoverEvent.Action.SHOW_ITEM, new BaseComponent[] { new TextComponent(getItemText(item)) });
	}

}
