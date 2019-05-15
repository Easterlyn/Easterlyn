package com.easterlyn.utilities.tuple;

/**
 * Simple object wrapper.
 * 
 * @author Jikoo
 */
public class Wrapper<T> {

	private T object;

	public T get() {
		return object;
	}

	public void set(T object) {
		this.object = object;
	}

}
