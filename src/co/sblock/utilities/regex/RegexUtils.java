package co.sblock.utilities.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
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
		// Strips useless codes and any spaces between them. Reset negates all prior colors and formatting.
		s = s.replaceAll("((((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-orA-FK-OR])+)\\s+?)+((\\" + ChatColor.COLOR_CHAR + "|&)[rR])", "$5");
		// Strips useless codes and any spaces between them. Colors reset prior colors and formatting.
		s = s.replaceAll("((((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-orA-FK-OR])+)\\s+?)((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fA-F])", "$5");
		// Strip all spaces between chat colors - actually strips about 1/2 per iteration
		s = s.replaceAll("\\s+(((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-orA-FK-OR])+)\\s+", " $1");
		// Strip all spaces that appear to be at start
		s = s.replaceAll("(\\A|\\s+)((((\\" + ChatColor.COLOR_CHAR + "|&)[0-9a-fk-orA-FK-OR])+)?\\s+?)", " $3");
		return s.trim();
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
}
