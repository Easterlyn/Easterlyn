package co.sblock.Sblock.Utilities.Regex;

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
	 * @return the regular expression created
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
}
