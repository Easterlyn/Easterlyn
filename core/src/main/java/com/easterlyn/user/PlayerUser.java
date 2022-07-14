package com.easterlyn.user;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.PlayerUtil;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import com.github.jikoo.planarwrappers.util.Generics;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.UUID;

public class PlayerUser extends User {

  PlayerUser(
      @NotNull EasterlynCore plugin,
      @NotNull UUID uuid,
      @NotNull ConcurrentConfiguration storage) {
    super(plugin, uuid, storage);
  }

  protected PlayerUser(
      @NotNull User user) {
    super(user);
  }

  @Override
  public @NotNull String getDisplayName() {
    return Generics.orDefault(
        getStorage().getString("displayName"),
        Generics.orDefault(
            Generics.functionAs(Player.class, getPlayer(), Player::getDisplayName),
            getUniqueId().toString()));
  }

  @Override
  public void sendMessage(@NotNull BaseComponent... components) {
    Player player = getPlayer();
    if (player != null) {
      player.spigot().sendMessage(components);
    }
  }

  @Override
  public boolean hasPermission(@NotNull String permission) {
    if (isOnline()) {
      Player player = getPlayer();
      if (player != null) {
        return player.hasPermission(permission);
      }
    }
    return PermissionUtil.hasPermission(getUniqueId(), permission);
  }

  public @Nullable Player getPlayer() {
    Player loadedPlayer = PlayerUtil.getLoadedPlayer(getUniqueId(), true);
    if (loadedPlayer == null && Bukkit.isPrimaryThread()) {
      loadedPlayer = PlayerUtil.loadOfflinePlayer(getUniqueId());
      if (loadedPlayer != null) {
        PlayerUtil.cachePlayer(loadedPlayer);
      }
    }
    return loadedPlayer;
  }

}
