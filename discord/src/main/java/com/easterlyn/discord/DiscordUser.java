package com.easterlyn.discord;

import com.easterlyn.user.PlayerUser;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.Colors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class DiscordUser extends PlayerUser {

  public DiscordUser(PlayerUser user) {
    // TODO not require hard linking?
    super(user);
  }

  @Override
  public TextComponent getMention() {
    TextComponent component = new TextComponent("@" + getDisplayName());
    component.setColor(getColor());

    TextComponent line = new TextComponent("#main");
    line.setColor(Colors.CHANNEL);
    TextComponent extra = new TextComponent("on Discord");
    extra.setColor(ChatColor.WHITE);
    line.addExtra(extra);
    line.addExtra(extra);
    component.addExtra(line);

    line = new TextComponent("\n" + getDisplayName());
    line.setColor(getColor());
    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(getUniqueId());
    if (offlinePlayer.getName() != null && !offlinePlayer.getName().equals(line.getText().trim())) {
      TextComponent realName = new TextComponent(" (" + offlinePlayer.getName() + ")");
      realName.setColor(ChatColor.WHITE);
      line.addExtra(realName);
    }
    component.addExtra(line);

    UserRank rank = getRank();
    line = new TextComponent("\n" + rank.getFriendlyName());
    line.setColor(rank.getColor());
    component.addExtra(line);

    return component;
  }

}
