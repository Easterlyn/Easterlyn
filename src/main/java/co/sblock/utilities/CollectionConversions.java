package co.sblock.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A couple of useful Stream-based functions for converting a Collection into a Collection of
 * something else.
 * 
 * @author Jikoo
 */
public class CollectionConversions {

	public static <T, R> List<R> toList(Collection<T> from, Function<T, R> function) {
		return from.stream().map(function).collect(Collectors.toCollection(ArrayList<R>::new));
	}

	public static <T, R> Set<R> toSet(Collection<T> from, Function<T, R> function) {
		return from.stream().map(function).collect(Collectors.toCollection(HashSet<R>::new));
	}

}
