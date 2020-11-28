package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import java.util.Objects;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CoordinatesCommand extends BaseCommand {

  // TODO lang
  @CommandAlias("coordinates|coords")
  @Description("Get your coordinates!")
  @CommandPermission("easterlyn.command.coordinates.self")
  @Syntax("[player]")
  @CommandCompletion("@player")
  public void getCoordinates(
      BukkitCommandIssuer issuer, @Flags(CoreContexts.ONLINE_WITH_PERM) User target) {
    Player player = target.getPlayer();

    if (player == null) {
      issuer.sendMessage("Unable to load player data at this time!");
      return;
    }

    Location loc = player.getLocation();
    String baseMessage =
        String.format(
            "%1$sOn %2$s%3$s%1$s at %2$s%4$.1f%1$s, "
                + "%2$s%5$.1f%1$s, %2$s%6$.1f%1$s, %2$s%7$.1f%1$s pitch, and %2$s%8$.1f%1$s yaw.",
            ChatColor.YELLOW,
            ChatColor.DARK_AQUA,
            Objects.requireNonNull(loc.getWorld()).getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getPitch(),
            loc.getYaw());

    if (!issuer.isPlayer()) {
      issuer.sendMessage(baseMessage);
      return;
    }

    TextComponent richText = new TextComponent(TextComponent.fromLegacyText(baseMessage));
    richText.setClickEvent(
        new ClickEvent(
            ClickEvent.Action.SUGGEST_COMMAND,
            String.format(
                "%1$s %2$.0f %3$.0f %4$.0f %5$.0f %6$.0f",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getPitch(),
                loc.getYaw())));
    richText.setHoverEvent(
        new HoverEvent(
            HoverEvent.Action.SHOW_TEXT,
            new Text(
                TextComponent.fromLegacyText(ChatColor.YELLOW + "Click to insert into chat!"))));
    issuer.getIssuer().spigot().sendMessage(richText);
  }
}
