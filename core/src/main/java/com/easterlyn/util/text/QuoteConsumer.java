package com.easterlyn.util.text;

import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface QuoteConsumer {

	Iterable<Pattern> getPatterns();

	default @Nullable Supplier<Matcher> handleQuote(String quote) {
		for (Pattern pattern : getPatterns()) {
			Matcher matcher = pattern.matcher(quote);
			if (matcher.matches()) {
				return () -> matcher;
			}
		}
		return null;
	}

	void addComponents(@NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier);

}
