package com.easterlyn.chat.channel;

import com.easterlyn.util.Colors;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MessageChannel extends InternalChannel {

  public MessageChannel() {
    super("dm");
  }

  @Override
  public boolean isFocusedChannel(@Nullable String channelName) {
    return true;
  }

  @Override
  public @NotNull TextComponent getMention() {
    TextComponent component = new TextComponent(getDisplayName());
    component.setColor(Colors.CHANNEL);
    component.setUnderlined(true);
    component.setHoverEvent(
        new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new Text(
                TextComponent.fromLegacyText(
                    Colors.CHANNEL + "Direct Messages"))));
    component.setClickEvent(
        new ClickEvent(Action.SUGGEST_COMMAND, "/r "));
    return component;
  }

}
