package co.sblock.commands;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.utilities.messages.Slack;

/**
 * Base for commands that should be processed off the main thread.
 * 
 * @author Jikoo
 */
public abstract class SblockAsynchronousCommand extends SblockCommand {

	public SblockAsynchronousCommand(String name) {
		super(name);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (getPermission() != null && !sender.hasPermission(getPermission())) {
			sender.sendMessage(getPermissionMessage());
			return true;
		}
		new BukkitRunnable() {
			@Override
			public void run() {
				try {
					if (!onCommand(sender, label, args)) {
						sender.sendMessage(getUsage());
					}
				} catch (Exception e) {
					sender.sendMessage(Color.BAD + "An error occurred processing this command. Please make sure your parameters are correct.");
					StringBuilder cause = new StringBuilder("\nTrace base: ");
					if (e.getMessage() != null) {
						cause.append(e.getMessage());
					} else {
						cause.append("null");
					}
					if (e.getCause() != null && e.getCause().getMessage() != null) {
						cause.append(", cause by: ").append(e.getCause().getMessage());
					}
					Slack.getInstance().postReport(sender.getName(), null,
							"Error processing command: " + StringUtils.join(args, ' ')
									+ cause.toString());
					e.printStackTrace();
				}
			}
		}.runTaskAsynchronously(Sblock.getInstance());
		return true;
	}

	protected UUID getUniqueId(String name) {
		Future<List<Player>> future = Bukkit.getScheduler().callSyncMethod(Sblock.getInstance(), new Callable<List<Player>>() {
			@Override
			public List<Player> call() throws Exception {
				return Bukkit.matchPlayer(name);
			}
		});
		List<Player> players;
		try {
			players = future.get();
		} catch (InterruptedException | ExecutionException e) {
			return null;
		}
		if (players.size() > 0) {
			return players.get(0).getUniqueId();
		}
		@SuppressWarnings("deprecation")
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		if (player.hasPlayedBefore()) {
			return player.getUniqueId();
		}
		return null;
	}
}
