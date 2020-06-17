package com.easterlyn.chat.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynChat;
import com.easterlyn.EasterlynCore;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NormalChannel;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.command.AddRemove;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@CommandAlias("channel")
@Description("{@@chat.commands.channel.description}")
@CommandPermission("easterlyn.command.channel")
public class ChannelCommand extends BaseCommand {

	private final SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm");

	@Dependency
	private EasterlynCore core;
	@Dependency
	private EasterlynChat chat;

	@CommandAlias("join")
	@Description("{@@chat.commands.channel.join.description}")
	@CommandPermission("easterlyn.command.join")
	@Syntax("<#channel> [password]")
	@CommandCompletion("@channelsJoinable @password")
	public void join(@Flags(CoreContexts.SELF) User user, @Flags(ChannelFlag.VISIBLE) Channel channel,
			@Optional String password) {
		channel.updateLastAccess();
		List<String> channels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);

		if (!channels.contains(channel.getName())) {
			if (!channel.isWhitelisted(user)) {
				if (channel.getPassword() == null || !channel.getPassword().equals(password)) {
					core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.join.error.password");
					return;
				}
				channel.setWhitelisted(user, true);
			}
			channels.add(channel.getName());
			user.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
		}

		user.getStorage().set(EasterlynChat.USER_CURRENT, channel.getName());

