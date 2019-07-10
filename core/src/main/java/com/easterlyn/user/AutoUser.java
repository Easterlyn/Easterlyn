package com.easterlyn.user;

import com.easterlyn.EasterlynCore;
import com.easterlyn.util.Colors;
import com.easterlyn.util.GenericUtil;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.wrapper.ConcurrentConfiguration;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AutoUser extends User {

	private final Plugin plugin;

	public AutoUser(@NotNull EasterlynCore core, @NotNull Plugin plugin) {
		super(core, new UUID(0, 0), new ConcurrentConfiguration());
		this.plugin = plugin;
	}

	@Nullable
	public Player getPlayer() {
		return null;
	}

	@NotNull
	public String getDisplayName() {
		return ChatColor.translateAlternateColorCodes('&',
				GenericUtil.orDefault(plugin.getConfig().getString("auto_user.name"), "Auto User"));
	}

	@NotNull
	public ChatColor getColor() {
		return Colors.getOrDefault(plugin.getConfig().getString("auto_user.color"), getRank().getColor());
	}

	public boolean isOnline() {
		return false;
	}

	public boolean hasPermission(String permission) {
		Permission perm = getPlugin().getServer().getPluginManager().getPermission(permission);
		return perm == null || perm.getDefault() == PermissionDefault.TRUE || perm.getDefault() == PermissionDefault.OP;
	}

	@NotNull
	public UserRank getRank() {
		return UserRank.ADMIN;
	}

	public TextComponent getMention() {
		TextComponent component = new TextComponent("@" + getDisplayName());
		component.setColor(getColor());

		String click = plugin.getConfig().getString("auto_user.click");
		if (click != null && !click.isEmpty()) {
			component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, click));
		}

		String hover = plugin.getConfig().getString("auto_user.hover");
		if (hover != null && !hover.isEmpty()) {
			hover = ChatColor.translateAlternateColorCodes('&', hover);
			component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, StringUtil.fromLegacyText(hover).toArray(new TextComponent[0])));
		}

		return component;
	}

	public void sendMessage(@NotNull String message) {
		// TODO should send to console
	}

	public void sendMessage(@NotNull BaseComponent... components) {
		// ^
	}

	void save() {}

}
