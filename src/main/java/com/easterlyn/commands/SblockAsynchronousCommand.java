package com.easterlyn.commands;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.easterlyn.Easterlyn;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Base for commands that should be processed off the main thread.
 * 
 * @author Jikoo
 */
public abstract class SblockAsynchronousCommand extends SblockCommand {

	public SblockAsynchronousCommand(Easterlyn plugin, String name) {
		super(plugin, name);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		new BukkitRunnable() {
			@Override
			public void run() {
				SblockAsynchronousCommand.super.execute(sender, label, args);
			}
		}.runTaskAsynchronously(getPlugin());
		return true;
	}

	protected UUID getUniqueId(String name) {
		Future<List<Player>> future = Bukkit.getScheduler().callSyncMethod(getPlugin(), new Callable<List<Player>>() {
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
		if (player != null && player.hasPlayedBefore()) {
			return player.getUniqueId();
		}
		return null;
	}

}
