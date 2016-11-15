package co.sblock.utilities;

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
		Permission permission = Bukkit.getPluginManager().getPermission(permissionName);
		if (permission == null) {
			permission = new Permission(permissionName, permissionDefault);
			Bukkit.getPluginManager().addPermission(permission);
		} else {
			permission.setDefault(permissionDefault);
		}
		return permission;
	}

	public static void addParent(String permissionName, String parentName) {
		Permission permission = getOrCreate(permissionName, PermissionDefault.OP);
		permission.addParent(parentName, true).recalculatePermissibles();;
	}

}
