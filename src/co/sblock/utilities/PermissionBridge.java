package co.sblock.utilities;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService;

/**
 * Bridge for permissions via zPermissions. Used for cases where a player is not online.
 * 
 * @author Jikoo
 */
public class PermissionBridge {

	private static PermissionBridge instance;

	private final ZPermissionsService service;

	private PermissionBridge() {
		service = Bukkit.getServicesManager().load(org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsService.class);
	}

	public boolean hasPermission(String group, String permission) {
		if (service == null) {
			return false;
		}
		if (!service.getAllGroups().contains(group)) {
			return false;
		}
		Map<String, Boolean> permissions = service.getGroupPermissions(null, null, group);
		return permissions.containsKey(permission) && permissions.get(permission);
	}

	public boolean hasPermission(UUID uuid, String permission) {
		if (service == null) {
			return false;
		}
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (!player.hasPlayedBefore()) {
			return false;
		}
		Map<String, Boolean> permissions = service.getPlayerPermissions(null, null, uuid);
		return permissions.containsKey(permission) && permissions.get(permission);
	}

	public static PermissionBridge getInstance() {
		if (instance == null) {
			instance = new PermissionBridge();
		}
		return instance;
	}

}
