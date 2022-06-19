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

  public static @NotNull Collection<TextComponent> toJSON(
      @Nullable String text,
      @NotNull Collection<QuoteConsumer> additionalHandlers) {
    if (text == null) {
      return Set.of();
    }

    return new TextParsing(additionalHandlers, text).toComponents();
  }

  private final @NotNull ParsedText parsedText = new ParsedText();
  private final @NotNull StringBuilder builder = new StringBuilder();
  private final @NotNull Collection<QuoteConsumer> consumers = new HashSet<>(QUOTE_CONSUMERS);
  private final @NotNull String text;
  private int index;
  private char currentChar;

  private TextParsing(
      @NotNull Collection<QuoteConsumer> additionalHandlers,
      @NotNull String text) {
    this.consumers.addAll(additionalHandlers);
    this.text = text;
  }

  private @NotNull Collection<TextComponent> toComponents() {
    for (index = 0; index < text.length(); ++index) {
      currentChar = text.charAt(index);
      if (!handleQuotedText()) {
        handleUnquotedText();
      }
    }

    flush();
    return parsedText.getComponents();
  }

  private void setIndex(int newIndex) {
    if (newIndex < index) {
      ReportableEvent.call("Index may not be reduced!", 5);
      return;
    }

    index = newIndex;
  }

  private boolean handleQuotedText() {
    BlockQuoteMatcher matcher = BLOCK_QUOTES.get(currentChar);

    if (matcher == null) {
      return false;
    }

    BlockQuote quote = matcher.findQuote(text, index);
    if (quote == null) {
      return false;
    }

    if (quote.getQuoteMarks() != null) {
      builder.append(quote.getQuoteMarks());
    }

    if (matcher.allowAdditionalParsing()) {
      consumeQuote(quote.getQuoteText());
    } else {
      builder.append(quote.getQuoteText());
    }

    if (quote.getQuoteMarks() != null) {
      builder.append(quote.getQuoteMarks());
    }

    setIndex(index + quote.getQuoteLength() - 1);

    return true;
  }

  private void handleUnquotedText() {
    if (currentChar == ' ') {
      builder.append(' ');
      return;
    }

    int nextSpace = text.indexOf(' ', index);
    if (nextSpace == -1) {
      nextSpace = text.length();
    }

    consumeQuote(text.substring(index, nextSpace));
    setIndex(nextSpace - 1);
  }

  private void consumeQuote(@NotNull String quote) {
    for (QuoteConsumer consumer : consumers) {
      Supplier<Matcher> matcher = consumer.handleQuote(quote);
      if (matcher == null) {
        continue;
      }
      flush();
      consumer.addComponents(parsedText, matcher);
      return;
    }

    builder.append(quote);
  }

  private void flush() {
    if (builder.length() > 0) {
      parsedText.addText(builder.toString());
      builder.delete(0, builder.length());
    }
  }

}
