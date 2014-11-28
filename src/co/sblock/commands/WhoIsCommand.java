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
import co.sblock.users.Region;
import co.sblock.users.User;
import co.sblock.users.UserAspect;
import co.sblock.users.UserClass;
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
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			sender.sendMessage(ChatColor.RED + "Please specify a user to look up.");
			return true;
		}
		if (!sender.hasPermission("group.felt")) {
			profile(sender, label, args);
			return true;
		}
		whois(sender, label, args);
		return true;
	}

	private void whois(final CommandSender sender, final String label, final String[] args) {
		Player p = Bukkit.getPlayer(args[0]);
		if (p == null) {
			final UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
			sender.sendMessage(ChatColor.GREEN + "Starting offline lookup...");
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
							sender.sendMessage(UserManager.getOfflineUserInfo(player.getUniqueId()));
						}
					}.runTask(Sblock.getInstance());
				}
			}.runTaskAsynchronously(Sblock.getInstance());
			return;
		}
		User u = UserManager.getUser(p.getUniqueId());
		sender.sendMessage(u.toString());
	}

	private void profile(CommandSender sender, String label, String[] args) {
		User user = null;
		if (args.length == 0) { // Already has to be a player, cast is safe
			user = UserManager.getUser(((Player) sender).getUniqueId());
		} else {
			Player pTarget = Bukkit.getPlayer(args[0]);
			if (pTarget != null) {
				user = UserManager.getUser(pTarget.getUniqueId());
			} else {
				// TODO allow offline?
				sender.sendMessage(ChatColor.YELLOW + "User not found.");
				return;
			}
		}
		sender.sendMessage(profile(user.getPlayerName(), user.getUserClass(), user.getAspect(),
				user.getDreamPlanet(), user.getMediumPlanet()));
	}

	private String profile(String name, UserClass userclass, UserAspect useraspect, Region dream, Region medium) {
		StringBuilder sb = new StringBuilder().append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH)
				.append("+------").append(ChatColor.DARK_AQUA).append(' ').append(name)
				.append(' ').append(ChatColor.YELLOW).append(ChatColor.STRIKETHROUGH)
				.append("------+\n").append(ChatColor.DARK_AQUA).append(userclass.getDisplayName())
				.append(ChatColor.YELLOW).append(" of ").append(useraspect.getColor())
				.append(useraspect.getDisplayName()).append('\n').append(ChatColor.YELLOW)
				.append("Dream planet: ").append(dream.getColor()).append(dream.getDisplayName())
				.append('\n').append(ChatColor.YELLOW).append("Medium planet: ")
				.append(medium.getColor()).append(medium.getDisplayName());
		return sb.toString();
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
