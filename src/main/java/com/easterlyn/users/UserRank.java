package com.easterlyn.users;

import com.easterlyn.chat.Language;

import org.bukkit.permissions.PermissionDefault;

import net.md_5.bungee.api.ChatColor;

/**
 * Enum representing ranks.
 * 
 * @author Jikoo
 */
public enum UserRank {

	DEFAULT("default", "Hero", PermissionDefault.TRUE),
	HERO("hero"),
	GODTIER("godtier"),
	DONATOR("donator"),
	HELPER("helper"),
	FELT("felt"),
	DENIZEN("denizen"),
	HORRORTERROR("horrorterror"),
	DANGER_DANGER_HIGH_VOLTAGE("ask.adam.before.touching", "Horrorterror", PermissionDefault.FALSE);

	private final String internalName, friendlyName;
	private final PermissionDefault permissionDefault;

	private UserRank(String internalName) {
		this(internalName, PermissionDefault.OP);
	}

	private UserRank(String internalName, PermissionDefault permissionDefault) {
		this(internalName, Character.toUpperCase(internalName.charAt(0)) + internalName.substring(1), permissionDefault);
	}

	private UserRank(String internalName, String friendlyName, PermissionDefault permissionDefault) {
		this.internalName = internalName;
		this.friendlyName = friendlyName;
		this.permissionDefault = permissionDefault;
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
		return String.format("sblock.group.%s", internalName);
	}

	public ChatColor getColor() {
		return Language.getColor(String.format("rank.%s", internalName));
	}

}
