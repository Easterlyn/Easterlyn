package com.easterlyn.users;

import org.bukkit.permissions.PermissionDefault;

/**
 * Enum representing ranks.
 *
 * @author Jikoo
 */
public enum UserRank {

	MEMBER("member", PermissionDefault.TRUE),
	STAFF("staff"),
	MODERATOR("moderator"),
	ADMIN("admin"),
	HEAD_ADMIN("head_admin", ADMIN.getFriendlyName()),
	DANGER_DANGER_HIGH_VOLTAGE("ask.adam.before.touching", HEAD_ADMIN.getFriendlyName(), PermissionDefault.FALSE);

	private final String internalName, friendlyName;
	private final PermissionDefault permissionDefault;
	private final String permission;

	UserRank(String internalName) {
		this(internalName, PermissionDefault.OP);
	}

	UserRank(String internalName, String friendlyName) {
		this(internalName, friendlyName, PermissionDefault.OP);
	}

	UserRank(String internalName, PermissionDefault permissionDefault) {
		this(internalName, Character.toUpperCase(internalName.charAt(0)) + internalName.substring(1), permissionDefault);
	}

	UserRank(String internalName, String friendlyName, PermissionDefault permissionDefault) {
		this.internalName = internalName;
		this.friendlyName = friendlyName;
		this.permissionDefault = permissionDefault;
		this.permission = String.format("easterlyn.group.%s", internalName);
	}

	public String getLowercaseName() {
		return internalName;
	}

	public String getFriendlyName() {
		return this.friendlyName;
	}

	public PermissionDefault getPermissionDefault() {
		return permissionDefault;
	}

	public String getPermission() {
		return permission;
	}

}
