package com.easterlyn.util.text.impl;

import com.easterlyn.util.Colors;
import com.easterlyn.util.text.ParsedText;
import com.easterlyn.util.text.StaticQuoteConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;

public class CommandQuoteConsumer extends StaticQuoteConsumer {

  public CommandQuoteConsumer() {
    super(Pattern.compile("/.{1,}"));
  }

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

}
