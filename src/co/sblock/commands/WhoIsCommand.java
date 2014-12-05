package co.sblock.commands;

import java.util.ArrayList;
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
import co.sblock.users.UserManager;

/**
 * SblockCommand for checking a User's stored data.
 * 
 * @author Jikoo
 */
public class WhoIsCommand extends SblockCommand {

	public WhoIsCommand() {
		super("whois");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <player>");
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add("profile");
		this.setAliases(aliases);
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
			return true;
		}
		final UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
		new BukkitRunnable() {
			@Override
			public void run() {
				@SuppressWarnings("deprecation")
				final OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
				if (uuid != null && Bukkit.getPlayer(uuid) == null) {
					return;
				}
				if (player == null || !player.hasPlayedBefore()) {
					sender.sendMessage(ChatColor.GOLD + args[0] + ChatColor.RED + " has never played on this server.");
					return;
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						if (sender.hasPermission("group.felt")) {
							sender.sendMessage(UserManager.getGuaranteedUser(player.getUniqueId()).getWhois());
						} else {
							sender.sendMessage(UserManager.getGuaranteedUser(player.getUniqueId()).getProfile());
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
