package co.sblock.utilities.rawmessages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;

import co.sblock.chat.channel.CanonNicks;

/**
 * DarkSeraphim's ChatColor to JSON colored element converter modified with a couple Sblock features.
 * 
 * @author DarkSeraphim
 **/
public class JSONUtil {
	private static final StringBuilder JSON_WRAPPER = new StringBuilder("{\"text\":\"\",\"extra\":[");

	private static final int RETAIN = "{\"text\":\"\",\"extra\":[".length();

	public static final String getWrappedJSON(String... jsonElements) {
		if (JSON_WRAPPER.length() > RETAIN) {
			JSON_WRAPPER.delete(RETAIN, JSON_WRAPPER.length());
		}
		if (jsonElements.length == 0) {
			throw new IllegalArgumentException("JSON elements are required to construct a wrapped JSON message!");
		}
		for (String element : jsonElements) {
			JSON_WRAPPER.append(element).append(",");
		}
		return JSON_WRAPPER.deleteCharAt(JSON_WRAPPER.length() - 1).append("]}").toString();
	}

	private static final StringBuilder JSON_BUILDER = new StringBuilder();

	public static String toJSONElements(String message, boolean injectLinks, CanonNicks quirk) {
		if (message == null || message.isEmpty()) {
			return null;
		}
		if (JSON_BUILDER.length() > 0) {
			JSON_BUILDER.delete(0, JSON_BUILDER.length());
		}
		String[] parts = message.split(Character.toString(ChatColor.COLOR_CHAR));
		boolean first = true;
		String colour = null;
		String format = null;
		for (String part : parts) {
			// If it starts with a colour, just ignore the empty String before it
			if (part.isEmpty())
				continue;
			String newStyle = getStyle(part.charAt(0));
			if (newStyle != null) {
				part = part.substring(1);
				if (newStyle.startsWith("\"c"))
					colour = newStyle;
				else
					format = newStyle;
			}
			if (!part.isEmpty()) {
				if (first)
					first = false;
				else {
					JSON_BUILDER.append(",");
				}
				JSON_BUILDER.append("{");
				if (colour != null) {
					JSON_BUILDER.append(colour);
					colour = null;
				}
				if (format != null) {
					JSON_BUILDER.append(format);
					format = null;
				}
				if (injectLinks) {
					part = injectLinks(part, quirk);
				} else if (quirk != null) {
					part = quirk.applyQuirk(part);
				}
				JSON_BUILDER.append(String.format("\"text\":\"%s", part));
				if (!injectLinks) {
					JSON_BUILDER.append('\"');
				}
				JSON_BUILDER.append('}');
			}
		}
		// Remove trailing commas - should be handled by getWrappedJSON
		if (JSON_BUILDER.charAt(JSON_BUILDER.length() - 1) == ',') {
			JSON_BUILDER.deleteCharAt(JSON_BUILDER.length() - 1);
		}
		return JSON_BUILDER.toString();
	}

	private static final StringBuilder COMPONENT_BUILDER = new StringBuilder();

	private static String getStyle(char colour) {
		if (COMPONENT_BUILDER.length() > 0) {
			COMPONENT_BUILDER.delete(0, COMPONENT_BUILDER.length());
		}
		switch (colour) {
		case 'k':
			return "\"obfuscated\": true,";
		case 'l':
			return "\"bold\": true,";
		case 'm':
			return "\"strikethrough\": true,";
		case 'n':
			return "\"underlined\": true,";
		case 'o':
			return "\"italic\": true,";
		case 'r':
			return "\"reset\": true,";
		default:
			break;
		}
		ChatColor cc = ChatColor.getByChar(colour);
		if (cc == null)
			return null;
		return COMPONENT_BUILDER.append("\"color\":\"").append(cc.name().toLowerCase()).append("\",")
				.toString();
	}

	private static final Pattern LINK_PATTERN = Pattern.compile("((https?://)?(([\\w-_]+\\.)+([a-zA-Z]{2,4}))((#|/)\\S*)?)(\\s|\\z)");
	private static final int DELETE_IF_EMPTY = ",{\"text\":\"".length();

	/**
	 * Wrap any links found in a more compact JSON version.
	 * 
	 * @author Jikoo
	 * 
	 * @param part the String to check for links
	 * @return the modified String
	 */
	private static String injectLinks(String part, CanonNicks quirk) {
		if (COMPONENT_BUILDER.length() > 0) { // Reusing the style StringBuilder for that sweet sweet microoptimization.
			COMPONENT_BUILDER.delete(0, COMPONENT_BUILDER.length());
		}
		Matcher match = LINK_PATTERN.matcher(part);
		int lastEnd = 0;
		while (match.find()) {
			if (quirk != null) {
				COMPONENT_BUILDER.append(quirk.applyQuirk(part.substring(lastEnd, match.start())));
			} else {
				COMPONENT_BUILDER.append(part.substring(lastEnd, match.start()));
			}
			if (lastEnd == 0) {
				// Initial JSON injection - uses extra to preserve previous colors for any following segments
				COMPONENT_BUILDER.append("\",\"extra\":[");
			} else {
				COMPONENT_BUILDER.append("\"},");
			}
			String url = match.group(1);
			// If URL does not start with http:// or https:// the client will crash. Client autofills this for normal links.
			if (!match.group().matches("https?://.*")) {
				url = "http://" + url;
			}
			// Glorious link wrapping ensues
			COMPONENT_BUILDER.append("{\"text\":\"[").append(match.group(3)).append("]").append(match.group(8))
					.append("\",\"color\":\"blue\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"")
					.append(url).append("\"},\"clickEvent\":{\"action\":\"open_url\",\"value\":\"")
					.append(url).append("\"}}");

			COMPONENT_BUILDER.append(",{\"text\":\"");
			lastEnd = match.end();
		}
		if (lastEnd == 0) {
			return part + '\"';
		}
		String substring = part.substring(lastEnd);
		if (substring.isEmpty()) {
			COMPONENT_BUILDER.delete(COMPONENT_BUILDER.length() - DELETE_IF_EMPTY, COMPONENT_BUILDER.length());
		} else {
			if (quirk != null) {
				COMPONENT_BUILDER.append(quirk.applyQuirk(substring));
			} else {
				COMPONENT_BUILDER.append(substring);
			}
			COMPONENT_BUILDER.append("\"}");
		}
		COMPONENT_BUILDER.append("]");
		return COMPONENT_BUILDER.toString();
	}
}
