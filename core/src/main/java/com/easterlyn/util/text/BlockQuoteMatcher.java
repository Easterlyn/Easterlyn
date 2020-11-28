package com.easterlyn.util.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface BlockQuoteMatcher {

  char getQuoteChar();

  default boolean allowAdditionalParsing() {
    return true;
  }

  @Nullable
  BlockQuote findQuote(@NotNull String message, int start);
}
