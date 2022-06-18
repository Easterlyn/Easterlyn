package com.easterlyn.util.text.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.easterlyn.util.text.BlockQuote;
import com.easterlyn.util.text.BlockQuoteMatcher;
import org.junit.Test;

public class BlockQuoteTest {

  @Test
  public void testBacktickQuoting() {
    BlockQuoteMatcher backtickMatcher = new BacktickMatcher();
    String message;
    BlockQuote quote;

    // Single backtick greedily finds first backtick, backticks not allowed in quote.
    message = "test `single backtick``";
    quote = backtickMatcher.findQuote(message, message.indexOf('`'));
    assertNotNull(quote);
    assertNull(quote.getQuoteMarks());
    assertEquals(BacktickMatcher.ｖａｐｏｒｗａｖｅ("single backtick"), quote.getQuoteText());
    assertFalse(quote.allowAdditionalParsing());

    // A series of unpaired backticks should return a quote so we don't quote them again later.
    message = "test ` unpaired backtick";
    quote = backtickMatcher.findQuote(message, message.indexOf('`'));
    assertNotNull(quote);
    assertNull(quote.getQuoteMarks());
    assertEquals("`", quote.getQuoteText());
    assertTrue(quote.allowAdditionalParsing());
    message = "test ``` unpaired backticks";
    quote = backtickMatcher.findQuote(message, message.indexOf('`'));
    assertNotNull(quote);
    assertNull(quote.getQuoteMarks());
    assertEquals("```", quote.getQuoteText());
    assertTrue(quote.allowAdditionalParsing());

    // Double backtick escapes multiple backticks and requires double backticks to close
    message = "test ``double ` backticks``` ```` ``";
    quote = backtickMatcher.findQuote(message, message.indexOf('`'));
    assertNotNull(quote);
    assertNull(quote.getQuoteMarks());
    assertEquals(BacktickMatcher.ｖａｐｏｒｗａｖｅ("double ` backticks``` ````"), quote.getQuoteText());
    assertFalse(quote.allowAdditionalParsing());

    // Multiple backticks require equal numbers to close and ignore lesser numbers of backticks.
    message = "``test ` multiple `` backticks``";
    for (int i = 0; i < 4; ++i) {
      message = '`' + message + '`';
      quote = backtickMatcher.findQuote(message, message.indexOf('`'));
      assertNotNull(quote);
      assertNull(quote.getQuoteMarks());
      assertEquals(BacktickMatcher.ｖａｐｏｒｗａｖｅ("test ` multiple `` backticks"), quote.getQuoteText());
      assertFalse(quote.allowAdditionalParsing());
    }
  }

  @Test
  public void testQuoteQuoting() {
    // TODO: multiple backslashes are not handled properly, i.e. 'test \\\\'
    BlockQuoteMatcher backtickMatcher = new QuoteMatcher();
    String message;
    BlockQuote quote;

    // Normal quoting
    message = "test \"one quote\"";
    quote = backtickMatcher.findQuote(message, message.indexOf('"'));
    assertNotNull(quote);
    assertEquals("\"", quote.getQuoteMarks());
    assertEquals("one quote", quote.getQuoteText());
    assertTrue(quote.allowAdditionalParsing());

    // Escaped opening quote
    message = "test \\\"escaped open\"";
    quote = backtickMatcher.findQuote(message, message.indexOf('"'));
    assertNull(quote);

    // Escaped closing quote
    message = "test \"escaped close\\\"";
    quote = backtickMatcher.findQuote(message, message.indexOf('"'));
    assertNull(quote);

    // No closing quote
    message = "test \"no close";
    quote = backtickMatcher.findQuote(message, message.indexOf('"'));
    assertNull(quote);

    // Escaped quote
    message = "test \"escaped \\\" quote\"";
    quote = backtickMatcher.findQuote(message, message.indexOf('"'));
    assertNotNull(quote);
    assertEquals("\"", quote.getQuoteMarks());
    assertEquals("escaped \" quote", quote.getQuoteText());
    assertTrue(quote.allowAdditionalParsing());
  }
}