		if (channel.getMembers().contains(user.getUniqueId())) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.focus.success",
					"{value}", channel.getDisplayName());
			return;
		}

		channel.getMembers().add(user.getUniqueId());
		String time = timestamp.format(new Date());
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(user.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue("chat.common.join", locale))) {
				component.addExtra(textComponent);
			}
			component.addExtra(channel.getMention());
			for (TextComponent textComponent : StringUtil.toJSON(' ' + core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});
	}

	@Private
	@CommandAlias("focus")
	@Description("{@@chat.commands.channel.focus.description}")
	@CommandPermission("easterlyn.command.join")
	@Syntax("<#channel> [password]")
	@CommandCompletion("@channelsListening")
	public void focus(@Flags(CoreContexts.SELF) User user, @Flags(ChannelFlag.VISIBLE) Channel channel,
			@Optional String password) {
		join(user, channel, password);
	}

	@CommandAlias("leave")
	@Description("{@@chat.commands.channel.leave.description}")
	@CommandPermission("easterlyn.command.leave")
	@Syntax("<#channel>")
	@CommandCompletion("@channelsListening")
	public void leave(@Flags(CoreContexts.SELF) User user, @Flags(ChannelFlag.LISTENING_OR_CURRENT) Channel channel) {
		channel.updateLastAccess();
		List<String> channels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
		String currentChannelName = user.getStorage().getString(EasterlynChat.USER_CURRENT);
		if (channel.getName().equals(currentChannelName) || currentChannelName == null) {
			if (channels.contains(EasterlynChat.DEFAULT.getName())) {
				user.getStorage().set(EasterlynChat.USER_CURRENT, EasterlynChat.DEFAULT.getName());
			}
		}

		if (!channels.remove(channel.getName())) {
			return;
		}

		user.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
		if (channel.isPrivate() && channel.getPassword() != null) {
			// Our clubhouse requires the secret knock on every entry.
			channel.setWhitelisted(user, false);
		}

		String time = timestamp.format(new Date());
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(user.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue("chat.common.quit", locale))) {
				component.addExtra(textComponent);
			}
			component.addExtra(channel.getMention());
			for (TextComponent textComponent : StringUtil.toJSON(' ' + core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});

		channel.getMembers().remove(user.getUniqueId());
	}

	@Subcommand("whitelist")
	@Description("{@@chat.commands.channel.whitelist.description}")
	@Syntax("[#channel] <add|remove> <target>")
	@CommandCompletion("@boolean @player")
	public void setWhitelisted(@Flags(CoreContexts.SELF) User user,
			@Flags(ChannelFlag.VISIBLE_OR_CURRENT) NormalChannel channel,
			AddRemove addRemove, @Flags(CoreContexts.OFFLINE) User target) {
		channel.updateLastAccess();
		if (!channel.isModerator(user)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.requires_mod");
			return;
		}
		boolean currentlyWhitelisted = channel.isWhitelisted(target);
		boolean add = addRemove == AddRemove.ADD;
		if (add == currentlyWhitelisted) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.no_change");
			return;
		}
		if (!add && channel.isModerator(target)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.whitelist.error.moderator");
			return;
		}

		channel.setWhitelisted(target, add);
		if (!add && channel.getMembers().contains(target.getUniqueId())) {
			leave(user, channel);
		}

		String time = timestamp.format(new Date());
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(target.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue(
					"chat.commands.channel.whitelist." + (add ? "add" : "remove"), locale))) {
				component.addExtra(textComponent);
			}
			component.addExtra(channel.getMention());
			for (TextComponent textComponent : StringUtil.toJSON(' ' + core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});
	}

	@Subcommand("moderator")
	@Description("{@@chat.commands.channel.moderator.description}")
	@Syntax("[#channel] <add|remove> <target>")
	@CommandCompletion("@boolean @player")
	public void setModerator(@Flags(CoreContexts.SELF) User user,
			@Flags(ChannelFlag.VISIBLE_OR_CURRENT) NormalChannel channel,
			AddRemove addRemove, @Flags(CoreContexts.OFFLINE) User target) {
		channel.updateLastAccess();
		if (!channel.isOwner(user)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.requires_owner");
			return;
		}
		boolean currentlyMod = channel.isModerator(target);
		boolean add = addRemove == AddRemove.ADD;
		if (add == currentlyMod) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.no_change");
			return;
		}

		channel.setModerator(target, add);

		String time = timestamp.format(new Date());
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(target.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue(
					"chat.commands.channel.moderator." + (add ? "add" : "remove"), locale))) {
				component.addExtra(textComponent);
			}
			component.addExtra(channel.getMention());
			for (TextComponent textComponent : StringUtil.toJSON(' ' + core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});
	}

	@Subcommand("ban")
	@Description("{@@chat.commands.ban.description}")
	@Syntax("[#channel] <add|remove> <target>")
	@CommandCompletion("@boolean @player")
	public void setBanned(@Flags(CoreContexts.SELF) User user,
			@Flags(ChannelFlag.VISIBLE_OR_CURRENT) NormalChannel channel,
			AddRemove addRemove, @Flags(CoreContexts.OFFLINE) User target) {
		channel.updateLastAccess();
		if (!channel.isModerator(user)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.requires_mod");
			return;
		}
		boolean currentlyBanned = channel.isBanned(target);
		boolean add = addRemove == AddRemove.ADD;
		if (add == currentlyBanned) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.no_change");
			return;
		}
		if (!add && channel.isModerator(target)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.error.moderator");
			return;
		}

		String time = timestamp.format(new Date());
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(target.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue(
					"chat.commands.channel.ban." + (add ? "add" : "remove"), locale))) {
				component.addExtra(textComponent);
			}
			component.addExtra(channel.getMention());
			for (TextComponent textComponent : StringUtil.toJSON(' ' + core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});

		channel.setBanned(target, add);
		List<String> channels = target.getStorage().getStringList(EasterlynChat.USER_CHANNELS);

		if (channel.getMembers().contains(target.getUniqueId())) {
			leave(target, channel);
		} else {
			if (channels.remove(channel.getName())) {
				target.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
			}
		}

		String currentChannelName = target.getStorage().getString(EasterlynChat.USER_CURRENT);
		if (channel.getName().equals(currentChannelName) || currentChannelName == null) {
			if (channels.contains(EasterlynChat.DEFAULT.getName())) {
				target.getStorage().set(EasterlynChat.USER_CURRENT, EasterlynChat.DEFAULT.getName());
			}
		}
	}

	@Subcommand("modify private")
	@Description("{@@chat.commands.channel.modify.private.description}")
	@Syntax("[#channel] <private>")
	@CommandCompletion("@boolean")
	public void setAccessLevel(@Flags(CoreContexts.SELF) User user,
			@Flags(ChannelFlag.VISIBLE_OR_CURRENT) NormalChannel channel, boolean isPrivate) {
		channel.updateLastAccess();
		if (!channel.isOwner(user)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.requires_owner");
			return;
		}

		if (channel.isPrivate() == isPrivate) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.no_change");
			return;
		}

		channel.setPrivate(isPrivate);

		String time = timestamp.format(new Date());
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(channel.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue(
					"chat.commands.channel.modify.private." + (isPrivate ? "set" : "unset"), locale))) {
				component.addExtra(textComponent);
			}
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});
	}

	@Subcommand("modify password")
	@Description("{@@chat.commands.channel.modify.password.description}")
	@Syntax("[#channel] <private>")
	@CommandCompletion("@password")
	public void setPassword(@Flags(CoreContexts.SELF) User user,
			@Flags(ChannelFlag.VISIBLE_OR_CURRENT) NormalChannel channel, @Optional String password) {
		channel.updateLastAccess();
		if (!channel.isOwner(user)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.requires_owner");
			return;
		}

		if (!channel.isPrivate()) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.modify.password.error.public");
			return;
		}

		if (password == null || password.isEmpty() || password.equalsIgnoreCase("off")
				|| password.equalsIgnoreCase("null") || password.equalsIgnoreCase("remove")) {
			password = null;
		}

		String time = timestamp.format(new Date());
		boolean hasPassword = password == null;
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(user.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue(
					"chat.commands.channel.modify.password." + (hasPassword ? "set" : "unset"), locale))) {
				component.addExtra(textComponent);
			}
			component.addExtra(channel.getMention());
			for (TextComponent textComponent : StringUtil.toJSON(' ' + core.getLocaleManager().getValue("chat.common.at", locale, "{time}", time))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});
	}

	@Subcommand("create")
	@Description("{@@chat.commands.channel.create.description}")
	@Syntax("<#channelname> <private>")
	@CommandCompletion("channelname")
	@CommandPermission("easterlyn.command.channel.create")
	public void create(@Flags(CoreContexts.SELF) User user, @Single String name) {
		if (!user.hasPermission("easterlyn.command.channel.createany") && (name.length() < 2 || name.length() > 17
				|| name.charAt(0) != '#' || !StringUtil.stripNonAlphanumerics(name).equals(name.substring(1)))) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.create.error.naming_conventions");
			return;
		}

		if (name.length() > 1 && name.charAt(0) == '#') {
			name = name.substring(1);
		}

		if (chat.getChannels().containsKey(name)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.create.error.duplicate");
			return;
		}

		NormalChannel channel = new NormalChannel(name, user.getUniqueId());
		chat.getChannels().put(name.toLowerCase(), channel);
		Player player = user.getPlayer();
		core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.create.success");
		if (player != null) {
			join(user, channel, channel.getPassword());
		}
	}

	@Subcommand("delete")
	@Description("{@@chat.commands.channel.delete.description}")
	@Syntax("<#channel>")
	@CommandCompletion("@channelsOwned")
	public void delete(@Flags(CoreContexts.SELF) User user, @Flags(ChannelFlag.VISIBLE_OR_CURRENT) NormalChannel channel,
			@Optional String name) {
		if (!channel.isOwner(user)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.common.requires_owner");
			return;
		}

		if (!channel.getDisplayName().equals(name)) {
			core.getLocaleManager().sendMessage(user.getPlayer(), "chat.commands.channel.delete.error.confirm");
			return;
		}

		chat.getChannels().remove(channel.getName());

		channel.getMembers().stream().map(uuid -> core.getUserManager().getUser(uuid)).forEach(member -> {
			List<String> channels = member.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
			channels.remove(channel.getName());
			member.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
			String currentChannelName = member.getStorage().getString(EasterlynChat.USER_CURRENT);
			if (channel.getName().equals(currentChannelName) || currentChannelName == null) {
				if (channels.contains(EasterlynChat.DEFAULT.getName())) {
					member.getStorage().set(EasterlynChat.USER_CURRENT, EasterlynChat.DEFAULT.getName());
				}
			}

			Player player = member.getPlayer();
			if (player == null) {
				return;
			}

			BaseComponent component = new TextComponent();
			component.addExtra(channel.getMention());
			String locale = core.getLocaleManager().getLocale(player);
			for (TextComponent textComponent : StringUtil.toJSON(core.getLocaleManager().getValue(
					"chat.commands.channel.delete.success", locale))) {
				component.addExtra(textComponent);
			}

			player.sendMessage(component);
		});
	}

	// TODO info command: info, list, members, listening

}
