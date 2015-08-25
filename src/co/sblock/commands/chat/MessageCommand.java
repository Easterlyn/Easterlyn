package co.sblock.commands.chat;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import co.sblock.Sblock;
import co.sblock.chat.ChannelManager;
import co.sblock.chat.Color;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockCommand;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.WrappedSenderPlayer;

import net.md_5.bungee.api.ChatColor;

/**
 * Reimplementation of messaging.
 * 
 * @author Jikoo
 */
public class MessageCommand extends SblockCommand {

	private final HashMap<GameProfile, GameProfile> reply;

	public MessageCommand() {
		super("m");
		this.setDescription("Send a private message");
		this.setUsage("/m <name> <message> or /r <reply to last message>");
		this.setAliases("w", "t", "pm", "msg", "tell", "whisper", "r", "reply");
		reply = new HashMap<>();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		label = label.toLowerCase();

		Player senderPlayer = null;
		OfflineUser senderUser = null;
		GameProfile senderProfile;
		if (sender instanceof Player) {
			senderPlayer = (Player) sender;
			senderUser = Users.getGuaranteedUser(senderPlayer.getUniqueId());
			senderProfile = new GameProfile(senderPlayer.getUniqueId(), senderPlayer.getName());
		} else {
			senderProfile = Sblock.getInstance().getFakeGameProfile(sender.getName());
		}

		boolean isReply = label.equals("r") || label.equals("reply");

		Player recipientPlayer = null;
		GameProfile recipientProfile;
		if (isReply) {
			if (args.length == 0) {
				return false;
			}
			if (!reply.containsKey(senderProfile)) {
				sender.sendMessage(Color.BAD + "You do not have anyone to reply to!");
				return true;
			}
			recipientProfile = reply.get(senderProfile);
			OfflinePlayer reply = Bukkit.getOfflinePlayer(recipientProfile.getId());
			// Probably a real player.
			if (reply.hasPlayedBefore()) {
				// Ensure that they're online
				if (!reply.isOnline()) {
					sender.sendMessage(Color.BAD + "The person you were talking to has logged out!");
					return true;
				}
				recipientPlayer = reply.getPlayer();
			}
		} else {
			if (args.length < 2) {
				return false;
			}
			if (args[0].equalsIgnoreCase("CONSOLE")) {
				recipientProfile = Sblock.getInstance().getFakeGameProfile("CONSOLE");
			} else {
				List<Player> players = Bukkit.matchPlayer(args[0]);
				if (players.size() == 0) {
					sender.sendMessage(Color.BAD + "That player is not online!");
					return true;
				}
				recipientPlayer = players.get(0);
				recipientProfile = new GameProfile(recipientPlayer.getUniqueId(), recipientPlayer.getName());
			}
		}

		MessageBuilder builder = new MessageBuilder();
		builder.setChannel(ChannelManager.getChannelManager().getChannel("#pm"));
		builder.setMessage(ChatColor.WHITE + recipientProfile.getName() + ": "
				+ StringUtils.join(args, ' ', isReply ? 0 : 1, args.length));
		if (senderUser != null) {
			builder.setSender(senderUser);
		} else {
			builder.setSender(senderProfile.getName());
		}

		if (!builder.canBuild(true)) {
			return true;
		}

		Message message = builder.toMessage();

		Set<Player> players = new HashSet<Player>();
		if (senderPlayer != null) {
			players.add(senderPlayer);
		} else {
			senderPlayer = new WrappedSenderPlayer(sender);
		}
		if (recipientPlayer != null) {
			players.add(recipientPlayer);
		}
		message.getChannel().getListening().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				players.add(player);
			}
		});

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(false, senderPlayer, players, message);

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
