package com.easterlyn.kitchensink.command;

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
import com.easterlyn.EasterlynKitchenSink;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.util.BossBarTimer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class DeathPointCommand extends BaseCommand {

  public static final String DEATH_COOLDOWN = "kitchensink.deathTime";
  @Dependency private EasterlynCore core;
  @Dependency private EasterlynKitchenSink sink;

  @CommandAlias("deathtp")
  @Description("{@@sink.module.deathtp.description}")
  @CommandPermission("easterlyn.command.deathtp.self")
  @Syntax("[target]")
  @CommandCompletion("@player")
  public void death(BukkitCommandIssuer issuer, @Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
    boolean other = !issuer.getUniqueId().equals(user.getUniqueId());

    if (!other && user.getStorage().getLong(DEATH_COOLDOWN) >= System.currentTimeMillis()) {
      SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss");
      issuer.sendInfo(
          MessageKey.of("sink.module.deathtp.error.cooldown"),
          "{value}",
          dateFormat.format(
              new Date(user.getStorage().getLong(DEATH_COOLDOWN) - System.currentTimeMillis())));
      return;
    }

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

    String title = core.getLocaleManager().getValue("sink.common.teleport.bar_title");
    if (title == null) {
      title = "";
    }

    new BossBarTimer(
            title,
            () -> {
              if (player.teleport(death)) {
                if (other) {
                  issuer.sendInfo(
                      MessageKey.of("sink.module.deathtp.success_other"),
                      "{target}",
                      player.getName());
                } else {
                  user.getStorage().set(DEATH_COOLDOWN, System.currentTimeMillis() + 30000L);
                }
                core.getLocaleManager().sendMessage(player, "sink.module.deathtp.success");
              } else {
                issuer.sendInfo(MessageKey.of("sink.common.teleport.blocked"));
              }
            },
            player)
        .withFailureFunction(
            BossBarTimer.supplierPlayerImmobile(player),
            () -> core.getLocaleManager().sendMessage(player, "sink.common.teleport.movement"))
        .schedule(sink, DEATH_COOLDOWN, 4, TimeUnit.SECONDS);
  }

}
