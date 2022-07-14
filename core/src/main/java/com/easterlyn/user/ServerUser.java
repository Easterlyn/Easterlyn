package com.easterlyn.user;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.Colors;
import com.easterlyn.util.text.TextParsing;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import com.github.jikoo.planarwrappers.util.Generics;
import java.util.Map;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

public class ServerUser extends User {

  private final Map<String, String> userData;

  public ServerUser(@NotNull EasterlynCore core, @NotNull Map<String, String> userData) {
    super(core, new UUID(0, 0), ConcurrentConfiguration.load(core, null));
    this.userData = userData;
  }

  @Override
  public @NotNull String getDisplayName() {
    return ChatColor.translateAlternateColorCodes(
        '&', Generics.orDefault(userData.get("name"), "Auto User"));
  }

  @Override
  public @NotNull ChatColor getColor() {
    return Colors.getOrDefault(userData.get("color"), getRank().getColor());
  }

  @Override
  public boolean isOnline() {
    return false;
  }

  @Override
  public boolean hasPermission(@NotNull String permission) {
    Permission perm = getPlugin().getServer().getPluginManager().getPermission(permission);
    return perm == null
        || perm.getDefault() == PermissionDefault.TRUE
        || perm.getDefault() == PermissionDefault.OP;
  }

  @Override
  public @NotNull UserRank getRank() {
    return UserRank.ADMIN;
  }

  @Override
  public TextComponent getMention() {
    TextComponent component = new TextComponent("@" + getDisplayName());
    component.setColor(getColor());

    String click = userData.get("click");
    if (click != null && !click.isEmpty()) {
      component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
    }

    String hover = userData.get("hover");
    if (hover != null && !hover.isEmpty()) {
      hover = ChatColor.translateAlternateColorCodes('&', hover);
      component.setHoverEvent(
          new HoverEvent(
              HoverEvent.Action.SHOW_TEXT,
              new Text(TextParsing.toJSON(hover).toArray(new TextComponent[0]))));
    }

    return component;
  }

  @Override
  public void sendMessage(@NotNull String message) {
    Bukkit.getConsoleSender().sendMessage(message);
  }

  @Override
  public void sendMessage(@NotNull BaseComponent... components) {
    Bukkit.getConsoleSender().spigot().sendMessage(components);
  }

}
