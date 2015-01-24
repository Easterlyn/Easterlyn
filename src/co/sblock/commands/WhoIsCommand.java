package co.sblock.commands;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.users.Users;

/**
 * SblockCommand for checking a User's stored data.
 * 
 * @author Jikoo
 */
public class WhoIsCommand extends SblockCommand {

	public WhoIsCommand() {
		super("whois");
		this.setAliases("profile");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <player>");
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
			return true;
		}
		final UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
		final OfflinePlayer targetPlayer;
		final String target = args.length > 0 ? args[0] : sender.getName();
		List<Player> players = Bukkit.matchPlayer(target);
		if (players.size() > 0) {
			targetPlayer = players.get(0);
		} else {
			targetPlayer = null;
		}
		new BukkitRunnable() {
			@SuppressWarnings("deprecation")
			@Override
			public void run() {
				final OfflinePlayer player;
				if (targetPlayer == null) {
					player = Bukkit.getOfflinePlayer(args[0]);
				} else {
					player = targetPlayer;
				}
				if (uuid != null && Bukkit.getPlayer(uuid) == null) {
					// Sender is no longer online
					return;
				}
				if (player == null || !player.hasPlayedBefore()) {
					sender.sendMessage(ChatColor.GOLD + target + ChatColor.RED + " has never played on this server.");
					return;
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						if (sender.hasPermission("sblock.felt")) {
							sender.sendMessage(Users.getGuaranteedUser(player.getUniqueId()).getWhois());
						} else {
							sender.sendMessage(Users.getGuaranteedUser(player.getUniqueId()).getProfile());
						}
					}
				}.runTask(Sblock.getInstance());
			}
		}.runTaskAsynchronously(Sblock.getInstance());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (args.length != 1) {
			return ImmutableList.of();
		} else {
			return super.tabComplete(sender, alias, args);
		}
	}
}
