package co.sblock.utilities;

import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;

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
		if (permissions.containsKey(permission) && permissions.get(permission)) {
			return true;
		}
		PluginManager pluginManager = Bukkit.getPluginManager();
		for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
			Permission perm = pluginManager.getPermission(entry.getKey());
			if (perm == null) {
				continue;
			}
			if (perm.getChildren().containsKey(permission)) {
				// If parent is true, child must be true to be true
				// While technically functional, not false (== rather than &&)
				// allows potential for edge cases and mistakes.
				return entry.getValue() && perm.getChildren().get(permission);
			}
		}
		Permission node = pluginManager.getPermission(permission);
		if (node != null) {
			return node.getDefault() == PermissionDefault.TRUE;
		}
		return false;
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
		if (permissions.containsKey(permission) && permissions.get(permission)) {
			return true;
		}
		PluginManager pluginManager = Bukkit.getPluginManager();
		for (Map.Entry<String, Boolean> entry : permissions.entrySet()) {
			Permission perm = pluginManager.getPermission(entry.getKey());
			if (perm == null) {
				continue;
			}
			if (perm.getChildren().containsKey(permission)) {
				return entry.getValue() && perm.getChildren().get(permission);
			}
		}
		Permission node = pluginManager.getPermission(permission);
		if (node != null) {
			switch (node.getDefault()) {
			case NOT_OP:
				return !player.isOp();
			case OP:
				return player.isOp();
			case TRUE:
				return true;
			case FALSE:
			default:
				return false;
			}
		}
		return false;
	}

	public static PermissionBridge getInstance() {
		if (instance == null) {
			instance = new PermissionBridge();
		}
		return instance;
	}

}
