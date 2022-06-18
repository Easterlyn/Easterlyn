package com.easterlyn.util.text;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.util.text.impl.BacktickMatcher;
import com.easterlyn.util.text.impl.CommandQuoteConsumer;
import com.easterlyn.util.text.impl.QuoteMatcher;
import com.easterlyn.util.text.impl.UrlQuoteConsumer;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class TextParsing {

  private static final Map<Character, BlockQuoteMatcher> BLOCK_QUOTES = new HashMap<>();
  private static final Set<QuoteConsumer> QUOTE_CONSUMERS = new HashSet<>();

  static {
    BLOCK_QUOTES.put('`', new BacktickMatcher());
    BLOCK_QUOTES.put('"', new QuoteMatcher());

    QUOTE_CONSUMERS.add(new UrlQuoteConsumer());
    QUOTE_CONSUMERS.add(new CommandQuoteConsumer());
  }

  public static void addBlockQuoteMatcher(char quoteStart, BlockQuoteMatcher matcher) {
    BLOCK_QUOTES.put(quoteStart, matcher);
  }

  public static void addQuoteConsumer(QuoteConsumer consumer) {
    QUOTE_CONSUMERS.add(consumer);
  }

  public static @NotNull Collection<TextComponent> toJSON(@Nullable String text) {
    return toJSON(text, Set.of());
  }

  // TODO parse colors
  public static @NotNull Collection<TextComponent> toJSON(
      @Nullable String text,
      @NotNull Collection<QuoteConsumer> additionalHandlers) {
    if (text == null) {
      return Set.of();
    }

    ParseState state = new ParseState(text, additionalHandlers);

    for (state.index = 0; state.index < text.length(); ++state.index) {
      state.currentChar = text.charAt(state.index);
      if (!handleQuotedText(state)) {
        handleUnquotedText(state);
      }
    }

    return state.parsedText.getComponents();
  }

  private static class ParseState {

    private final ParsedText parsedText = new ParsedText();
    private final StringBuilder builder = new StringBuilder();
    private final Collection<QuoteConsumer> consumers = new HashSet<>(QUOTE_CONSUMERS);
    private final String text;
    private int index;
    private char currentChar;

    private ParseState(
        @NotNull String text,
        @NotNull Collection<QuoteConsumer> additionalHandlers) {
      this.text = text;
      this.consumers.addAll(additionalHandlers);
    }

    private void setIndex(int newIndex) {
      if (newIndex < index) {
        ReportableEvent.call("Index may not be reduced!", 5);
        return;
      }

      index = newIndex;
    }

  }

  private static boolean handleQuotedText(@NotNull ParseState state) {
    BlockQuoteMatcher matcher = BLOCK_QUOTES.get(state.currentChar);

    if (matcher == null) {
      return false;
    }

    BlockQuote quote = matcher.findQuote(state.text, state.index);
    if (quote == null) {
      return false;
    }

    if (quote.getQuoteMarks() != null) {
      state.builder.append(quote.getQuoteMarks());
    }

    if (matcher.allowAdditionalParsing()) {
      consumeQuote(state.parsedText, state.consumers, state.builder, quote.getQuoteText());
    } else {
      state.builder.append(quote.getQuoteText());
    }

    if (quote.getQuoteMarks() != null) {
      state.builder.append(quote.getQuoteMarks());
    }

    state.setIndex(state.index + quote.getQuoteLength() - 1);

    return true;
  }

  private static void handleUnquotedText(@NotNull ParseState state) {
    if (state.currentChar == ' ') {
      state.builder.append(state.currentChar);
      return;
    }

    int nextSpace = state.text.indexOf(' ', state.index);
    if (nextSpace == -1) {
      nextSpace = state.text.length();
    }

    consumeQuote(
        state.parsedText,
        state.consumers,
        state.builder,
        state.text.substring(state.index, nextSpace));
    state.setIndex(nextSpace - 1);
  }

  private static void consumeQuote(
      @NotNull ParsedText parsedText,
      @NotNull Collection<QuoteConsumer> consumers,
      @NotNull StringBuilder builder,
      @Nullable String quote) {

    if (quote == null) {
      if (builder.length() == 0) {
        return;
      }
      parsedText.addText(builder.toString());
      if (builder.length() > 0) {
        builder.delete(0, builder.length());
      }
      return;
    }

    for (QuoteConsumer consumer : consumers) {
      Supplier<Matcher> matcher = consumer.handleQuote(quote);
      if (matcher == null) {
        continue;
      }
      if (builder.length() > 0) {
        parsedText.addText(builder.toString());
        builder.delete(0, builder.length());
      }
      consumer.addComponents(parsedText, matcher);
      return;
    }

    builder.append(quote);
  }

  private TextParsing() {
    throw new IllegalStateException("Cannot instantiate utility class!");
  }

}
