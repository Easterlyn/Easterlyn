package com.easterlyn.utilities;

import me.lucko.luckperms.api.LuckPermsApi;
import org.bukkit.Bukkit;

import java.util.UUID;

/**
 * Bridge for permissions via zPermissions. Used for cases where a player is not online.
 *
 * @author Jikoo
 */
public class PermissionBridge {

	private static PermissionBridge instance;

	private LuckPermsApi service;

	private PermissionBridge() {
		try {
			Class.forName("me.lucko.luckperms.api.LuckPermsApi.class");
			service = Bukkit.getServicesManager().load(LuckPermsApi.class);
		} catch (ClassNotFoundException e) {
			service = null;
		}
	}

	public boolean hasPermission(UUID uuid, String permission) {
		if (service == null) {
			return false;
		}
		return service.getUserSafe(uuid).map(user ->
				user.getPermissions().stream().anyMatch(node -> node.getPermission().equals(permission))
		).orElse(false);
	}

	public static PermissionBridge getInstance() {
		if (instance == null) {
			instance = new PermissionBridge();
		}
		return instance;
	}

}
