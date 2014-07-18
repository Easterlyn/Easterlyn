package co.sblock.utilities.threadsafe;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author ted
 *
 * simple class for generating a set
 */
public class SetGenerator {
	
	/**
	 * 
	 * @return
	 */
	public static <T> Set<T> generate() {
		return Collections.newSetFromMap(new ConcurrentHashMap<T, Boolean>());
	}
}
