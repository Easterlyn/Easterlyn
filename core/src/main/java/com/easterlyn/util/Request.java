package com.easterlyn.util;

public abstract class Request {

	private final long expiry;

	public Request() {
		this.expiry = System.currentTimeMillis() + 60000L;
	}

	public abstract void accept();

	public abstract void decline();

	public long getExpiry() {
		return expiry;
	}

}
