package co.sblock.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.mojang.authlib.GameProfile;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
import co.sblock.utilities.player.PlayerLoader;

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

		Player senderPlayer;
		if (sender instanceof Player) {
			senderPlayer = (Player) sender;
		} else {
			senderPlayer = PlayerLoader.getFakePlayer(sender.getName());
		}
		GameProfile senderProfile = new GameProfile(senderPlayer.getUniqueId(), senderPlayer.getName());

		boolean isReply = label.equals("r") || label.equals("reply");

		Player recipientPlayer;
		GameProfile recipientProfile;
		if (isReply) {
			if (args.length == 0) {
				return false;
			}
			if (!reply.containsKey(senderProfile)) {
				sender.sendMessage(ChatColor.RED + "You do not have anyone to reply to!");
				return true;
			}
			recipientProfile = reply.get(senderProfile);
			OfflinePlayer reply = Bukkit.getOfflinePlayer(recipientProfile.getId());
			// Probably a real player.
			if (reply.hasPlayedBefore()) {
				// Ensure that they're online
				if (!reply.isOnline()) {
					sender.sendMessage(ChatColor.RED + "The person you were talking to has logged out!");
					return true;
				}
				recipientPlayer = reply.getPlayer();
			} else {
				// Reply was a fake player
				recipientPlayer = PlayerLoader.getFakePlayer(recipientProfile.getId(), recipientProfile.getName());
			}
		} else {
			if (args.length < 2) {
				return false;
			}
			if (args[0].equalsIgnoreCase("CONSOLE")) {
				recipientPlayer = PlayerLoader.getFakePlayer("CONSOLE");
			} else {
				List<Player> players = Bukkit.matchPlayer(args[0]);
				if (players.size() == 0) {
					sender.sendMessage(ChatColor.RED + "That player is not online!");
					return true;
				}
				recipientPlayer = players.get(0);
			}
			recipientProfile = new GameProfile(recipientPlayer.getUniqueId(), recipientPlayer.getName());
		}

		OfflineUser senderUser = Users.getGuaranteedUser(senderPlayer.getUniqueId());

		Message message = new MessageBuilder().setChannel(ChannelManager.getChannelManager().getChannel("#pm")).setSender(senderUser)
				.setMessage(recipientPlayer.getName() + ": " + StringUtils.join(args, ' ', isReply ? 0 : 1, args.length)).toMessage();

		Set<Player> players = new HashSet<Player>();
		players.add(recipientPlayer);
		players.add(senderPlayer);
		message.getChannel().getListening().forEach(uuid -> {
			Player player = Bukkit.getPlayer(uuid);
			if (player != null) {
				players.add(player);
			}
		});

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(false, (Player) sender, players, message);

		Bukkit.getPluginManager().callEvent(event);

		if (!event.isCancelled()) {
			reply.put(senderProfile, recipientProfile);
			reply.put(recipientProfile, senderProfile);
		}

		return true;
	}

}
