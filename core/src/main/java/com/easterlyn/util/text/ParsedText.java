package com.easterlyn.util.text;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParsedText {

  private final List<TextComponent> components;
  private @Nullable Boolean bold;
  private @Nullable Boolean italic;
  private @Nullable Boolean underline;
  private @Nullable Boolean strike;
  private @Nullable Boolean magic;
  private @Nullable ChatColor color;

  public ParsedText() {
    this.components = new LinkedList<>();
  }

  public Collection<TextComponent> getComponents() {
    return this.components;
  }

  public void addComponent(TextComponent component) {
    this.components.add(component);
  }

  public void addComponent(TextComponent... components) {
    Collections.addAll(this.components, components);
  }

  public void addComponents(Collection<TextComponent> components) {
    this.components.addAll(components);
  }

  public void addText(@NotNull String text) {
    addText(text, null, null);
  }

  public void addText(
      @NotNull String text, @Nullable HoverEvent hover, @Nullable ClickEvent click) {
    StringBuilder builder = new StringBuilder();

    for (int i = 0; i < text.length(); ++i) {
      char c = text.charAt(i);
      if (c != ChatColor.COLOR_CHAR) {
        builder.append(c);
        continue;
      }

      ++i;
      if (i >= text.length()) {
        break;
      }

      c = text.charAt(i);
      if (c >= 'A' && c <= 'Z') {
        c = (char) (c + 32);
      }

      ChatColor format = ChatColor.getByChar(c);
      if (format != null) {
        if (builder.length() > 0) {
          components.add(newComponent(builder, hover, click));
          builder = new StringBuilder();
        }

        if (ChatColor.BOLD.equals(format)) {
          bold = true;
        } else if (ChatColor.ITALIC.equals(format)) {
          italic = true;
        } else if (ChatColor.UNDERLINE.equals(format)) {
          underline = true;
        } else if (ChatColor.STRIKETHROUGH.equals(format)) {
          strike = true;
        } else if (ChatColor.MAGIC.equals(format)) {
          magic = true;
        } else {
          bold = null;
          italic = null;
          underline = null;
          strike = null;
          magic = null;
          color = ChatColor.RESET.equals(format) ? null : format;
        }
      }
    }

    components.add(newComponent(builder, hover, click));
  }

  private TextComponent newComponent(
      StringBuilder builder, @Nullable HoverEvent hover, @Nullable ClickEvent click) {
    TextComponent component = new TextComponent();
    component.setColor(color);
    component.setBold(bold);
    component.setItalic(italic);
    component.setUnderlined(underline);
    component.setStrikethrough(strike);
    component.setObfuscated(magic);
    component.setText(builder.toString());
    component.setHoverEvent(hover);
    component.setClickEvent(click);

    return component;
  }
}
