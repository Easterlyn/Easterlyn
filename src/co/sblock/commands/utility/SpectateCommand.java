package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.commands.SblockCommand;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;

/**
 * SblockCommand for toggling spectate mode.
 * 
 * @author Jikoo
 */
public class SpectateCommand extends SblockCommand {

	public SpectateCommand() {
		super("spectate");
		this.setDescription("Player: Become the ghost (toggles spectator mode)");
		this.setUsage("To toggle spectate mode, use no arguments.\n"
				+ "To prevent players from spectating to you, use /spectate deny\n"
				+ "To allow players to spectate to you, use /spectate allow");
		Permission permission;
		try {
			permission = new Permission("sblock.command.spectate.nocooldown", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.spectate.nocooldown");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.helper", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		if (args.length > 0) {
			args[0] = args[0].toLowerCase();
			if (args[0].equals("on") || args[0].equals("allow") || args[0].equals("true")) {
				user.setSpectatable(true);
				sender.sendMessage(ChatColor.GREEN
						+ "Other players are now allowed to spectate to you!");
				return true;
			}
			if (args[0].equals("off") || args[0].equals("deny") || args[0].equals("false")) {
				user.setSpectatable(false);
				sender.sendMessage(ChatColor.GREEN
						+ "Other players are no longer allowed to spectate to you!");
				return true;
			}
			sender.sendMessage(this.getUsage());
			return true;
		}
		if (user instanceof OnlineUser && ((OnlineUser) user).isServer()) {
			sender.sendMessage(ChatColor.RED + "Perhaps you should focus on helping your client!");
			return true;
		}
		if (Spectators.getInstance().isSpectator(player.getUniqueId())) {
			sender.sendMessage(ChatColor.GREEN + "Suddenly, you snap back to reality. It was all a dream... wasn't it?");
			Spectators.getInstance().removeSpectator(player);
		} else {
			if (player.getGameMode() != GameMode.SURVIVAL) {
				sender.sendMessage(ChatColor.RED + "You can only enter spectate mode from survival.");
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + "You feel a tingling sensation about your extremities as you hover up slightly.");
			Spectators.getInstance().addSpectator(player);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		// TODO allow completion of allow/deny
		return ImmutableList.of();
	}
}
