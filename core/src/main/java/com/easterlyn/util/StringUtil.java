package com.easterlyn.util;

import com.easterlyn.util.text.BacktickMatcher;
import com.easterlyn.util.text.BlockQuote;
import com.easterlyn.util.text.BlockQuoteMatcher;
import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.QuoteConsumer;
import com.easterlyn.util.text.QuoteMatcher;
import com.easterlyn.util.text.StaticQuoteConsumer;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of useful string manipulation functions.
 *
 * @author Jikoo
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class StringUtil {

  // TODO move item-related methods to ItemUtil
  public static final Pattern IP_PATTERN = Pattern.compile("([0-9]{1,3}\\.){3}[0-9]{1,3}");
  public static final Pattern URL_PATTERN =
      Pattern.compile("^(([^:/?#]+)://)?(([a-zA-Z0-9-_]+\\.)*[a-zA-Z0-9-]+\\.[a-zA-Z]+)(\\S*)?$");
  public static final Simplifier TO_LOWER_CASE = s -> s.toLowerCase(Locale.ENGLISH);
  public static final Simplifier STRIP_URLS =
      s -> trimExtraWhitespace(URL_PATTERN.matcher(s).replaceAll(" "));
  public static final Simplifier NORMALIZE = s -> Normalizer.normalize(s, Normalizer.Form.NFD);
  private static final Pattern ENUM_NAME_PATTERN = Pattern.compile("(?<=(?:\\A|_)([A-Z]))([A-Z]+)");
  private static final Pattern COMMAND_PATTERN = Pattern.compile("/.{1,}");
  private static final Map<Character, BlockQuoteMatcher> BLOCK_QUOTES = new HashMap<>();
  private static final Set<QuoteConsumer> QUOTE_CONSUMERS = new HashSet<>();

  static {
    BLOCK_QUOTES.put('`', new BacktickMatcher());
    BLOCK_QUOTES.put('"', new QuoteMatcher());

    QUOTE_CONSUMERS.add(
        new StaticQuoteConsumer(URL_PATTERN) {
          @Override
          public @Nullable Supplier<Matcher> handleQuote(String quote) {
            if ("d.va".equalsIgnoreCase(quote)) {
              // Overwatch was shut down for a reason.
              return null;
            }
            Supplier<Matcher> matcherSupplier = super.handleQuote(quote);
            if (matcherSupplier == null) {
              return null;
            }
            Matcher matcher = matcherSupplier.get();
            if (matcher.group(3) == null || matcher.group(3).isEmpty()) {
              // Matches, but main group is somehow empty.
              return null;
            }
            return matcherSupplier;
          }

          @Override
          public void addComponents(
              @NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier) {
            Matcher matcher = matcherSupplier.get();
            String url = matcher.group();
            // Correct missing protocol
            if (matcher.group(1) == null || matcher.group(1).isEmpty()) {
              url = "http://" + url;
            }
            TextComponent component = new TextComponent();
            component.setText('[' + matcher.group(3).toLowerCase(Locale.ENGLISH) + ']');
            component.setColor(Colors.WEB_LINK);
            component.setUnderlined(true);
            TextComponent[] hover = {new TextComponent(url)};
            hover[0].setColor(Colors.WEB_LINK);
            hover[0].setUnderlined(true);
            component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
            component.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
            components.addComponent(component);
          }
        });
    QUOTE_CONSUMERS.add(
        new StaticQuoteConsumer(COMMAND_PATTERN) {
          @Override
          public void addComponents(
              @NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier) {
            String group = matcherSupplier.get().group();
            TextComponent component = new TextComponent(group);
            component.setColor(Colors.COMMAND);
            TextComponent hover = new TextComponent("Click to run!");
            hover.setColor(Colors.COMMAND);
            component.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(new TextComponent[] {hover})));
            component.setClickEvent(
                new ClickEvent(ClickEvent.Action.RUN_COMMAND, ChatColor.stripColor(group.trim())));
            components.addComponent(component);
          }
        });
  }

  public static void addBlockQuoteMatcher(char quoteStart, BlockQuoteMatcher matcher) {
    BLOCK_QUOTES.put(quoteStart, matcher);
  }

  public static void addQuoteConsumer(QuoteConsumer consumer) {
    QUOTE_CONSUMERS.add(consumer);
  }

  public static Collection<TextComponent> toJSON(@Nullable String message) {
    return toJSON(message, Collections.emptyList());
  }

  // TODO appears to not be parsing colors correctly
  public static Collection<TextComponent> toJSON(
      @Nullable String message, Collection<QuoteConsumer> additionalHandlers) {
    if (message == null) {
      return Collections.emptyList();
    }

    ParsedText parsedText = new ParsedText();
    StringBuilder builder = new StringBuilder();
    Collection<QuoteConsumer> consumers =
        Stream.concat(QUOTE_CONSUMERS.stream(), additionalHandlers.stream())
            .collect(Collectors.toSet());

    int maxIndex = message.length() - 1;
    nextChar:
    for (int i = 0; i < message.length(); ++i) {

      char c = message.charAt(i);

      BlockQuoteMatcher matcher = BLOCK_QUOTES.get(c);

      // Cheeky while to prevent excessive nesting
      //noinspection ConstantConditions
      do {
        if (matcher == null) {
          break;
        }

        BlockQuote quote = matcher.findQuote(message, i);
        if (quote == null) {
          break;
        }

        i += quote.getQuoteLength() - 1;

        if (!matcher.allowAdditionalParsing()) {
          if (quote.getQuoteMarks() != null) {
            builder
                .append(quote.getQuoteMarks())
                .append(quote.getQuoteText())
                .append(quote.getQuoteMarks());
          } else {
            builder.append(quote.getQuoteText());
          }
          continue nextChar;
        }

        if (quote.getQuoteMarks() != null) {
          builder.append(quote.getQuoteMarks());
        }

        String quoteText = quote.getQuoteText();

        consumeQuote(parsedText, consumers, builder, quote.getQuoteText());

        if (quote.getQuoteMarks() != null) {
          builder.append(quote.getQuoteMarks());
        }

        continue nextChar;
      } while (false);

      if (c == ' ') {
        builder.append(c);
        continue;
      }

      int nextSpace = message.indexOf(' ', i);
      if (nextSpace == -1) {
        nextSpace = message.length();
      }

      consumeQuote(parsedText, consumers, builder, message.substring(i, nextSpace));
      i = nextSpace - 1;
    }

    // Parse remaining text in builder
    consumeQuote(parsedText, consumers, builder, null);

    // The client will crash if the array is empty
    if (parsedText.getComponents().isEmpty()) {
      parsedText.addComponent(new TextComponent(""));
    }

    return parsedText.getComponents();
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

  public static @NotNull String getConsoleText(TextComponent component) {
    StringBuilder builder = new StringBuilder();
    getConsoleText(component, builder);
    return builder.toString();
  }

  public static @NotNull String getConsoleText(Collection<TextComponent> components) {
    StringBuilder builder = new StringBuilder();
    for (TextComponent component : components) {
      getConsoleText(component, builder);
    }
    return builder.toString();
  }

  private static void getConsoleText(TextComponent component, StringBuilder builder) {

    ClickEvent clickEvent = component.getClickEvent();

    if (clickEvent != null && clickEvent.getAction() == ClickEvent.Action.OPEN_URL) {
      // URLs should show in place of text in console
      builder.append(clickEvent.getValue());
    } else {
      builder.append(component.getText());
    }

    List<BaseComponent> extra = component.getExtra();

    if (extra == null) {
      return;
    }

    for (BaseComponent baseComponent : extra) {
      if (baseComponent instanceof TextComponent) {
        getConsoleText((TextComponent) baseComponent, builder);
      }
    }
  }

  /**
   * Trims additional spaces, including ones surrounding chat colors.
   *
   * @param s the String to trim
   * @return the trimmed String
   */
  public @NotNull static String trimExtraWhitespace(@NotNull String s) {
    // Strips useless codes and any spaces between them.
    // Reset negates all prior colors and formatting.
    s =
        s.replaceAll(
            "(((["
                + ChatColor.COLOR_CHAR
                + "&][0-9a-fk-orA-FK-OR])+)\\s+?)+(["
                + ChatColor.COLOR_CHAR
                + "&][rR])",
            "$4");
    // Strips useless codes and any spaces between them. Colors reset prior colors and formatting.
    s =
        s.replaceAll(
            "(((["
                + ChatColor.COLOR_CHAR
                + "&][0-9a-fk-orA-FK-OR])+)\\s+?)(["
                + ChatColor.COLOR_CHAR
                + "&][0-9a-fA-F])",
            "$4");
    // Strip all spaces between chat colors - actually strips about 1/2 per iteration
    s = s.replaceAll("\\s+(([" + ChatColor.COLOR_CHAR + "&][0-9a-fk-orA-FK-OR])+)\\s+", " $1");
    // Strip all spaces that appear to be at start
    s =
        s.replaceAll(
            "(\\A|\\s+)((([" + ChatColor.COLOR_CHAR + "&][0-9a-fk-orA-FK-OR])+)?\\s+?)", " $3");
    return s.trim();
  }

  /**
   * Checks if a String is nothing but ChatColors and whitespace.
   *
   * @param s the String to check
   * @return true if the String will appear empty to the client
   */
  public static boolean appearsEmpty(@NotNull String s) {
    return s.replaceAll("(\\s|[" + ChatColor.COLOR_CHAR + "&][0-9a-fk-rA-FK-R])", "").isEmpty();
  }

  /**
   * Returns a more user-friendly version of standard Enum names.
   *
   * @param e the Enum to prettify
   * @return the user-friendly version of the name
   */
  public @NotNull static String getFriendlyName(@NotNull Enum<?> e) {
    return getFriendlyName(e.name());
  }

  /**
   * Returns a more user-friendly version of standard Enum names.
   *
   * @param name the name to prettify
   * @return the user-friendly version of the name
   */
  public @NotNull static String getFriendlyName(@NotNull String name) {
    Matcher matcher = ENUM_NAME_PATTERN.matcher(name);
    StringBuilder builder = new StringBuilder();
    while (matcher.find()) {
      if (builder.length() > 0) {
        builder.append(' ');
      }
      builder.append(matcher.group(1)).append(matcher.group(2).toLowerCase());
    }
    return builder.toString();
  }

  public @NotNull static String stripEndPunctuation(@NotNull String word) {
    if (word.length() == 0) {
      return word;
    }
    char character = word.charAt(word.length() - 1);
    if (character < '0'
        || character > '9' && character < 'A'
        || character > 'Z' && character != '_' && character < 'a'
        || character > 'z') {
      return word.substring(0, word.length() - 1);
    }
    return word;
  }

  public @NotNull static String stripNonAlphanumerics(@NotNull String word) {
    StringBuilder sb = new StringBuilder();
    for (char character : word.toCharArray()) {
      if (character < '0'
          || character > '9' && character < 'A'
          || character > 'Z' && character != '_' && character < 'a'
          || character > 'z') {
        continue;
      }
      sb.append(character);
    }
    return sb.toString();
  }

  public static boolean isOnlyAscii(@NotNull String string) {
    // Also no tildes because I can
    for (char character : string.toCharArray()) {
      if (character < ' ' || character > '}') {
        return false;
      }
    }
    return true;
  }

  public @NotNull static String getTrace(@NotNull Throwable throwable) {
    return getTrace(throwable, 50);
  }

  public @NotNull static String getTrace(@NotNull Throwable throwable, int limit) {
    StringBuilder trace = new StringBuilder(throwable.toString());
    StackTraceElement[] elements = throwable.getStackTrace();
    for (int i = 0; i < elements.length && i < limit; i++) {
      trace.append("\n\tat ").append(elements[i].toString());
    }
    if (throwable.getCause() != null) {
      trace.append("\nCaused by: ").append(throwable.getCause().toString());
      for (int i = 0; i < elements.length && i < limit; i++) {
        trace.append("\n\tat ").append(elements[i].toString());
      }
    }
    return trace.toString();
  }

  public @NotNull static String join(@NotNull Object[] args, char separator) {
    return join(args, String.valueOf(separator));
  }

  public @NotNull static String join(@NotNull Object[] args, String separator) {
    return join(args, separator, 0, args.length);
  }

  public @NotNull static String join(
      @NotNull Object[] array, char separator, int startIndex, int endIndex) {
    return join(array, String.valueOf(separator), startIndex, endIndex);
  }

  public @NotNull static String join(
      Object @NotNull [] array, String separator, int startIndex, int endIndex) {
    int totalCount = endIndex - startIndex;
    if (totalCount <= 0) {
      return "";
    }

    StringBuilder builder = new StringBuilder(totalCount * 16);

    for (int i = startIndex; i < endIndex; ++i) {
      if (i > startIndex) {
        builder.append(separator);
      }

      if (array[i] != null) {
        builder.append(array[i]);
      }
    }

    return builder.toString();
  }

  /**
   * Interprets a String as a Boolean value. Returns
   *
   * <pre>null</pre>
   *
   * if no interpretation matches.
   *
   * @param string the String to interpret
   * @return the interpreted value
   */
  public @Nullable static Boolean asBoolean(String string) {
    string = string.toLowerCase();
    switch (string) {
      case "y":
      case "yes":
      case "on":
      case "ok":
      case "t":
      case "true":
      case "add":
      case "1":
        return true;
      case "n":
      case "no":
      case "off":
      case "f":
      case "false":
      case "remove":
      case "del":
      case "delete":
      case "0":
        return false;
      default:
        return null;
    }
  }

  /**
   * Check if a String starts with a prefix, ignoring case.
   *
   * @param string the String to check
   * @param prefix the prefix of the String
   * @return true if the String starts with the prefix
   */
  public static boolean startsWithIgnoreCase(String string, String prefix) {
    if (prefix.length() > string.length()) {
      return false;
    }
    for (int i = 0; i < prefix.length(); ++i) {
      if (Character.toLowerCase(string.charAt(i)) != Character.toLowerCase(prefix.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  /**
   * Jaro-Winkler string comparison implementation.
   *
   * @param a the first String
   * @param b the second String
   * @param simplifiers Simplification functions to apply to the String before comparing
   * @return similarity score where 0 is no match and 1 is 100% match
   */
  public static float compare(@NotNull String a, @NotNull String b, Simplifier... simplifiers) {
    for (Function<String, String> simplifier : simplifiers) {
      a = simplifier.apply(a);
      b = simplifier.apply(b);
    }
    final float jaroScore = compareJaro(a, b);

    if (jaroScore < (float) 0.7) {
      return jaroScore;
    }

    return jaroScore
        + (Math.min(commonPrefix(a, b).length(), 4) * (float) 0.1 * (1.0f - jaroScore));
  }

  private static float compareJaro(@NotNull String a, @NotNull String b) {
    if (a.isEmpty() && b.isEmpty()) {
      return 1.0f;
    }

    if (a.isEmpty() || b.isEmpty()) {
      return 0.0f;
    }

    final int[] charsA = a.codePoints().toArray();
    final int[] charsB = b.codePoints().toArray();

    // Intentional integer division to round down.
    final int halfLength = Math.max(0, Math.max(charsA.length, charsB.length) / 2 - 1);

    final int[] commonA = getCommonCodePoints(charsA, charsB, halfLength);
    final int[] commonB = getCommonCodePoints(charsB, charsA, halfLength);

    // commonA and commonB will always contain the same multi-set of
    // characters. Because getCommonCharacters has been optimized, commonA
    // and commonB are -1-padded. So in this loop we count transposition
    // and use commonCharacters to determine the length of the multi-set.
    float transpositions = 0;
    int commonCharacters = 0;
    for (int length = commonA.length;
        commonCharacters < length && commonA[commonCharacters] > -1;
        commonCharacters++) {
      if (commonA[commonCharacters] != commonB[commonCharacters]) {
        transpositions++;
      }
    }

    if (commonCharacters == 0) {
      return 0.0f;
    }

    float aCommonRatio = commonCharacters / (float) charsA.length;
    float bCommonRatio = commonCharacters / (float) charsB.length;
    float transpositionRatio = (commonCharacters - transpositions / 2.0f) / commonCharacters;

    return (aCommonRatio + bCommonRatio + transpositionRatio) / 3.0f;
  }

  /*
   * Returns an array of code points from a within b. A character in b is
   * counted as common when it is within separation distance from the position
   * in a.
   */
  private static int[] getCommonCodePoints(
      final int[] charsA, final int[] charsB, final int separation) {
    final int[] common = new int[Math.min(charsA.length, charsB.length)];
    final boolean[] matched = new boolean[charsB.length];

    // Iterate of string a and find all characters that occur in b within
    // the separation distance. Mark any matches found to avoid
    // duplicate matchings.
    int commonIndex = 0;
    for (int i = 0, length = charsA.length; i < length; i++) {
      final int character = charsA[i];
      final int index = indexOf(character, charsB, i - separation, i + separation + 1, matched);
      if (index > -1) {
        common[commonIndex++] = character;
        matched[index] = true;
      }
    }

    if (commonIndex < common.length) {
      common[commonIndex] = -1;
    }

    // Both invocations will yield the same multi-set terminated by -1, so
    // they can be compared for transposition without making a copy.
    return common;
  }

  /*
   * Search for code point in buffer starting at fromIndex to toIndex - 1.
   *
   * Returns -1 when not found.
   */
  private static int indexOf(
      int character, int[] buffer, int fromIndex, int toIndex, boolean[] matched) {

    // compare char with range of characters to either side
    for (int j = Math.max(0, fromIndex), length = Math.min(toIndex, buffer.length);
        j < length;
        j++) {
      // check if found
      if (buffer[j] == character && !matched[j]) {
        return j;
      }
    }

    return -1;
  }

  private @NotNull static String commonPrefix(@NotNull CharSequence a, @NotNull CharSequence b) {
    int maxPrefixLength = Math.min(a.length(), b.length());

    int p;

    p = 0;
    while (p < maxPrefixLength && a.charAt(p) == b.charAt(p)) {
      ++p;
    }

    if (validSurrogatePairAt(a, p - 1) || validSurrogatePairAt(b, p - 1)) {
      --p;
    }

    return a.subSequence(0, p).toString();
  }

  private static boolean validSurrogatePairAt(@NotNull CharSequence string, int index) {
    return index >= 0
        && index <= string.length() - 2
        && Character.isHighSurrogate(string.charAt(index))
        && Character.isLowSurrogate(string.charAt(index + 1));
  }

  public interface Simplifier extends Function<String, String> {}
}
