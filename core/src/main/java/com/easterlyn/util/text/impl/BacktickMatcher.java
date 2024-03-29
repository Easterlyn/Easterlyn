package com.easterlyn.util.text.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.easterlyn.util.text.BlockQuote;
import com.easterlyn.util.text.BlockQuoteMatcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A BlockQuoteMatcher implementation for creating code blocks.
 *
 * <p>For simplicity's sake (and because Discord already can't handle it properly), backticks must
 * match exactly in number for start and end - this means that ``` ```` ``` will quote "````"
 * instead of requiring either 2 opening backticks or 5+ backticks quoting. For that reason, this
 * unit test will not cover such outliers.
 *
 * <p>N.B. quoted text is trimmed.
 */
public class BacktickMatcher implements BlockQuoteMatcher {

  /**
   * ＨＩＧＨＬＹ ＤＥＳＣＲＩＰＴＩＶＥ ＭＥＴＨＯＤ ＮＡＭＥ. Converts ascii characters in a String to full-width characters.
   *
   * @param text the String to convert
   * @return the new String created
   */
  @SuppressWarnings({"NonAsciiCharacters", "checkstyle:MethodName"})
  static @NotNull String ｖａｐｏｒｗａｖｅ(@NotNull String text) {
    StringBuilder newText = new StringBuilder(text.length());

    for (int i = 0; i < text.length(); ++i) {
      char character = text.charAt(i);
      if (character == ' ') {
        // Full width space is \u3000, but MC does not support it. 2 spaces.
        newText.append("  ");
      } else if (character >= '!' && character <= '~') {
        newText.append((char) (text.charAt(i) - 0x20 + 0xff00));
      } else {
        newText.append(text.charAt(i));
      }
    }

    return newText.toString();
  }

  @Override
  public char getQuoteChar() {
    return '`';
  }

  @Override
  public @Nullable BlockQuote findQuote(@NotNull String message, int start) {
    int backticks = 1;
    while (start < message.length() - 1 && message.charAt(++start) == '`') {
      ++backticks;
    }

    int endBacktick;
    if (backticks == 1) {
      // 1 backtick forces next backtick to end block no matter what
      endBacktick = message.indexOf('`', start) + 1;
    } else {
      // TODO should we cache patterns? Seems unlikely to be a commonly hit thing
      Pattern backtickEnd = Pattern.compile("(?<!`)`{" + backticks + "}(?!`)");
      Matcher matcher = backtickEnd.matcher(message);
      if (matcher.find(start)) {
        endBacktick = matcher.end();
      } else {
        endBacktick = -1;
      }
    }

    int finalBackticks = backticks;
    int finalStart = start;

    // Is block closed? If not, block quote starting backticks.
    if (endBacktick <= start) {
      String backtickString = message.substring(start - backticks, start);
      return new BlockQuote() {
        @Override
        public @Nullable String getQuoteMarks() {
          return null;
        }

        @Override
        public @NotNull String getQuoteText() {
          return backtickString;
        }

        @Override
        public int getQuoteLength() {
          return finalBackticks;
        }
      };
    }

    String codeBlock = ｖａｐｏｒｗａｖｅ(message.substring(start, endBacktick - backticks).trim());
    return new BlockQuote() {
      @Override
      public @Nullable String getQuoteMarks() {
        return null;
      }

      @Override
      public @NotNull String getQuoteText() {
        return codeBlock;
      }

      @Override
      public int getQuoteLength() {
        return endBacktick + finalBackticks - finalStart;
      }

      @Override
      public boolean allowAdditionalParsing() {
        return false;
      }
    };
  }
}
