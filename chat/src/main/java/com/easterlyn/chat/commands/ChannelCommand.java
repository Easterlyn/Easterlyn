package com.easterlyn.chat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.Easterlyn;
import com.easterlyn.EasterlynChat;
import com.easterlyn.chat.channel.AccessLevel;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NormalChannel;
import com.easterlyn.users.User;
import com.easterlyn.util.StringUtil;
import com.easterlyn.util.command.AddRemove;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

// TODO rich notify
@CommandAlias("channel")
@CommandPermission("easterlyn.command.channel")
public class ChannelCommand extends BaseCommand {

	private final SimpleDateFormat timestamp = new SimpleDateFormat("HH:mm");

	@Dependency
	private Easterlyn easterlyn;
	@Dependency
	private EasterlynChat chat;

	@CommandAlias("join|focus")
	@Description("Join or focus on a channel.")
	@Syntax("<channel> [password]")
	@CommandPermission("easterlyn.command.join")
	public void join(@Flags("self") User user, Channel channel, @Optional String password) {
		channel.updateLastAccess();
		List<String> channels = user.getStorage().getStringList(EasterlynChat.USER_CHANNELS);

		if (!channels.contains(channel.getName())) {
			if (!channel.isWhitelisted(user)) {
				if (channel.getPassword() == null || !channel.getPassword().equals(password)) {
					user.sendMessage("No can do, buckaroo! Invalid password. Alternately, get someone to manually approve you.");
					return;
				}
				channel.setWhitelisted(user, true);
			}
			channels.add(channel.getName());
			user.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
		}

		if (!channel.getMembers().contains(user.getUniqueId())) {
			channel.getMembers().add(user.getUniqueId());
			TextComponent[] message = StringUtil.fromLegacyText(user.getDisplayName() + " joined " + channel.getDisplayName()
					+ " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
			channel.getMembers().forEach(uuid -> {
				Player player = Bukkit.getPlayer(uuid);
				if (player != null) {
					player.sendMessage(message);
				}
			});
		}

		user.getStorage().set(EasterlynChat.USER_CURRENT, channel.getName());
	}

	@CommandAlias("leave")
	@Description("Leave a channel.")
	@Syntax("[channel]")
	@CommandPermission("easterlyn.command.leave")
	public void leave(@Flags("self") User user, Channel channel) {
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
		if (channel.getAccess() == AccessLevel.PRIVATE && channel.getPassword() != null) {
			// Our clubhouse requires the secret knock on every entry.
			channel.setWhitelisted(user, false);
		}

		TextComponent[] message = StringUtil.fromLegacyText(user.getDisplayName() + " quit " + channel.getDisplayName()
				+ " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});

		channel.getMembers().remove(user.getUniqueId());
	}

	@Subcommand("whitelist")
	@Description("Add or remove a user from the whitelist.")
	@Syntax("[channel] add|remove <user>")
	public void setWhitelisted(@Flags("self") User user, NormalChannel channel, AddRemove addRemove, @Flags("other") User target) {
		channel.updateLastAccess();
		if (!channel.isModerator(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask a channel mod to make changes.");
			return;
		}
		boolean currentlyWhitelisted = channel.isWhitelisted(target);
		boolean add = addRemove == AddRemove.ADD;
		if (add == currentlyWhitelisted) {
			user.sendMessage("Buddy, that's not a change.");
			return;
		}
		if (!add && channel.isModerator(target)) {
			user.sendMessage("Channel moderators cannot be removed from the whitelist.");
			return;
		}

		TextComponent[] message = StringUtil.fromLegacyText(user.getDisplayName() + (add ? " added " : " removed ")
				+ target.getDisplayName() + (add ? " to" : " from") + " the whitelist in " + channel.getDisplayName()
				+ " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
		channel.setWhitelisted(target, add);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	@Subcommand("moderator")
	@Description("Add or remove a channel moderator.")
	@Syntax("[channel] add|remove <user>")
	public void setModerator(@Flags("self") User user, NormalChannel channel, AddRemove addRemove, @Flags("other") User target) {
		channel.updateLastAccess();
		if (!channel.isOwner(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask the channel owner to make changes.");
			return;
		}
		boolean currentlyMod = channel.isModerator(target);
		boolean add = addRemove == AddRemove.ADD;
		if (add == currentlyMod) {
			user.sendMessage("Buddy, that's not a change.");
			return;
		}

		TextComponent[] message = StringUtil.fromLegacyText(user.getDisplayName() + (add ? " added " : " removed ")
				+ target.getDisplayName() + (add ? " to" : " from") + " the mod list in " + channel.getDisplayName()
				+ " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
		channel.setModerator(target, add);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	@Subcommand("ban")
	@Description("Ban a user from a channel.")
	@Syntax("[channel] add|remove <user>")
	public void setBanned(@Flags("self") User user, NormalChannel channel, AddRemove addRemove, @Flags("other") User target) {
		channel.updateLastAccess();
		if (!channel.isModerator(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask a channel mod to make changes.");
			return;
		}
		boolean currentlyBanned = channel.isBanned(target);
		boolean add = addRemove == AddRemove.ADD;
		if (add == currentlyBanned) {
			user.sendMessage("Buddy, that's not a change.");
			return;
		}
		if (!add && channel.isModerator(target)) {
			user.sendMessage("Channel moderators cannot be banned.");
			return;
		}

		TextComponent[] message = StringUtil.fromLegacyText(user.getDisplayName() + (add ? " banned " : " unbanned ")
				+ target.getDisplayName() + " from " + channel.getDisplayName() + " at " + timestamp.format(new Date())
		).toArray(new TextComponent[0]);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});

		channel.setBanned(target, add);
		List<String> channels = target.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
		channels.remove(channel.getName());
		// TODO if user was not in the channel, should they be notified?
		target.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
		String currentChannelName = target.getStorage().getString(EasterlynChat.USER_CURRENT);
		if (channel.getName().equals(currentChannelName) || currentChannelName == null) {
			if (channels.contains(EasterlynChat.DEFAULT.getName())) {
				target.getStorage().set(EasterlynChat.USER_CURRENT, EasterlynChat.DEFAULT.getName());
			}
		}
	}

	@Subcommand("modify access")
	@Description("Set a channel's access level.")
	@Syntax("[channel] <access>")
	public void setAccessLevel(@Flags("self") User user, NormalChannel channel, AccessLevel accessLevel) {
		channel.updateLastAccess();
		if (!channel.isOwner(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask the channel owner to make changes.");
			return;
		}

		if (channel.getAccess() == accessLevel) {
			user.sendMessage("Buddy, that's not a change.");
			return;
		}


		channel.setAccess(accessLevel);
		TextComponent[] message = StringUtil.fromLegacyText(user.getDisplayName() + " set " + channel.getDisplayName()
				+ " to access level " + accessLevel.name() + " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	@Subcommand("modify password")
	@Description("Set or remove a channel's password.")
	@Syntax("[channel] <cool password>")
	public void setPassword(@Flags("self") User user, NormalChannel channel, @Optional String password) {
		channel.updateLastAccess();
		if (!channel.isOwner(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask the channel owner to make changes.");
			return;
		}

		if (channel.getAccess() == AccessLevel.PUBLIC) {
			user.sendMessage("Buddy, that's a public channel. You gotta make it private to set a password.");
			return;
		}

		if (password == null || password.equalsIgnoreCase("off") || password.equalsIgnoreCase("null")
				|| password.equalsIgnoreCase("remove")) {
			password = null;
		}

		TextComponent[] message;
		if (password == null) {
			message = StringUtil.fromLegacyText(user.getDisplayName() + " removed the password from "
					+ channel.getDisplayName() + " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
		} else {
			message = StringUtil.fromLegacyText(user.getDisplayName() + " set the password to " + password
					+ " in " + channel.getDisplayName() + " at " + timestamp.format(new Date())).toArray(new TextComponent[0]);
		}
		channel.setPassword(password);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	@Subcommand("create")
	@Description("Create a new channel!")
	@Syntax("#<channelname>")
	@CommandPermission("easterlyn.command.channel.create")
	public void create(@Flags("self") User user, @Single String name) {
		if (!user.hasPermission("easterlyn.command.channel.create.anyname") && (name.length() < 2 || name.length() > 17
				|| name.charAt(0) != '#' || !StringUtil.stripNonAlphanumerics(name).equals(name.substring(1)))) {
			user.sendMessage("Invalid channel name. Valid channel names start with \"#\" and contain 1-16 alphabetical characters.");
			return;
		}

		if (name.length() > 1 && name.charAt(0) == '#') {
			name = name.substring(1);
		}

		if (chat.getChannels().containsKey(name)) {
			user.sendMessage("Channel already exists, sorry chum.");
			return;
		}

		chat.getChannels().put(name, new NormalChannel(name, user.getUniqueId()));
		Player player = user.getPlayer();
		user.sendMessage("Channel created! Manipulate it with `/channel modify");
		if (player != null) {
			player.chat("/channel join #" + name);
		}
	}

	@Subcommand("delete")
	@Description("DELET CHANEL")
	@Syntax("<channel>")
	public void delete(@Flags("self") User user, @Flags("other") NormalChannel channel, @Optional String name) {
		if (!channel.isOwner(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask the channel owner to make changes.");
			return;
		}

		if (!channel.getDisplayName().equals(name)) {
			user.sendMessage("Please include the channel name again to confirm.\nExample: `/channel delete #main #main`");
			return;
		}

		chat.getChannels().remove(channel.getName());

		TextComponent[] message = StringUtil.fromLegacyText(channel.getDisplayName()
				+ " has been disbanded. That's all, folks!").toArray(new TextComponent[0]);
		channel.getMembers().stream().map(uuid -> easterlyn.getUserManager().getUser(uuid)).forEach(member -> {
			member.sendMessage(message);
			List<String> channels = member.getStorage().getStringList(EasterlynChat.USER_CHANNELS);
			channels.remove(channel.getName());
			member.getStorage().set(EasterlynChat.USER_CHANNELS, channels);
			String currentChannelName = member.getStorage().getString(EasterlynChat.USER_CURRENT);
			if (channel.getName().equals(currentChannelName) || currentChannelName == null) {
				if (channels.contains(EasterlynChat.DEFAULT.getName())) {
					member.getStorage().set(EasterlynChat.USER_CURRENT, EasterlynChat.DEFAULT.getName());
				}
			}
		});
	}

	// TODO info commands: info, list, members, listening

}
