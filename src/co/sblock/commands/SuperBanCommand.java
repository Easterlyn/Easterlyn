package co.sblock.commands;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.BanList.Type;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.scheduler.BukkitRunnable;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.users.OfflineUser;
import co.sblock.users.UserManager;

/**
 * SblockCommand for a dual ip and UUID ban.
 * 
 * @author Jikoo
 */
public class SuperBanCommand extends SblockCommand {

	public SuperBanCommand() {
		super("sban");
		this.setDescription("YOU CAN'T ESCAPE THE RED MILES.");
		this.setUsage("/sban <target> [optional reason]");
		this.setPermission("group.denizen");
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (args == null || args.length == 0) {
			return false;
		}
		final String target = args[0];
		final StringBuilder reason = new StringBuilder();
		for (int i = 1; i < args.length; i++) {
			reason.append(ChatColor.translateAlternateColorCodes('&', args[i])).append(' ');
		}
		if (args.length == 1) {
			reason.append("Git wrekt m8.");
		}
		if (target.contains(".")) { // IPs probably shouldn't be announced.
			Bukkit.getBanList(org.bukkit.BanList.Type.IP).addBan(target, reason.toString(), null, sender.getName());
		} else {
			Bukkit.broadcastMessage(ChatColor.DARK_RED + target
					+ " has been wiped from the face of the multiverse. " + reason.toString());
			new BukkitRunnable() {
				@Override
				public void run() {
					@SuppressWarnings("deprecation")
					final OfflinePlayer player = Bukkit.getOfflinePlayer(target);
					new BukkitRunnable() {
						@Override
						public void run() {
							OfflineUser victim = UserManager.getGuaranteedUser(player.getUniqueId());
							Bukkit.getBanList(Type.NAME).addBan(victim.getPlayerName(),
									"<ip=" + victim.getUserIP() + ">" + reason, null, sender.getName());
							Bukkit.getBanList(Type.IP).addBan(victim.getUserIP(),
									"<name=" + victim.getPlayerName() + ">" + reason, null, sender.getName());
							victim.getPlayer().kickPlayer(reason.toString());
						}
					}.runTask(Sblock.getInstance());
				}
			}.runTaskAsynchronously(Sblock.getInstance());
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (args.length < 2) {
			return super.tabComplete(sender, alias, args);
		}
		return ImmutableList.of();
	}
}
