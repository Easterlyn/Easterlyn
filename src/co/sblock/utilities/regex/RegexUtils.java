package co.sblock.utilities.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;

/**
 * A collection of useful regex functions.
 * 
 * @author Jikoo
 */
public class RegexUtils {

	/**
	 * Creates a regular expression that will ignore case when matching the specified Strings.
	 * 
	 * @param s the Strings
	 * 
	 * @return the regular expression create
	 */
	public static String ignoreCaseRegex(String... s) {
		if (s.length == 0) {
			return new String();
		}
		StringBuilder regex = new StringBuilder();
		if (s.length > 1) {
			regex.append('(');
		}
		for (int i = 0; i < s.length; i++) {
			regex.append('(');
			for (int j = 0; j < s[i].length(); j++) {
				regex.append('[');
				char ch = s[i].charAt(j);
				if (Character.isLetter(ch)) {
					regex.append(Character.toUpperCase(ch)).append(Character.toLowerCase(ch));
				} else {
					regex.append(ch);
				}
				regex.append(']');
			}
			regex.append(')').append('|');
		}
		regex.deleteCharAt(regex.length() - 1);
		if (s.length > 1) {
			regex.append(')');
		}
		return regex.toString();
	}

	/**
	 * Trims additional spaces, including ones surrounding chat colors.
	 * 
	 * @param s the String to trim
	 * 
	 * @return the trimmed String
	 */
	public static String trimExtraWhitespace(String s) {
		// Strip all spaces between colors
		Pattern p = Pattern.compile("(((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-rA-FK-R])+)\\s+(((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-rA-FK-R])+)");
		boolean complete = false;
		while (!complete) {
			Matcher m = p.matcher(s);
			int lastMatch = 0;
			StringBuilder newMessage = new StringBuilder();
			while (m.find()) {
				newMessage.append(s.substring(lastMatch, m.start())).append(m.group(3)).append(m.group(4));
				lastMatch = m.end();
			}
			if (lastMatch > 0) {
				s = newMessage.append(s.substring(lastMatch)).toString();
				continue;
			}
			complete = true;
		}
		// Strip all useless colors
		//s = s.replaceAll("(((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-rA-FK-R])+)((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-rA-F])", "$4");
		
		return s.replaceAll("(\\A|\\s)+((((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-rA-FK-R])+)?\\s+?)", " $3");
	}

	/**
	 * Checks if a String is nothing but ChatColors and whitespace.
	 * 
	 * @param s the String to check
	 * 
	 * @return true if the String will appear empty to the client
	 */
	public static boolean appearsEmpty(String s) {
		return s.replaceAll("(\\s|(" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-rA-FK-R])", "").isEmpty();
	}

	/**
	 * Returns a more user-friendly version of standard Enum names.
	 * 
	 * @param name the name to prettify
	 * 
	 * @return the user-friendly version of the name
	 */
	public static String getFriendlyName(String name) {
		StringBuilder sb = new StringBuilder();
		name = name.toLowerCase();
		Matcher m = Pattern.compile("(\\A|_)[a-z]").matcher(name);
		int end = 0;
		while (m.find()) {
			sb.append(name.substring(end, m.start()));
			sb.append(m.group().toUpperCase().replace("_", " "));
			end = m.end();
		}
		sb.append(name.substring(end));
		return sb.toString();
	}

	public static String getFriendlyName(LivingEntity e) {
		StringBuilder sb = new StringBuilder();
		if (e.getType() == EntityType.SKELETON && ((Skeleton) e).getSkeletonType() == SkeletonType.WITHER) {
			sb.append("Wither ");
		}
		return sb.append(getFriendlyName(e.getType().name().toLowerCase())).toString();
		
	}

	public static String getFriendlyName(Material m) {
		return getFriendlyName(m.name().toLowerCase());
	}
}
