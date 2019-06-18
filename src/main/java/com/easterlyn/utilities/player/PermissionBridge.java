package com.easterlyn.utilities.player;

import com.easterlyn.utilities.TextUtils;
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

/**
 * Bridge for permissions via LuckPerms. Used for cases where a player is not online.
 *
 * @author Jikoo
 */
public class PermissionBridge {

	private PermissionBridge() {}

	public static void loadPermissionData(UUID uuid) {
		if (Bukkit.isPrimaryThread()) {
			TextUtils.getTrace(new Throwable("Loading permission data on main thread"), 5);
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
		boolean hasPermission = permissionData.getPermissionValue(permission).asBoolean();

		if (loadedUser) {
			// Clean up loaded user.
			luckPermsApi.cleanupUser(user);
		}

		return hasPermission;
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
