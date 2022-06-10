package com.easterlyn.kitchensink.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;

public class NoCommandPrefix implements Listener {

  @EventHandler(ignoreCancelled = true)
  public void onPlayerCommand(@NotNull PlayerCommandPreprocessEvent event) {
    int colon = event.getMessage().indexOf(':');
    int space = event.getMessage().indexOf(' ');
    if (!event.getPlayer().hasPermission("easterlyn.commands.unfiltered")
        && 0 < colon
        && (colon < space || space < 0)) {
      event.setMessage("/" + event.getMessage().substring(colon + 1));
    }
  }

  @EventHandler
  public void onCommandSend(@NotNull PlayerCommandSendEvent event) {
    if (event.getPlayer().hasPermission("easterlyn.commands.unfiltered")) {
      return;
    }

    event.getCommands().removeIf(command -> command.indexOf(':') > -1);
  }

  @EventHandler
  public void onTabComplete(@NotNull TabCompleteEvent event) {
    if (!(event.getSender() instanceof Player)
        || event.getSender().hasPermission("easterlyn.commands.unfiltered")) {
      return;
    }

    int colon = event.getBuffer().indexOf(':');
    int space = event.getBuffer().indexOf(' ');
    if (0 < colon && (colon < space || space < 0)) {
      event.setCancelled(true);
    }
  }
}
