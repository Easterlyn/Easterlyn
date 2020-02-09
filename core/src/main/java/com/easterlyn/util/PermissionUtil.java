package com.easterlyn.util;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import me.lucko.luckperms.api.caching.UserData;
import me.lucko.luckperms.api.context.ContextSet;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;

/**
 * Permission-related utility.
 *
 * @author Jikoo
 */
public class PermissionUtil {

	private PermissionUtil() {}

	@NotNull
	public static Permission getOrCreate(@NotNull String permissionName, @NotNull PermissionDefault permissionDefault) {
		Permission permission = Bukkit.getPluginManager().getPermission(permissionName);
		if (permission ==  null) {
			permission = new Permission(permissionName, permissionDefault);
			Bukkit.getPluginManager().addPermission(permission);
		}
		permission.setDefault(permissionDefault);
		permission.recalculatePermissibles();
		return permission;
	}

	public static void addParent(@NotNull String permissionName, @NotNull String parentName) {
		addParent(permissionName, parentName, PermissionDefault.FALSE);
	}

	public static void addParent(@NotNull String permissionName, @NotNull String parentName, @NotNull PermissionDefault permissionDefault) {
		Permission permission = getOrCreate(permissionName, permissionDefault);
		permission.addParent(parentName, true).recalculatePermissibles();
	}

	public static void loadPermissionData(UUID uuid) {
		if (Bukkit.isPrimaryThread()) {
			// TODO error is not thrown anywhere
			StringUtil.getTrace(new Throwable("Loading permission data on main thread"), 5);
		}

		Optional<LuckPermsApi> apiOptional = LuckPerms.getApiSafe();
		if (!apiOptional.isPresent()) {
			return;
		}

		LuckPermsApi luckPermsApi = apiOptional.get();
		User user = luckPermsApi.getUser(uuid);

		if (user != null) {
			return;
		}

		luckPermsApi.getStorage().loadUser(uuid).join();
	}

	public static boolean hasPermission(UUID uuid, String permission) {
		return hasAnyPermission(uuid, Collections.singleton(permission));
	}

	public static boolean hasAnyPermission(UUID uuid, Collection<String> permissions) {

		Optional<LuckPermsApi> apiOptional = LuckPerms.getApiSafe();
		if (!apiOptional.isPresent()) {
			return false;
		}

		LuckPermsApi luckPermsApi = apiOptional.get();
		boolean loadedUser = false;
		User user = luckPermsApi.getUser(uuid);

		if (user == null && !Bukkit.isPrimaryThread()) {
			// Load offline user if necessary.
			loadedUser = true;
			luckPermsApi.getStorage().loadUser(uuid).join();
			user = luckPermsApi.getUser(uuid);
		}

		if (user == null) {
			// User could not be loaded.
			return false;
		}

		UserData userData = user.getCachedData();
		PermissionData permissionData = userData.getPermissionData(Contexts.of(ContextSet.empty(), true, true, true, true, true, false));
		boolean hasPermission = false;
		for (String permission : permissions) {
			hasPermission = permissionData.getPermissionValue(permission).asBoolean();

			if (hasPermission) {
				break;
			}
		}

		if (loadedUser) {
			// Clean up loaded user.
			luckPermsApi.cleanupUser(user);
		}

		return hasPermission;
	}

	public static boolean hasAnyPermission(Permissible permissible, Collection<String> permissions) {
		for (String permissionName : permissions) {
			if (permissible.hasPermission(permissionName)) {
				return true;
			}

			if (permissible.isPermissionSet(permissionName)) {
				continue;
			}

			Permission permission = Bukkit.getPluginManager().getPermission(permissionName);

			switch (permission != null ? permission.getDefault() : PermissionDefault.OP) {
				case TRUE:
					return true;
				case OP:
					if (permissible.isOp()) {
						return true;
					}
					break;
				case NOT_OP:
					if (!permissible.isOp()) {
						return true;
					}
					break;
				case FALSE:
				default:
					break;
			}
		}

		return false;
	}

	public static void releasePermissionData(UUID uuid) {
		Optional<LuckPermsApi> apiOptional = LuckPerms.getApiSafe();
		if (!apiOptional.isPresent()) {
			return;
		}

		LuckPermsApi luckPermsApi = apiOptional.get();
		User user = luckPermsApi.getUser(uuid);

		if (user != null) {
			luckPermsApi.cleanupUser(user);
		}
	}

}
