package com.easterlyn.commands.admin;

import java.util.Date;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.NumberUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * SblockCommand for adding a temporary ban.
 * 
 * @author Jikoo
 */
public class TemporaryBanCommand extends SblockCommand {

	public TemporaryBanCommand(Easterlyn plugin) {
		super(plugin, "tempban");
		this.setAliases("tempbanip");
		this.setPermissionLevel(UserRank.FELT);
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (args == null || args.length < 2) {
			return false;
		}
		final String target = args[0];
		String arguments = StringUtils.join(args, ' ', 1, args.length);
		final Pair<String, Long> banData = NumberUtils.parseAndRemoveFirstTime(arguments);
		if (banData.getRight() < 1) {
			sender.sendMessage("Unable to parse ban duration! Must be a minimum of 1 second.");
			return false;
		}
		arguments = banData.getLeft().length() > 0 ? banData.getLeft() : "Take some time to cool off.";
		final Date expiry = new Date(System.currentTimeMillis() + banData.getRight());
		if (target.matches("([0-9]{1,3}\\.){3}[0-9]{1,3}")) { // IPs probably shouldn't be announced.
			Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(target, arguments, expiry, sender.getName());
		} else {
			banByName(sender, target, arguments, expiry);
		}
		return true;
	}

	private void banByName(final CommandSender sender, final String target, final String reason, final Date expiry) {
		new BukkitRunnable() {
			@Override
			public void run() {
				@SuppressWarnings("deprecation")
				final OfflinePlayer player = Bukkit.getOfflinePlayer(target);
				if (player == null) {
					return;
				}
				new BukkitRunnable() {
					@Override
					public void run() {
						Bukkit.getBanList(Type.NAME).addBan(player.getName(), reason,
									expiry, sender.getName());
						if (player.isOnline()) {
							player.getPlayer().kickPlayer(reason);
						}
						Bukkit.broadcastMessage(getLang().getValue("command.tempban.announce")
								.replace("{PLAYER}", player.getName()).replace("{REASON}", reason.toString()));
					}
				}.runTask(getPlugin());
			}
		}.runTaskAsynchronously(getPlugin());
	}

}
