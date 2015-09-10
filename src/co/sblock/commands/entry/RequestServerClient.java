package co.sblock.commands.entry;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

/**
 * 
 * 
 * @author Jikoo
 */
public class RequestServerClient extends SblockCommand {

	private final Map<UUID, Pair<UUID, Boolean>> pending;
	public RequestServerClient() {
		super("reqserver");
		this.setDescription("Accept an open request!");
		this.setUsage("/acceptrequest");
		this.setAliases("reqclient", "reqyes", "reqno");

		pending = new HashMap<>();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		label = label.toLowerCase();
		Player player= (Player) sender;

		if (label.equals("reqyes")) {
			reqYes(player);
			return true;
		}
		if (label.equals("reqno")) {
			reqNo(player);
			return true;
		}
		if (label.equals("reqserver")) {
			req(player, args, true);
			return true;
		}
		if (label.equals("reqclient")) {
			req(player, args, false);
			return true;
		}
		return true;
	}

	public void reqYes(Player player) {
		if (!pending.containsKey(player.getUniqueId())) {
			player.sendMessage(Color.BAD + "You should get someone to /reqserver or /reqclient before attempting to accept!");
			return;
		}
		Pair<UUID, Boolean> pair = pending.remove(player.getUniqueId());
		OfflineUser server = Users.getGuaranteedUser(pair.getRight() ? player.getUniqueId() : pair.getLeft());
		OfflineUser client = Users.getGuaranteedUser(pair.getRight() ? pair.getLeft() : player.getUniqueId());

		if (server.getClient() != null) {
			Users.getGuaranteedUser(server.getClient()).setServer(null);
		}
		server.setClient(client.getUUID());
		if (client.getServer() != null) {
			OfflineUser oldServer = Users.getGuaranteedUser(client.getServer());
			oldServer.setClient(null);
			if (oldServer.isOnline()) {
				oldServer.getOnlineUser().stopServerMode();
			}
		}
		client.setServer(server.getUUID());
		server.sendMessage(Color.GOOD_PLAYER + client.getPlayerName() + Color.GOOD + " is now your client!");
		client.sendMessage(Color.GOOD_PLAYER + server.getPlayerName() + Color.GOOD + " is now your server!");

		if (!server.isOnline()) {
			server.save();
		}
		if (!client.isOnline()) {
			client.save();
		}
	}

	public void reqNo(Player player) {
		if (!pending.containsKey(player.getUniqueId())) {
			player.sendMessage(Color.BAD + "You vigorously decline... no one.");
			return;
		}
		Pair<UUID, Boolean> pair = pending.remove(player.getUniqueId());
		player.sendMessage(Color.BAD + "Request declined!");
		Player declined = Bukkit.getPlayer(pair.getLeft());
		if (declined != null) {
			declined.sendMessage(Color.BAD_PLAYER + player.getName() + Color.BAD + " declined your request.");
		}
	}

	public void req(Player player, String[] args, boolean server) {
		if (args.length == 0) {
			player.sendMessage(Color.BAD + "Who ya gonna call?");
			return;
		}
		if (player.getName().equalsIgnoreCase(args[0])) {
			player.sendMessage(Color.BAD + "Playing with yourself can only entertain you for so long. Find a friend!");
			return;
		}
		List<Player> players = Bukkit.matchPlayer(args[0]);
		if (!players.isEmpty()) {
			player.sendMessage(Color.BAD + "No user by that name is logged in!");
			return;
		}
		Player target = players.get(0);

		if (pending.containsKey(target.getUniqueId())) {
			player.sendMessage(Color.BAD_PLAYER + target.getName() + Color.BAD
					+ " has a pending request to handle already!");
			return;
		}

		player.sendMessage(Color.GOOD + "Request sent to " + Color.GOOD_PLAYER + target.getName());
		pending.put(target.getUniqueId(), new ImmutablePair<UUID, Boolean>(target.getUniqueId(), server));
		target.sendMessage(Color.GOOD_PLAYER + player.getName() + Color.GOOD
				+ " has requested that you be their " + (server ? "server" : "client") + "!\n"
				+ Color.COMMAND + "/reqyes" + Color.GOOD + " or " + Color.COMMAND + "/reqno");
		return;
	}
}
