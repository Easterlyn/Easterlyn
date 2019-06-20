package com.easterlyn.util.tuple;

import org.jetbrains.annotations.Nullable;

/**
 * Tuple for one object.
 * 
 * @author Jikoo
 */
public class Single<T> {

	@Nullable
	private T object;

	@Nullable
	public T get() {
		return object;
	}

	public void set(@Nullable T object) {
		this.object = object;
	}

}
