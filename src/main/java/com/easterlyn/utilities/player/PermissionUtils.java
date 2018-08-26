package com.easterlyn.utilities.player;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

/**
 * Permission-related utility.
 *
 * @author Jikoo
 */
public class PermissionUtils {

	private PermissionUtils() {}

	public static Permission getOrCreate(String permissionName, PermissionDefault permissionDefault) {
		Permission permission;
		try {
			permission = new Permission(permissionName, permissionDefault);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (Exception e) {
			permission = Bukkit.getPluginManager().getPermission(permissionName);
			permission.setDefault(permissionDefault);
			permission.recalculatePermissibles();
		}
		return permission;
	}

	public static void addParent(String permissionName, String parentName) {
		addParent(permissionName, parentName, PermissionDefault.FALSE);
	}

	public static void addParent(String permissionName, String parentName, PermissionDefault permissionDefault) {
		Permission permission = getOrCreate(permissionName, permissionDefault);
		permission.addParent(parentName, true).recalculatePermissibles();
	}

}
