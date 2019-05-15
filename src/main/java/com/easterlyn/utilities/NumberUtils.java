package com.easterlyn.utilities;

import com.easterlyn.utilities.tuple.Pair;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Conversions for different bases.
 *
 * @author Jikoo
 */
public class NumberUtils {

	private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String ZERO = "0";
	private static final StringBuilder BUILDER = new StringBuilder();
	private static final Map<String, Integer> ROMAN_NUMERALS = new LinkedHashMap<>();
	private static final Pattern TIME_PATTERN = Pattern.compile(
			"(([0-9]+)\\s*y[ears]*\\s*)?(([0-9]+)\\s*mo[nths]*\\s*)?(([0-9]+)\\s*w[eks]*\\s*)?"
			+ "(([0-9]+)\\s*d[ays]*\\s*)?(([0-9]+)\\s*h[ours]*\\s*)?(([0-9]+)\\s*m[inutes]*\\s*)?"
			+ "(([0-9]+)\\s*s[econds]*\\s*)?", Pattern.CASE_INSENSITIVE);

	static {
		ROMAN_NUMERALS.put("M", 1000);
		ROMAN_NUMERALS.put("CM", 900);
		ROMAN_NUMERALS.put("D", 500);
		ROMAN_NUMERALS.put("CD", 400);
		ROMAN_NUMERALS.put("C", 100);
		ROMAN_NUMERALS.put("XC", 90);
		ROMAN_NUMERALS.put("L", 50);
		ROMAN_NUMERALS.put("XL", 40);
		ROMAN_NUMERALS.put("X", 10);
		ROMAN_NUMERALS.put("IX", 9);
		ROMAN_NUMERALS.put("V", 5);
		ROMAN_NUMERALS.put("IV", 4);
		ROMAN_NUMERALS.put("I", 1);
	}

	/**
	 * Convert a BigInteger into a String in another base.
	 * <p>
	 * Throws an IllegalArgumentException if the BigInteger provided is negative or if the base is
	 * not between 2 and 62 inclusive.
	 *
	 * @param bigInt the BigInteger to convert
	 * @param base the base to convert to
	 * @param minimumDigits the minimum number of characters in the String returned
	 *
	 * @return the BigInteger converted to a String in the correct base
	 */
	public static String getBase(BigInteger bigInt, int base, int minimumDigits) {
		if (bigInt.compareTo(BigInteger.ZERO) < 0) {
			throw new IllegalArgumentException("Cannot convert a negative number!");
		}
		if (base > 62 || base < 2) {
			throw new IllegalArgumentException("Cannot convert to a base that is not > 1 and < 63!");
		}
		if (base == 10) {
			return bigInt.toString();
		}
		BigInteger bigBase = BigInteger.valueOf(base);
		StringBuilder builder = new StringBuilder();
		while (bigInt.compareTo(BigInteger.ZERO) > 0) {
			BigInteger[] division = bigInt.divideAndRemainder(bigBase);
			bigInt = division[0];
			builder.insert(0, CHARS.charAt(division[1].intValue()));
		}
		while (builder.length() < minimumDigits) {
			builder.insert(0, ZERO);
		}
		return builder.length() == 0 ? ZERO : builder.toString();
	}

	/**
	 * Creates a MD5 hash of a String.
	 *
	 * @param string the String to hash
	 * @return the BigInteger of the hash
	 */
	public static BigInteger md5(String string) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return new BigInteger(1, digest.digest(string.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Converts a Roman numeral String into an integer.
	 *
	 * @param roman the Roman numeral String to convert to an integer
	 *
	 * @return the integer represented by the String
	 *
	 * @throws NumberFormatException if the String given contains characters which are not Roman numerals
	 */
	public static int intFromRoman(String roman) throws NumberFormatException {
		char[] chars = roman.toCharArray();
		int value = 0;
		int lastValue = 1000;
		for (int i = 0; i < chars.length; i++) {
			int j = i + 1;
			int currentValue = getRomanCharValue(chars[i]);
			if (currentValue < lastValue && j < chars.length) {
				int nextValue = getRomanCharValue(chars[j]);
				if (nextValue > currentValue) {
					currentValue = nextValue - currentValue;
					i++;
				}
			}
			value += currentValue;
		}
		return value;
	}

	private static int getRomanCharValue(char character) throws NumberFormatException {
		Integer value = ROMAN_NUMERALS.get(String.valueOf(character));
		if (value == null) {
			throw new NumberFormatException(character + " is not a valid character of a roman numeral!");
		}
		return value;
	}

	/**
	 * Converts an integer into a Roman numeral String.
	 *
	 * @param number the integer to convert into Roman numerals
	 *
	 * @return the converted String
	 */
	public static String romanFromInt(int number) {
		synchronized (BUILDER) {
			if (BUILDER.length() > 0) {
				BUILDER.delete(0, BUILDER.length());
			}
			for (Map.Entry<String, Integer> entry : ROMAN_NUMERALS.entrySet()) {
				for (int matches = number / entry.getValue(); matches > 0; matches--) {
					BUILDER.append(entry.getKey());
				}
				number %= entry.getValue();
			}
			return BUILDER.toString();
		}
	}

	/**
	 * Interprets and strips the first area of a String which can be interpreted as a time.
	 *
	 * @param input the input String
	 *
	 * @return a Pair containing the remains of the String and the time interpreted
	 *
	 * @throws NumberFormatException if no part of the String can be interpreted as a time
	 */
	public static Pair<String, Long> parseAndRemoveFirstTime(String input) throws NumberFormatException {
		synchronized (BUILDER) {
			Matcher matcher = TIME_PATTERN.matcher(input);
			long time = 0;
			while (matcher.find()) {
				if (matcher.group().isEmpty()) {
					continue;
				}
				if (matcher.group(2) != null) {
					// Years: 365d * 24h * 60m * 60s * 1000ms
					time += 31536000000L * Long.parseLong(matcher.group(2));
				}
				if (matcher.group(4) != null) {
					// Months: 30d * 24h * 60m * 60s * 1000ms
					time += 2592000000L * Long.parseLong(matcher.group(4));
				}
				if (matcher.group(6) != null) {
					// Weeks: 7d * 24h * 60m * 60s * 1000ms
					time += 604800000L * Long.parseLong(matcher.group(6));
				}
				if (matcher.group(8) != null) {
					// Days: 24h * 60m * 60s * 1000ms
					time += 86400000L * Long.parseLong(matcher.group(8));
				}
				if (matcher.group(10) != null) {
					// Hours: 60m * 60s * 1000ms
					time += 3600000L * Long.parseLong(matcher.group(10));
				}
				if (matcher.group(12) != null) {
					// Minutes: 60s * 1000ms
					time += 60000L * Long.parseLong(matcher.group(12));
				}
				if (matcher.group(14) != null) {
					// Seconds: 1000ms
					time += 1000L * Long.parseLong(matcher.group(14));
				}
			}
			if (BUILDER.length() > 0) {
				BUILDER.delete(0, BUILDER.length());
			}
			BUILDER.append(input, 0, matcher.start()).append(
					input.substring(matcher.end()));
			return new Pair<>(BUILDER.toString(), time);
		}
	}

}
