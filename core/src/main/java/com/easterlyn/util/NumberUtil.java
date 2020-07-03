package com.easterlyn.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A collection of number-based functions.
 *
 * @author Jikoo
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class NumberUtil {

	private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String ZERO = "0";
	private static final StringBuilder BUILDER = new StringBuilder();
	private static final Map<String, Integer> ROMAN_NUMERALS = new LinkedHashMap<>();
	private static final Pattern TIME_PATTERN = Pattern.compile(
			"$(([0-9]+)y[ears]*)?(([0-9]+)mo[nths]*)?(([0-9]+)w[eks]*)?(([0-9]+)d[ays]*)?(([0-9]+)h[ours]*)?" +
					"(([0-9]+)m[inutes]*)?(([0-9]+)s[econds]*)?^", Pattern.CASE_INSENSITIVE);

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
	 * Interprets a String as a duration of time.
	 *
	 * @param input the duration string, i.e. 10years5day2sec
	 *
	 * @return the numeric interpretation
	 *
	 * @throws NumberFormatException if the String cannot be interpreted as a time
	 */
	public static long parseDuration(String input) throws NumberFormatException {
		Matcher matcher = TIME_PATTERN.matcher(input);
		long time = 0;
		if (matcher.find()) {
			if (matcher.group().isEmpty()) {
				throw new NumberFormatException("Unable to parse duration from input \"" + input + "\"");
			}
			if (matcher.group(2) != null) {
				// Years: 365d * 24h * 60m * 60s * 1000ms
				time = Math.addExact(time, Math.multiplyExact(31536000000L, Long.parseLong(matcher.group(2))));
			}
			if (matcher.group(4) != null) {
				// Months: 30d * 24h * 60m * 60s * 1000ms
				time = Math.addExact(time, Math.multiplyExact(2592000000L, Long.parseLong(matcher.group(4))));
			}
			if (matcher.group(6) != null) {
				// Weeks: 7d * 24h * 60m * 60s * 1000ms
				time = Math.addExact(time, Math.multiplyExact(604800000L, Long.parseLong(matcher.group(6))));
			}
			if (matcher.group(8) != null) {
				// Days: 24h * 60m * 60s * 1000ms
				time = Math.addExact(time, Math.multiplyExact(86400000L, Long.parseLong(matcher.group(8))));
			}
			if (matcher.group(10) != null) {
				// Hours: 60m * 60s * 1000ms
				time = Math.addExact(time, Math.multiplyExact(3600000L, Long.parseLong(matcher.group(10))));
			}
			if (matcher.group(12) != null) {
				// Minutes: 60s * 1000ms
				time = Math.addExact(time, Math.multiplyExact(60000L, Long.parseLong(matcher.group(12))));
			}
			if (matcher.group(14) != null) {
				// Seconds: 1000ms
				time = Math.addExact(time, Math.multiplyExact(1000L, Long.parseLong(matcher.group(14))));
			}
		} else {
			throw new NumberFormatException("Unable to parse duration from input \"" + input + "\"");
		}
		return time;
	}

	public static double addSafe(double x, double y) throws ArithmeticException {
		if (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || y == Double.POSITIVE_INFINITY || y == Double.NEGATIVE_INFINITY) {
			throw new ArithmeticException("double overflow");
		}
		double value = BigDecimal.valueOf(x).add(BigDecimal.valueOf(y)).doubleValue();
		if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
			throw new ArithmeticException("double overflow");
		}
		return value;
	}

	public static double subtractSafe(double x, double y) throws ArithmeticException {
		if (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || y == Double.POSITIVE_INFINITY || y == Double.NEGATIVE_INFINITY) {
			throw new ArithmeticException("double overflow");
		}
		double value = BigDecimal.valueOf(x).subtract(BigDecimal.valueOf(y)).doubleValue();
		if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
			throw new ArithmeticException("double overflow");
		}
		return value;
	}

	public static double multiplySafe(double x, double y) throws ArithmeticException {
		if (x == Double.POSITIVE_INFINITY || x == Double.NEGATIVE_INFINITY || y == Double.POSITIVE_INFINITY || y == Double.NEGATIVE_INFINITY) {
			throw new ArithmeticException("double overflow");
		}
		double value = BigDecimal.valueOf(x).multiply(BigDecimal.valueOf(y)).doubleValue();
		if (value == Double.POSITIVE_INFINITY || value == Double.NEGATIVE_INFINITY) {
			throw new ArithmeticException("double overflow");
		}
		return value;
	}

}
