package co.sblock.utilities;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A collection of conversions, because apparently everything is terrible.
 * 
 * @author Jikoo
 */
public class Base {

	private static final String BASE_62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final BigInteger BASE_62 = BigInteger.valueOf(BASE_62_CHARS.length());
	private static final String ZERO = "0";

	public static String getBase62(BigInteger bigInt, int minimumDigits) {
		if (bigInt.compareTo(BigInteger.ZERO) == -1) {
			throw new IllegalArgumentException("Cannot convert a negative number into Base62!");
		}
		StringBuilder builder = new StringBuilder();
		while (bigInt.compareTo(BigInteger.ZERO) > 0) {
			BigInteger[] division = bigInt.divideAndRemainder(BASE_62);
			bigInt = division[0];
			builder.insert(0, BASE_62_CHARS.charAt(division[1].intValue()));
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
