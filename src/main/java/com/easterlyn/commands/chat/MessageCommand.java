package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.ChannelManager;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.events.event.EasterlynAsyncChatEvent;
import com.easterlyn.users.User;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.player.WrappedSenderPlayer;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Reimplementation of messaging.
 *
 * @author Jikoo
 */
public class MessageCommand extends EasterlynCommand {

	private final Users users;
	private final ChannelManager manager;
	private final HashMap<GameProfile, GameProfile> reply;

	public MessageCommand(Easterlyn plugin) {
		super(plugin, "m");
		this.setAliases("w", "t", "pm", "msg", "tell", "whisper", "r", "reply");
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		this.reply = new HashMap<>();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		label = label.toLowerCase();

		Player senderPlayer = null;
		User senderUser = null;
		GameProfile senderProfile;
		if (sender instanceof Player) {
			senderPlayer = (Player) sender;
			senderUser = users.getUser(senderPlayer.getUniqueId());
			senderProfile = new GameProfile(senderPlayer.getUniqueId(), senderPlayer.getName());
		} else {
			senderProfile = ((Easterlyn) getPlugin()).getFakeGameProfile(sender.getName());
		}

		boolean isReply = label.equals("r") || label.equals("reply");

		Player recipientPlayer = null;
		GameProfile recipientProfile;
		if (isReply) {
			if (args.length == 0) {
				return false;
			}
			if (!reply.containsKey(senderProfile)) {
				sender.sendMessage(getLang().getValue("command.m.replyUnset"));
				return true;
			}
			recipientProfile = reply.get(senderProfile);
			OfflinePlayer reply = Bukkit.getOfflinePlayer(recipientProfile.getId());
			// Probably a real player.
			if (reply.hasPlayedBefore()) {
				// Ensure that they're online
				if (!reply.isOnline()) {
					sender.sendMessage(getLang().getValue("command.m.replyMissing"));
					return true;
				}
				recipientPlayer = reply.getPlayer();
			}
		} else {
			if (args.length < 2) {
				return false;
			}
			if (args[0].equalsIgnoreCase("CONSOLE")) {
				recipientProfile = ((Easterlyn) getPlugin()).getFakeGameProfile("CONSOLE");
			} else {
				List<Player> players = Bukkit.matchPlayer(args[0]);
				if (players.size() == 0) {
					sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
					return true;
				}
				recipientPlayer = players.get(0);
				recipientProfile = new GameProfile(recipientPlayer.getUniqueId(), recipientPlayer.getName());
			}
		}

		MessageBuilder builder = new MessageBuilder((Easterlyn) getPlugin())
				.setChannel(manager.getChannel("#pm")).setMessage(
						ChatColor.WHITE + recipientProfile.getName() + ": "
						+ TextUtils.join(args, ' ', isReply ? 0 : 1, args.length));
		if (senderUser != null) {
			builder.setSender(senderUser);
		} else {
			builder.setSender(senderProfile.getName());
		}

		if (builder.canNotBuild(true)) {
			return true;
		}

		if (senderPlayer == null) {
			senderPlayer = new WrappedSenderPlayer((Easterlyn) getPlugin(), sender);
		}

		EasterlynAsyncChatEvent event = new EasterlynAsyncChatEvent(false, senderPlayer, builder.toMessage());

		if (!(senderPlayer instanceof WrappedSenderPlayer)) {
			event.getRecipients().add(senderPlayer);
		}
		if (recipientPlayer != null) {
			event.getRecipients().add(recipientPlayer);
		}

		Bukkit.getPluginManager().callEvent(event);

		// If the event is not globally cancelled because the message was sent to a channel other than #pm,
		// the event was cancelled prior to being successfully sent. Probably a muted player trying to talk.
		if (event.isCancelled() && event.isGlobalCancelled()) {
			reply.put(senderProfile, recipientProfile);
			reply.put(recipientProfile, senderProfile);
		}

		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
		if (args.length == 1 && !(alias = alias.toLowerCase()).equals("r") && !alias.equals("reply")) {
			return super.tabComplete(sender, alias, args);
		}
		return ImmutableList.of();
	}

}
