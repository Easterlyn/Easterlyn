package co.sblock.utilities;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Conversions for different bases.
 * 
 * @author Jikoo
 */
public class Base {

	private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String ZERO = "0";

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
		if (bigInt.compareTo(BigInteger.ZERO) == -1) {
			throw new IllegalArgumentException("Cannot convert a negative number!");
		}
		if (base > 62 || base < 2) {
			throw new IllegalArgumentException("Cannot convert to a base that is not > 1 and < 62!");
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

	public static BigInteger md5(String string) {
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			return new BigInteger(1, digest.digest(string.getBytes("UTF-8")));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}
}
