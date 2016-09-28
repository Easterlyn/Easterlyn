package co.sblock.utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.md_5.bungee.api.ChatColor;

/**
 * A collection of useful String-altering functions. Not named StringUtils for ease due to our use
 * of the Apache class by the same name.
 * 
 * @author Jikoo
 */
public class TextUtils {

	public static final Pattern IP_PATTERN = Pattern.compile("([0-9]{1,3}\\.){3}[0-9]{1,3}");
	public static final Pattern URL_PATTERN = Pattern.compile("^(https?://)?(([\\w-_]+\\.)+([a-z]{2,4}))((#|/)\\S*)?$", Pattern.CASE_INSENSITIVE);
	private static final Pattern ENUM_NAME_PATTERN = Pattern.compile("(?<=(?:\\A|_)([A-Z]))([A-Z]+)");

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
	 * @param e the Enum to prettify
	 * 
	 * @return the user-friendly version of the name
	 */
	public static String getFriendlyName(Enum<?> e) {
		return getFriendlyName(e.name());
	}

	/**
	 * Returns a more user-friendly version of standard Enum names.
	 * 
	 * @param name the name to prettify
	 * 
	 * @return the user-friendly version of the name
	 */
	public static String getFriendlyName(String name) {
		Matcher matcher = ENUM_NAME_PATTERN.matcher(name);
		StringBuilder builder = new StringBuilder();
		while (matcher.find()) {
			if (builder.length() > 0) {
				builder.append(' ');
			}
			builder.append(matcher.group(1)).append(matcher.group(2).toLowerCase());
		}
		return builder.toString();
	}

	public static String stripEndPunctuation(String word) {
		if (word.length() == 0) {
			return word;
		}
		char character = word.charAt(word.length() - 1);
		if (character < '0' || character > '9' && character < 'A' || character > 'Z'
				&& character != '_' && character < 'a' || character > 'z') {
			return word.substring(0, word.length() - 1);
		}
		return word;
	}

	public static String stripNonAlphanumerics(String word) {
		StringBuilder sb = new StringBuilder();
		for (char character : word.toCharArray()) {
			if (character < '0' || character > '9' && character < 'A' || character > 'Z'
					&& character != '_' && character < 'a' || character > 'z') {
				continue;
			}
			sb.append(character);
		}
		return sb.toString();
	}

	public static boolean isOnlyAscii(String string) {
		// Also no tildes because I can
		for (char character : string.toCharArray()) {
			if (character < ' ' || character > '}') {
				return false;
			}
		}
		return true;
	}

	public static String getTrace(Throwable throwable) {
		return getTrace(throwable, 50);
	}

	public static String getTrace(Throwable throwable, int limit) {
		StringBuilder trace = new StringBuilder(throwable.toString());
		StackTraceElement[] elements = throwable.getStackTrace();
		for (int i = 0; i < elements.length && i < limit; i++) {
			trace.append("\n\tat ").append(elements[i].toString());
		}
		if (throwable.getCause() != null) {
			trace.append("\nCaused by: ").append(throwable.getCause().toString());
			for (int i = 0; i < elements.length && i < limit; i++) {
				trace.append("\n\tat ").append(elements[i].toString());
			}
		}
		return trace.toString();
	}
}
