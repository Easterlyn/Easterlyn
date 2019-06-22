package com.easterlyn.chat.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.Easterlyn;
import com.easterlyn.chat.AccessLevel;
import com.easterlyn.chat.EasterlynChat;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.channel.NormalChannel;
import com.easterlyn.users.User;
import com.easterlyn.util.command.AddRemove;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
			String message = user.getDisplayName() + " joined " + channel.getDisplayName() + " at " + timestamp.format(new Date());
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

		String message = user.getDisplayName() + " quit " + channel.getDisplayName() + " at " + timestamp.format(new Date());
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

		String message = user.getDisplayName() + (add ? " added " : " removed ") + target.getDisplayName()
				+ (add ? " to" : " from") + " the whitelist in " + channel.getDisplayName() + " at " + timestamp.format(new Date());
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

		String message = user.getDisplayName() + (add ? " added " : " removed ") + target.getDisplayName()
				+ (add ? " to" : " from") + " the mod list in " + channel.getDisplayName() + " at " + timestamp.format(new Date());
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

		String message = user.getDisplayName() + (add ? " banned " : " unbanned ") + target.getDisplayName()
				+ " from " + channel.getDisplayName() + " at " + timestamp.format(new Date());
		channel.setBanned(target, add);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});

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
		String message = user.getDisplayName() + " set " + channel.getDisplayName() + " to access level "
				+ accessLevel.name() + " at " + timestamp.format(new Date());
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

		String message;
		if (password == null) {
			message = user.getDisplayName() + " removed the password from " + channel.getDisplayName() + " at "
					+ timestamp.format(new Date());
		} else {
			message = user.getDisplayName() + " set the password to " + password + " in " + channel.getDisplayName() + " at "
					+ timestamp.format(new Date());
		}
		channel.setPassword(password);
		channel.getMembers().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				player.sendMessage(message);
			}
		});
	}

	@Subcommand("delete")
	@Description("DELET CHANEL")
	@Syntax("<channel>")
	public void disband(@Flags("self") User user, @Flags("other") NormalChannel channel, @Optional String name) {
		if (!channel.isOwner(user)) {
			user.sendMessage("Buddy, that's not your channel. Ask the channel owner to make changes.");
			return;
		}

		if (!channel.getDisplayName().equals(name)) {
			user.sendMessage("Please include the channel name again to confirm\nI.e. /channel delete #main #main");
			return;
		}

		chat.getChannels().remove(channel.getName());

		String message = channel.getDisplayName() + " has been disbanded. That's all, folks!";
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
