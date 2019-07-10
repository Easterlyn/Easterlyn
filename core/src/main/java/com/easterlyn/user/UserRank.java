package com.easterlyn.user;

import com.easterlyn.util.Colors;
import java.util.function.Supplier;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.permissions.PermissionDefault;

/**
 * Enum representing ranks.
 *
 * @author Jikoo
 */
public enum UserRank {

	MEMBER("member", PermissionDefault.TRUE, () -> Colors.RANK_MEMBER),
	RETIRED_STAFF("retired_staff", PermissionDefault.TRUE, () -> Colors.RANK_RETIRED_STAFF),
	STAFF("staff", () -> Colors.RANK_STAFF),
	MODERATOR("moderator", () -> Colors.RANK_MODERATOR),
	ADMIN("admin", () -> Colors.RANK_ADMIN),
	HEAD_ADMIN("head_admin", ADMIN.friendlyName, () -> Colors.RANK_HEAD_ADMIN),
	DANGER_DANGER_HIGH_VOLTAGE("ask.adam.before.touching", HEAD_ADMIN.friendlyName, PermissionDefault.FALSE, HEAD_ADMIN.colorSupplier);

	private final String friendlyName;
	private final PermissionDefault permissionDefault;
	private final String permission;
	private final Supplier<ChatColor> colorSupplier;

	UserRank(String internalName, Supplier<ChatColor> colorSupplier) {
		this(internalName, PermissionDefault.OP, colorSupplier);
	}

	UserRank(String internalName, String friendlyName, Supplier<ChatColor> colorSupplier) {
		this(internalName, friendlyName, PermissionDefault.OP, colorSupplier);
	}

	UserRank(String internalName, PermissionDefault permissionDefault, Supplier<ChatColor> colorSupplier) {
		this(internalName, Character.toUpperCase(internalName.charAt(0)) + internalName.substring(1), permissionDefault, colorSupplier);
	}

	UserRank(String internalName, String friendlyName, PermissionDefault permissionDefault, Supplier<ChatColor> colorSupplier) {
		this.friendlyName = friendlyName;
		this.permissionDefault = permissionDefault;
		this.permission = "easterlyn.group.%s" + internalName;
		this.colorSupplier = colorSupplier;
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

	public ChatColor getColor() {
		return colorSupplier.get();
	}

}
