package com.easterlyn.util;

import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.UserRank;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Permission-related utility.
 *
 * @author Jikoo
 */
public class PermissionUtil {

  private PermissionUtil() {}

  public @NotNull static Permission getOrCreate(
      @NotNull String permissionName, @NotNull PermissionDefault permissionDefault) {
    Permission permission = Bukkit.getPluginManager().getPermission(permissionName);
    if (permission == null) {
      permission = new Permission(permissionName, permissionDefault);
      Bukkit.getPluginManager().addPermission(permission);
    }
    permission.setDefault(permissionDefault);
    permission.recalculatePermissibles();
    return permission;
  }

  public static void addParent(@NotNull String permissionName, @NotNull UserRank rank) {
    addParent(permissionName, rank.getPermission());
  }

  public static void addParent(@NotNull String permissionName, @NotNull String parentName) {
    addParent(permissionName, parentName, PermissionDefault.FALSE);
  }

  public static void addParent(
      @NotNull String permissionName,
      @NotNull String parentName,
      @NotNull PermissionDefault permissionDefault) {
    Permission permission = getOrCreate(permissionName, permissionDefault);
    permission.addParent(parentName, true).recalculatePermissibles();
  }

  public static boolean hasPermission(UUID uuid, String permission) {
    RegisteredServiceProvider<LuckPerms> registration =
        Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if (registration == null) {
      return false;
    }

    LuckPerms luckPerms = registration.getProvider();
    boolean loadedUser = false;
    User user = luckPerms.getUserManager().getUser(uuid);

    if (user == null && !Bukkit.isPrimaryThread()) {
      // Load offline user if necessary.
      loadedUser = true;
      user = loadPermissionData(uuid);
    }

    if (user == null) {
      // User could not be loaded.
      return false;
    }

    CachedDataManager userData = user.getCachedData();
    CachedPermissionData permissionData =
        userData.getPermissionData(QueryOptions.defaultContextualOptions());
    boolean hasPermission = permissionData.checkPermission(permission).asBoolean();

    if (loadedUser) {
      // Clean up loaded user.
      releasePermissionData(uuid);
    }

    return hasPermission;
  }

  public static @Nullable User loadPermissionData(UUID uuid) {
    if (Bukkit.isPrimaryThread()) {
      ReportableEvent.call("Loading permission data on main thread", new Throwable(), 5);
    }

    RegisteredServiceProvider<LuckPerms> registration =
        Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if (registration == null) {
      return null;
    }

    LuckPerms luckPerms = registration.getProvider();
    User user = luckPerms.getUserManager().getUser(uuid);

    if (user != null) {
      return null;
    }

    return luckPerms.getUserManager().loadUser(uuid).join();
  }

  public static void releasePermissionData(UUID uuid) {
    Player player = Bukkit.getPlayer(uuid);
    if (player != null) {
      // Player is online, do not release data
      return;
    }

    RegisteredServiceProvider<LuckPerms> registration =
        Bukkit.getServicesManager().getRegistration(LuckPerms.class);
    if (registration == null) {
      return;
    }

    LuckPerms luckPerms = registration.getProvider();
    User user = luckPerms.getUserManager().getUser(uuid);

    if (user != null) {
      luckPerms.getUserManager().cleanupUser(user);
    }
  }
}
