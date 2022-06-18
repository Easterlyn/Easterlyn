package com.easterlyn.util.text;

import org.jetbrains.annotations.NotNull;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class StaticQuoteConsumer implements QuoteConsumer {

  private final Collection<Pattern> patterns;

  public StaticQuoteConsumer(@NotNull Pattern pattern) {
    this.patterns = Set.of(pattern);
  }

  public StaticQuoteConsumer(@NotNull Pattern @NotNull ... patterns) {
    if (patterns.length == 0) {
      throw new UnsupportedOperationException("Include a pattern in the constructor, you numpty.");
    }
    this.patterns = Set.of(patterns);
  }

  public StaticQuoteConsumer(Collection<Pattern> patterns) {
    this.patterns = patterns;
  }

  @Override
  public Iterable<Pattern> getPatterns() {
    return patterns;
  }

}
