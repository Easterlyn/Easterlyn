package com.easterlyn.chat.event;

import com.easterlyn.EasterlynChat;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.event.UserEvent;
import com.easterlyn.user.PlayerUser;
import com.easterlyn.user.User;
import com.easterlyn.util.Colors;
import com.easterlyn.util.text.TextParsing;
import com.easterlyn.util.text.impl.PlayerItemQuoteConsumer;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class UserChatEvent extends UserEvent implements Cancellable {

  private static final HandlerList HANDLER_LIST = new HandlerList();

  private final Channel channel;
  private final String text;
  private final boolean thirdPerson;
  private boolean cancelled = false;

  public UserChatEvent(@NotNull User who, @NotNull Channel channel, @NotNull String text) {
    this(who, channel, text, false);
  }

  public UserChatEvent(
      @NotNull User who, @NotNull Channel channel, @NotNull String text, boolean thirdPerson) {
    super(who);
    this.channel = channel;
    this.text = text;
    this.thirdPerson = thirdPerson;
  }

  public @NotNull static HandlerList getHandlerList() {
    return HANDLER_LIST;
  }

  public @NotNull Channel getChannel() {
    return channel;
  }

  public @NotNull String getText() {
    return text;
  }

  public boolean isThirdPerson() {
    return thirdPerson;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  public @NotNull HandlerList getHandlers() {
    return HANDLER_LIST;
  }

  public void send() {
    send(Collections.emptyList());
  }

  public void send(Collection<UUID> additionalRecipients) {
    Bukkit.getPluginManager().callEvent(this);

    if (this.isCancelled()) {
      return;
    }

    TextComponent channelElement = new TextComponent();
    TextComponent[] channelHover =
        new TextComponent[] {
          new TextComponent("/join "), new TextComponent(channel.getDisplayName())
        };
    channelHover[0].setColor(Colors.COMMAND);
    channelHover[1].setColor(Colors.CHANNEL);
    channelElement.setHoverEvent(
        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new net.md_5.bungee.api.chat.hover.content.Text(channelHover)));
    channelElement.setClickEvent(
        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/join " + channel.getDisplayName()));

    TextComponent channelName = new TextComponent(channel.getDisplayName());
    channelName.setColor(
        (channel.isOwner(getUser())
            ? Colors.CHANNEL_OWNER
            : channel.isModerator(getUser()) ? Colors.CHANNEL_MODERATOR : Colors.CHANNEL_MEMBER));

    TextComponent nameElement = new TextComponent(thirdPerson ? "> " : " <");
    TextComponent userElement = getUser().getMention();
    nameElement.setHoverEvent(userElement.getHoverEvent());
    nameElement.setClickEvent(userElement.getClickEvent());
    TextComponent nameText = new TextComponent(userElement.getText().substring(1));
    nameText.setColor(userElement.getColor());
    nameElement.addExtra(nameText);
    nameElement.addExtra(new TextComponent(thirdPerson ? " " : "> "));

    Collection<TextComponent> messageComponents;
    Player player;
    if (getUser() instanceof PlayerUser playerUser) {
      player = playerUser.getPlayer();
    } else {
      player = null;
    }
    if (player == null) {
      messageComponents = TextParsing.toJSON(text);
    } else {
      messageComponents = TextParsing.toJSON(text, Set.of(new PlayerItemQuoteConsumer(player)));
    }

    Stream.concat(additionalRecipients.stream(), channel.getMembers().stream())
        .distinct()
        .map(uuid -> getUser().getPlugin().getUserManager().getLoadedPlayer(uuid))
        .forEach(
            user -> {
              if (user == null) {
                return;
              }

              boolean highlight = false;

              // Copy and convert TextComponents from parsed message
              List<TextComponent> highlightedComponents = new LinkedList<>();
              for (TextComponent textComponent : messageComponents) {
                String text = textComponent.getText();
                Matcher matcher = getHighlightPattern(user).matcher(text);
                int previousMatch = 0;

                while (matcher.find()) {
                  highlight = true;
                  if (matcher.start() > 0) {
                    TextComponent start = new TextComponent(textComponent);
                    start.setText(text.substring(previousMatch, matcher.start()));
                    highlightedComponents.add(start);
                  }
                  TextComponent mention = new TextComponent(user.getMention());
                  mention.setColor(Colors.HIGHLIGHT);
                  highlightedComponents.add(mention);
                  // Set previous match to end of group 1 so next will pick up group 2 if it exists.
                  previousMatch = matcher.end(1);
                }
                if (previousMatch == 0) {
                  // No matches, no need to modify component for user.
                  highlightedComponents.add(textComponent);
                  continue;
                }
                if (previousMatch == text.length()) {
                  // Last match coincided with the end of the text.
                  continue;
                }
                TextComponent end = new TextComponent(textComponent);
                end.setText(text.substring(previousMatch));
                highlightedComponents.add(end);
              }

              TextComponent finalMessage = new TextComponent();
              // Set text a nice relaxing grey if not focused.
              finalMessage.setColor(
                  channel.isFocusedChannel(user.getStorage().getString(EasterlynChat.USER_CURRENT))
                      ? ChatColor.WHITE
                      : ChatColor.GRAY);

              TextComponent channelBrace;
              if (highlight) {
                channelBrace = new TextComponent("!!");
                channelBrace.setColor(Colors.HIGHLIGHT);
              } else {
                channelBrace = new TextComponent("[");
              }
              BaseComponent finalChannel = channelElement.duplicate();
              finalChannel.addExtra(channelBrace);
              finalChannel.addExtra(channelName);
              if (!highlight) {
                channelBrace = new TextComponent("]");
              }
              finalChannel.addExtra(channelBrace);

              finalMessage.addExtra(finalChannel);
              finalMessage.addExtra(nameElement);
              for (TextComponent component : highlightedComponents) {
                finalMessage.addExtra(component);
              }

              user.sendMessage(finalMessage);
            });

    Bukkit.getConsoleSender()
        .sendMessage(
            ChatColor.stripColor(
                String.format(
                    "[%1$s]" + (thirdPerson ? "> %2$s " : " <%2$s> ") + "%3$s",
                    channel.getDisplayName(),
                    getUser().getDisplayName(),
                    text)));
  }

  private @NotNull Pattern getHighlightPattern(User user) {
    Object storedPattern = user.getTemporaryStorage().get(EasterlynChat.USER_HIGHLIGHTS);
    if (storedPattern instanceof Pattern) {
      return (Pattern) storedPattern;
    }
    StringBuilder builder = new StringBuilder("(?:^|\\s)@?(");
    getHighlights(user).forEach(string -> builder.append("\\Q").append(string).append("\\E|"));
    builder.replace(
        builder.length() - 1,
        builder.length(),
        ")([\\\\W&&[^" + ChatColor.COLOR_CHAR + "}]])?(?:$|\\s)");
    Pattern pattern = Pattern.compile(builder.toString());
    user.getTemporaryStorage().put(EasterlynChat.USER_HIGHLIGHTS, pattern);
    return pattern;
  }

  private @NotNull Stream<String> getHighlights(User user) {
    List<String> highlights = user.getStorage().getStringList(EasterlynChat.USER_HIGHLIGHTS);

    highlights.add(user.getUniqueId().toString());
    highlights.add(user.getDisplayName());

    Player player = user.isOnline() && user instanceof PlayerUser playerUser ? playerUser.getPlayer() : null;
    if (player != null) {
      highlights.add(player.getName());
    }

    return highlights.stream().distinct().filter(string -> !string.isEmpty());
  }
}
