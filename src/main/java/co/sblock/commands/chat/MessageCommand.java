package co.sblock.commands.chat;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Chat;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.User;
import co.sblock.users.Users;
import co.sblock.utilities.WrappedSenderPlayer;

import net.md_5.bungee.api.ChatColor;

/**
 * Reimplementation of messaging.
 * 
 * @author Jikoo
 */
public class MessageCommand extends SblockCommand {

	private final Users users;
	private final ChannelManager manager;
	private final HashMap<GameProfile, GameProfile> reply;

	public MessageCommand(Sblock plugin) {
		super(plugin, "m");
		this.users = plugin.getModule(Users.class);
		this.manager = plugin.getModule(Chat.class).getChannelManager();
		this.reply = new HashMap<>();
		this.setAliases("w", "t", "pm", "msg", "tell", "whisper", "r", "reply");
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
			senderProfile = ((Sblock) getPlugin()).getFakeGameProfile(sender.getName());
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
				recipientProfile = ((Sblock) getPlugin()).getFakeGameProfile("CONSOLE");
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

		MessageBuilder builder = new MessageBuilder((Sblock) getPlugin())
				.setChannel(manager.getChannel("#pm")).setMessage(
						ChatColor.WHITE + recipientProfile.getName() + ": "
						+ StringUtils.join(args, ' ', isReply ? 0 : 1, args.length));
		if (senderUser != null) {
			builder.setSender(senderUser);
		} else {
			builder.setSender(senderProfile.getName());
		}

		if (!builder.canBuild(true)) {
			return true;
		}

		if (senderPlayer == null) {
			senderPlayer = new WrappedSenderPlayer((Sblock) getPlugin(), sender);
		}

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(false, senderPlayer, builder.toMessage());

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

}
