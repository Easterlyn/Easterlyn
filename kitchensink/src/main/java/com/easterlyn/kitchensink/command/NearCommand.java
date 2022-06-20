package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.util.Colors;
import com.easterlyn.util.text.ParsedText;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class NearCommand extends BaseCommand {

  private static final int MAX_RADIUS = 200;

  @Dependency EasterlynCore core;

  @CommandAlias("near")
  @Description("{@@sink.module.near.description}")
  @Syntax("[range]")
  @CommandCompletion("@integer")
  @CommandPermission("easterlyn.command.near")
  public void near(@Flags(CoreContexts.SELF) Player issuer, @Default("200") int range) {
    range = Math.max(1, range);
    if (!issuer.hasPermission("easterlyn.command.near.far")) {
      range = Math.min(MAX_RADIUS, range);
    }

    String locale = core.getLocaleManager().getLocale(issuer);
    ParsedText message = new ParsedText();
    message.addText(core.getLocaleManager().getValue("sink.module.near.message", locale));

    List<Player> players = issuer.getWorld().getPlayers();
    if (players.size() <= 1) {
      message.addText(core.getLocaleManager().getValue("sink.module.near.none", locale));
      return;
    }

    Location location = issuer.getLocation();
    boolean showSpectate = issuer.hasPermission("easterlyn.command.near.spectate");
    boolean showInvisible = issuer.hasPermission("easterlyn.command.near.invisible");
    double squared = Math.pow(range, 2);
    int matches = 0;
    TextComponent separator = new TextComponent(", ");
    ChatColor normalA = Colors.getOrDefault("normal.a", ChatColor.YELLOW);
    separator.setColor(normalA);
    ChatColor normalB = Colors.getOrDefault("normal.b", ChatColor.DARK_AQUA);

    for (Player player : players) {
      if (issuer.getUniqueId().equals(player.getUniqueId())
          || !issuer.canSee(player)
          || !showSpectate && player.getGameMode() == GameMode.SPECTATOR
          || !showInvisible && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
        continue;
      }
      double distanceSquared = location.distanceSquared(player.getLocation());
      if (distanceSquared > squared) {
        continue;
      }
      ++matches;
      int distance = (int) Math.sqrt(distanceSquared);
      message.addComponent(core.getUserManager().getUser(player.getUniqueId()).getMention());
      message.addText(normalA + " (" + normalB + distance + normalA + ')');
      message.addComponent(separator);
    }

    if (matches == 0) {
      message.addText(core.getLocaleManager().getValue("sink.module.near.none", locale));
    }

    List<TextComponent> components = message.getComponents();
    if (matches > 0) {
      // Remove trailing comma component
      components.remove(components.size() - 1);
    }

    issuer.spigot().sendMessage(new TextComponent(components.toArray(new BaseComponent[0])));
  }
}
