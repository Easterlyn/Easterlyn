package com.easterlyn.chat.channel;

import com.easterlyn.user.User;
import com.easterlyn.util.Colors;
import com.easterlyn.util.command.Group;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public interface Channel extends Group {

  /**
   * Get the name of the channel.
   *
   * @return the name
   */
  @NotNull String getName();

  /**
   * Get whether the channel counts as matching a channel name. Note that this does not necessarily
   * mean that the channel is the exact channel denoted by the channel name, only that it is a
   * supported identifier.
   *
   * @param name the name to match
   * @return whether the name matches
   */
  boolean isFocusedChannel(@Nullable String name);

  /**
   * Get the display name of the channel.
   *
   * @return the channel's display name
   */
  default @NotNull String getDisplayName() {
    return "#" + getName();
  }

  /**
   * Get a TextComponent representing the channel in a user-friendly way.
   *
   * @return a TextComponent used to represent the channel
   */
  @Override
  default @NotNull TextComponent getMention() {
    TextComponent component = new TextComponent(getDisplayName());
    component.setColor(Colors.CHANNEL);
    component.setUnderlined(true);
    component.setHoverEvent(
        new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new Text(
                TextComponent.fromLegacyText(
                    Colors.COMMAND + "/join " + Colors.CHANNEL + getDisplayName()))));
    component.setClickEvent(
        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + getDisplayName()));
    return component;
  }

  /**
   * Get whether the channel is private or public.
   *
   * @return true if the channel is private
   */
  boolean isPrivate();

  /**
   * Set whether the channel is private or public.
   *
   * @param isPrivate true if the channel is private
   */
  void setPrivate(boolean isPrivate);

  /**
   * Get the password for the channel.
   *
   * @return the channel's password or null if no password is set
   */
  @Nullable String getPassword();

  /**
   * Set the password for the channel.
   *
   * @param password the new password
   */
  void setPassword(@Nullable String password);

  /**
   * Get the channel owner's UUID.
   *
   * @return the UUID
   */
  @Nullable UUID getOwner();

  /**
   * Get whether a user is a channel's owner.
   *
   * @param user a user
   * @return if this user is an owner
   */
  default boolean isOwner(@NotNull User user) {
    return user.getUniqueId().equals(getOwner())
        || user.hasPermission("easterlyn.chat.channel.owner");
  }

  /**
   * Get whether a user is a channel moderator.
   *
   * @param user a user
   * @return whether this user has permission to moderate the channel
   */
  boolean isModerator(@NotNull User user);

  /**
   * Set whether a user is a moderator.
   *
   * @param user the user
   * @param moderator whether the user is a moderator
   */
  void setModerator(@NotNull User user, boolean moderator);

  /**
   * Check if the user allowed to enter the channel.
   *
   * @param user a user
   * @return whether the user is allowed to join
   */
  boolean isWhitelisted(@NotNull User user);

  /**
   * Set whether a user is allowed to enter the channel.
   *
   * @param user the user
   * @param whitelisted whether the user is allowed to join the channel
   */
  void setWhitelisted(@NotNull User user, boolean whitelisted);

  /**
   * Check if the given user is banned.
   *
   * @param user the user
   * @return true if the user is banned
   */
  boolean isBanned(@NotNull User user);

  /**
   * Sets whether a user is banned.
   *
   * @param user the user
   * @param banned whether the user is a moderator
   */
  void setBanned(@NotNull User user, boolean banned);

  /**
   * Check if the channel has been recently accessed and should not be deleted.
   *
   * @return true if the channel should not be deleted
   */
  boolean isRecentlyAccessed();

  /** Update the last access time. */
  void updateLastAccess();

  /**
   * Loads additional data out of a ConfigurationSection specific to this channel.
   *
   * @param data the ConfigurationSection for the channel
   */
  void load(@NotNull ConfigurationSection data);

  /**
   * Save the Channel's data.
   *
   * @param channelStorage the Configuration storing channels
   */
  default void save(@NotNull Configuration channelStorage) {
    channelStorage.set(getName() + ".class", getClass().getName());
    UUID owner = getOwner();
    if (owner != null) {
      channelStorage.set(getName() + ".owner", owner.toString());
    }
  }

}
