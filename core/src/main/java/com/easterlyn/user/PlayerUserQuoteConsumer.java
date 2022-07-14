package com.easterlyn.user;

import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.QuoteConsumer;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PlayerUserQuoteConsumer implements QuoteConsumer {

  private final Supplier<Collection<PlayerUser>> userList;

  PlayerUserQuoteConsumer(Supplier<Collection<PlayerUser>> userList) {
    this.userList = userList;
  }

  @Override
  public Iterable<Pattern> getPatterns() {
    return userList.get().stream()
        .map(User::getMentionPattern)
        .collect(Collectors.toSet());
  }

  @Override
  public @Nullable Supplier<Matcher> handleQuote(String quote) {
    for (User loaded : userList.get()) {
      Pattern mentionPattern = loaded.getMentionPattern();
      Matcher matcher = mentionPattern.matcher(quote);
      if (!matcher.find()) {
        continue;
      }
      return new UserMatcher() {
        @Override
        public User getUser() {
          return loaded;
        }

        @Override
        public Matcher get() {
          return matcher;
        }
      };
    }
    return null;
  }

  @Override
  public void addComponents(
      @NotNull ParsedText components, @NotNull Supplier<Matcher> matcherSupplier) {
    if (!(matcherSupplier instanceof UserMatcher)) {
      components.addText(matcherSupplier.get().group());
      return;
    }

    Matcher matcher = matcherSupplier.get();
    User user = ((UserMatcher) matcherSupplier).getUser();

    components.addComponent(user.getMention());
    if (matcher.group(2) != null && !matcher.group(2).isEmpty()) {
      components.addText(matcher.group(2));
    }
  }

  private interface UserMatcher extends Supplier<Matcher> {
    User getUser();
  }

}
