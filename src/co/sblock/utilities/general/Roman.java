package co.sblock.utilities.general;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Roman numeral conversion utility.
 * 
 * @author Jikoo
 */
public class Roman {

	private static final StringBuilder NUMERAL_BUILDER = new StringBuilder();
	private static final LinkedHashMap<String, Integer> ROMAN_NUMERALS = new LinkedHashMap<>();
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

	public static int fromString(String string) throws NumberFormatException {
		char[] chars = string.toCharArray();
		int value = 0;
		int lastValue = 1000;
		for (int i = 0; i < chars.length; i++) {
			int j = i + 1;
			int currentValue = getCharValue(chars[i]);
			if (currentValue < lastValue && j < chars.length) {
				int nextValue = getCharValue(chars[j]);
				if (nextValue > currentValue) {
					currentValue = nextValue - currentValue;
					i++;
				}
			}
			value += currentValue;
		}
		return value;
	}

	private static int getCharValue(char character) throws NumberFormatException {
		Integer value = ROMAN_NUMERALS.get(String.valueOf(character));
		if (value == null) {
			throw new NumberFormatException(character + " is not a valid character of a roman numeral!");
		}
		return value;
	}

	public static String fromInt(int number) {
		if (NUMERAL_BUILDER.length() > 0) {
			NUMERAL_BUILDER.delete(0, NUMERAL_BUILDER.length());
		}
		for (Map.Entry<String, Integer> entry : ROMAN_NUMERALS.entrySet()) {
			for (int matches = number / entry.getValue(); matches > 0; matches--) {
				NUMERAL_BUILDER.append(entry.getKey());
			}
			number %=entry.getValue();
		}
		return NUMERAL_BUILDER.toString();
	}
}
