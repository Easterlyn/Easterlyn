package com.easterlyn.user;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.Colors;
import com.easterlyn.util.Request;
import com.easterlyn.util.command.Group;
import com.easterlyn.util.text.TextParsing;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.ServerOperator;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Data storage for a user.
 *
 * @author Jikoo
 */
public abstract class User extends PermissibleBase implements Group, ServerOperator {

  protected final EasterlynCore plugin;
  private final UUID uuid;
  private final ConcurrentConfiguration storage;
  private final Map<String, Object> tempStore;

  User(
      @NotNull EasterlynCore plugin,
      @NotNull UUID uuid,
      @NotNull ConcurrentConfiguration storage) {
    super(null);
    this.plugin = plugin;
    this.uuid = uuid;
    this.storage = storage;
    this.tempStore = new ConcurrentHashMap<>();
    this.plugin.getServer().getPluginManager().subscribeToDefaultPerms(false, this);
  }

  @Contract(pure = true)
  protected User(@NotNull User user) {
    super(null);
    plugin = user.plugin;
    uuid = user.uuid;
    storage = user.storage;
    tempStore = user.tempStore;
  }

  public @NotNull UUID getUniqueId() {
    return uuid;
  }

  @Override
  public @NotNull Collection<UUID> getMembers() {
    return Collections.singleton(getUniqueId());
  }

  public abstract @NotNull String getDisplayName();

  public @NotNull ChatColor getColor() {
    return Colors.getOrDefault(getStorage().getString("color"), getRank().getColor());
  }

  public void setColor(@NotNull ChatColor color) {
    if (color == ChatColor.RESET) {
      getStorage().set("color", null);
      return;
    }
    if (color == ChatColor.BOLD
        || color == ChatColor.UNDERLINE
        || color == ChatColor.ITALIC
        || color == ChatColor.STRIKETHROUGH
        || color == ChatColor.MAGIC) {
      throw new IllegalArgumentException("Color must be a color, not a format code!");
    }
    getStorage().set("color", color.getName());
    reloadMention();
  }

  public boolean isOnline() {
    return plugin.getServer().getPlayer(getUniqueId()) != null;
  }

  public @NotNull UserRank getRank() {
    UserRank[] userRanks = UserRank.values();
    for (int i = userRanks.length - 1; i > 0; --i) {
      if (hasPermission(userRanks[i].getPermission())) {
        return userRanks[i];
      }
    }
    return UserRank.MEMBER;
  }

  protected @NotNull Pattern getMentionPattern() {
    Object storedPattern = getTemporaryStorage().get("mentionPattern");

    if (storedPattern instanceof Pattern) {
      return (Pattern) storedPattern;
    }

    StringBuilder builder = new StringBuilder("^@?(");
    OfflinePlayer player = Bukkit.getOfflinePlayer(getUniqueId());
    if (player.getName() != null) {
      builder.append("\\Q").append(player.getName()).append("\\E").append('|');
      Player online = player.isOnline() ? player.getPlayer() : null;
      if (online != null
          && !online.getDisplayName().isEmpty()
          && !online.getDisplayName().equals(player.getName())) {
        builder.append("\\Q").append(online.getDisplayName()).append("\\E|");
      }
    }
    builder.append(getUniqueId()).append(")([\\\\W&&[^" + ChatColor.COLOR_CHAR + "}]])?$");

    Pattern pattern = Pattern.compile(builder.toString(), Pattern.CASE_INSENSITIVE);
    getTemporaryStorage().put("mentionPattern", pattern);

    return pattern;
  }

  @Override
  public TextComponent getMention() {
    Object storedMention = getTemporaryStorage().get("mention");

    if (storedMention instanceof TextComponent) {
      return (TextComponent) storedMention;
    }

    TextComponent component = new TextComponent("@" + getDisplayName());
    component.setColor(getColor());
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getUniqueId());
    component.setClickEvent(
        new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND,
            (offlinePlayer.isOnline() ? "/msg " : "/mail send ")
                + (offlinePlayer.getName() != null ? offlinePlayer.getName() : getUniqueId())));

    TextComponent line = new TextComponent(getDisplayName());
    line.setColor(getColor());
    if (offlinePlayer.getName() != null && !offlinePlayer.getName().equals(line.getText())) {
      TextComponent realName = new TextComponent(" (" + offlinePlayer.getName() + ")");
      realName.setColor(net.md_5.bungee.api.ChatColor.WHITE);
      line.addExtra(realName);
    }
    TextComponent extra = new TextComponent(" - ");
    extra.setColor(net.md_5.bungee.api.ChatColor.WHITE);
    line.addExtra(extra);
    extra = new TextComponent("Click to message!");
    extra.setColor(Colors.COMMAND);
    line.addExtra(extra);
    List<TextComponent> hovers = new ArrayList<>();
    hovers.add(line);

    UserRank rank = getRank();
    line = new TextComponent("\n" + rank.getFriendlyName());
    line.setColor(rank.getColor());
    hovers.add(line);
    // TODO class and affinity

    component.setHoverEvent(
        new HoverEvent(
            HoverEvent.Action.SHOW_TEXT, new Text(hovers.toArray(new TextComponent[0]))));

    getTemporaryStorage().put("mention", component);

    return component;
  }

  public void reloadMention() {
    // Temp store is null on super initialization.
    if (tempStore != null) {
      this.tempStore.remove("mentionPattern");
      this.tempStore.remove("mention");
    }
  }

  public void sendMessage(@NotNull String message) {
    sendMessage(TextParsing.toJSON(message).toArray(new TextComponent[0]));
  }

  public abstract void sendMessage(@NotNull BaseComponent... components);

  public @NotNull ConcurrentConfiguration getStorage() {
    return storage;
  }

  public @NotNull Map<String, Object> getTemporaryStorage() {
    return tempStore;
  }

  /**
   * Gets and clears the pending request, if any.
   *
   * @return the Request or null if not present
   */
  public @Nullable Request pollPendingRequest() {
    Object stored = tempStore.remove("core.request");
    if (!(stored instanceof Request request)) {
      return null;
    }
    return request.getExpiry() > System.currentTimeMillis() ? request : null;
  }

  /**
   * Sets a user's pending request if nothing is currently pending.
   *
   * @param request the Request
   * @return true if the Request was successfully added
   */
  public boolean setPendingRequest(@NotNull Request request) {
    Object stored = tempStore.get("core.request");
    if (stored instanceof Request && ((Request) stored).getExpiry() > System.currentTimeMillis()) {
      return false;
    }
    tempStore.put("core.request", request);
    return true;
  }

  public EasterlynCore getPlugin() {
    return plugin;
  }

  @Override
  public abstract boolean hasPermission(@NotNull String permission);

  @Override
  public boolean hasPermission(@NotNull Permission perm) {
    return hasPermission(perm.getName());
  }

  @Override
  public void recalculatePermissions() {
    super.recalculatePermissions();
    reloadMention();
  }

}
