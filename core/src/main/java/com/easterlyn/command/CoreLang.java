package com.easterlyn.command;

import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;
import java.util.Locale;

public enum CoreLang implements MessageKeyProvider {
	NO_CONSOLE,
	ONLY_CONSOLE,
	NO_PERMISSION,
	ERROR_LOGGED,
	VALUES,
	NUMBER,
	NUMBER_WITHIN,
	WHOLE_NUMBER,
	WHOLE_NUMBER_WITHIN,
	INVALID_PLAYER,
	SUCCESS;

	private final MessageKey key;

	CoreLang() {
		this.key = MessageKey.of("core.common." + this.name().toLowerCase(Locale.ENGLISH));
	}

	@Override
	public MessageKey getMessageKey() {
		return key;
	}

}
