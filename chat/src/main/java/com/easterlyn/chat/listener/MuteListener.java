package com.easterlyn.chat.listener;

import com.easterlyn.EasterlynChat;
import com.easterlyn.chat.event.UserChatEvent;
import com.easterlyn.plugin.EasterlynPlugin;
import com.easterlyn.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.jetbrains.annotations.NotNull;

public class MuteListener implements Listener {

  private final EasterlynPlugin plugin;

  public MuteListener(EasterlynPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  private void onUserChat(@NotNull UserChatEvent event) {
    if (event.getUser().getStorage().getLong(EasterlynChat.USER_MUTE, 0L)
        > System.currentTimeMillis()) {
      event.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  private void onPlayerChat(@NotNull AsyncPlayerChatEvent event) {
    cancelIfMute(event.getPlayer(), event);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  private void onBookEdit(@NotNull PlayerEditBookEvent event) {
    cancelIfMute(event.getPlayer(), event);
  }

  @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
  private void onSignChange(@NotNull SignChangeEvent event) {
    cancelIfMute(event.getPlayer(), event);
  }

  private void cancelIfMute(@NotNull Player player, @NotNull Cancellable cancellable) {
    // Have to get or load immediately - there's little way to undo the events after the fact.
    User user = plugin.getCore().getUserManager().getOrLoadNow(player.getUniqueId());
    if (user.getStorage().getLong(EasterlynChat.USER_MUTE, 0L) > System.currentTimeMillis()) {
      cancellable.setCancelled(true);
    }
  }
}
