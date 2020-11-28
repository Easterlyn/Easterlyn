package com.easterlyn.util.text;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Representation of a block quote for string to TextComponent parsing. */
public interface BlockQuote {

  @Nullable
  String getQuoteMarks();

  @NotNull
  String getQuoteText();

  int getQuoteLength();

  default boolean allowAdditionalParsing() {
    return true;
  }
}
