package com.easterlyn.util.text.impl;

import com.easterlyn.util.Colors;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.StaticQuoteConsumer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.Locale;
import java.util.function.Supplier;
import java.util.regex.Matcher;

public class UrlQuoteConsumer extends StaticQuoteConsumer {

  public UrlQuoteConsumer() {
    super(StringUtil.URL_PATTERN);
  }

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
      @NotNull ParsedText components,
      @NotNull Supplier<Matcher> matcherSupplier) {
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

}
