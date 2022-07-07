package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;

public class DeathCoordinates extends BaseCommand implements Listener {

  @Dependency private EasterlynCore core;

  @CommandAlias("deathtp")
  @Description("{@@sink.module.deathtp.description}")
  @CommandPermission("easterlyn.command.deathtp.self")
  @Syntax("[target]")
  @CommandCompletion("@player")
  public void death(BukkitCommandIssuer issuer, @Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    Player player = user.getPlayer();

    if (player == null) {
      issuer.sendInfo(MessageKey.of("sink.common.teleport.player_unloaded"));
      return;
    }

    Location death = player.getLastDeathLocation();
    if (death == null) {
      issuer.sendInfo(MessageKey.of("sink.module.death.error.missing"));
      return;
    }

    World world = death.getWorld();
    if (world == null) {
      issuer.sendInfo(MessageKey.of("sink.module.death.error.world_unloaded"));
      return;
    }

    core.getLocaleManager().sendMessage(issuer.getIssuer(), "sink.module.death.success",
        "{world}", world.getName(),
        "{x}", String.valueOf(death.getBlockX()),
        "{y}", String.valueOf(death.getBlockY()),
        "{z}", String.valueOf(death.getBlockZ()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
    // TODO fun messages, clickable location component
    // TODO permanent record - maybe merge into /death
    Player player = event.getEntity();
    Location location = player.getLocation();
    String message =
        "Death point: {X} {Y} {Z}"
            .replace("{X}", String.valueOf(location.getBlockX()))
            .replace("{Y}", String.valueOf(location.getBlockY()))
            .replace("{Z}", String.valueOf(location.getBlockZ()));
    player.sendMessage("Oh dear, you are dead. " + message);
    Bukkit.getConsoleSender().sendMessage(player.getName() + " died. " + message);
  }

}
