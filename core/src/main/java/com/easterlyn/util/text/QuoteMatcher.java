package com.easterlyn.util.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QuoteMatcher implements BlockQuoteMatcher {

	@Override
	public char getQuoteChar() {
		return '"';
	}

	@Override
	public @Nullable BlockQuote findQuote(@NotNull String message, int start) {
		if (start != 0 && message.charAt(start - 1) == '\\') {
			// Opening quote is escaped
			return null;
		}

		int endIndex = start;

		while ((endIndex = message.indexOf('"', endIndex + 1)) != -1) {
			if (message.charAt(endIndex - 1) != '\\') {
				// TODO does not support escaping backslash, i.e. "\\" -> "\"
				break;
			}
		}

		if (endIndex == -1) {
			return null;
		}

		String quote = message.substring(start + 1, endIndex).replace("\\\"", "\"");
		int length = endIndex + 2 - start;

		return new BlockQuote() {
			@Override
			public @NotNull String getQuoteMarks() {
				return "\"";
			}

			@Override
			public @NotNull String getQuoteText() {
				return quote;
			}

			@Override
			public int getQuoteLength() {
				return length;
			}
		};

	}

}
