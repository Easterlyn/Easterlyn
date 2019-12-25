package com.easterlyn.utilities.player;

import com.easterlyn.utilities.TextUtils;
import java.util.UUID;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.cacheddata.CachedDataManager;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

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

		RegisteredServiceProvider<LuckPerms> registration = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (registration == null) {
			return;
		}

		LuckPerms luckPerms = registration.getProvider();
		User user = luckPerms.getUserManager().getUser(uuid);

		if (user != null) {
			return;
		}

		luckPerms.getUserManager().loadUser(uuid).join();
	}

	public static boolean hasPermission(UUID uuid, String permission) {

		RegisteredServiceProvider<LuckPerms> registration = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
		if (registration == null) {
			return false;
		}

		LuckPerms luckPerms = registration.getProvider();
		boolean loadedUser = false;
		User user = luckPerms.getUserManager().getUser(uuid);

		if (user == null && !Bukkit.isPrimaryThread()) {
			// Load offline user if necessary.
			loadedUser = true;
			user = luckPerms.getUserManager().loadUser(uuid).join();
		}

		if (user == null) {
			// User could not be loaded.
			return false;
		}

		CachedDataManager userData = user.getCachedData();
		CachedPermissionData permissionData = userData.getPermissionData(QueryOptions.defaultContextualOptions());
		boolean hasPermission = permissionData.checkPermission(permission).asBoolean();

		if (loadedUser) {
			// Clean up loaded user.
			luckPerms.getUserManager().cleanupUser(user);
		}

		return hasPermission;
	}

	public static void releasePermissionData(UUID uuid) {
		RegisteredServiceProvider<LuckPerms> registration = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
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
