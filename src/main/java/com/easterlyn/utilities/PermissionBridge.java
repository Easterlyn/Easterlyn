package com.easterlyn.utilities;

import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.Contexts;
import me.lucko.luckperms.api.LuckPermsApi;
import me.lucko.luckperms.api.User;
import me.lucko.luckperms.api.caching.PermissionData;
import me.lucko.luckperms.api.caching.UserData;

import java.util.Optional;
import java.util.UUID;

/**
 * Bridge for permissions via zPermissions. Used for cases where a player is not online.
 *
 * @author Jikoo
 */
public class PermissionBridge {

	private static PermissionBridge instance;

	private PermissionBridge() {}

	public boolean hasPermission(UUID uuid, String name, String permission) {

		Optional<LuckPermsApi> apiOptional = LuckPerms.getApiSafe();
		if (!apiOptional.isPresent()) {
			return false;
		}

		LuckPermsApi luckPermsApi = apiOptional.get();
		boolean loadedUser = false;
		User user = luckPermsApi.getUser(uuid);

		if (user == null) {
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
		PermissionData permissionData = userData.getPermissionData(Contexts.allowAll());
		boolean hasPermission = permissionData.getPermissionValue(permission).asBoolean();

		if (loadedUser) {
			// Clean up loaded user.
			luckPermsApi.cleanupUser(user);
		}

		return hasPermission;
	}

	public static PermissionBridge getInstance() {
		if (instance == null) {
			instance = new PermissionBridge();
		}
		return instance;
	}

}
