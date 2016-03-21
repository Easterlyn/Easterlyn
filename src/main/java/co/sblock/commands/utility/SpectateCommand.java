package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.micromodules.Spectators;
import co.sblock.users.User;
import co.sblock.users.Users;

/**
 * SblockCommand for toggling spectate mode.
 * 
 * @author Jikoo
 */
public class SpectateCommand extends SblockCommand {

	private final Spectators spectators;
	private final Users users;

	public SpectateCommand(Sblock plugin) {
		super(plugin, "spectate");
		this.spectators = plugin.getModule(Spectators.class);
		this.users = plugin.getModule(Users.class);
		this.setAliases("spec", "spectator");
		this.setDescription("Player: Become the ghost (toggles spectator mode)");
		this.setUsage("To toggle spectate mode, use no arguments.\n"
				+ "To prevent players from spectating to you, use /spectate deny\n"
				+ "To allow players to spectate to you, use /spectate allow");
		Permission permission;
		try {
			permission = new Permission("sblock.command.spectate.unrestricted", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.spectate.unrestricted");
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
		if (!spectators.isEnabled()) {
			sender.sendMessage("Spectate module is not enabled!");
			return true;
		}
		Player player = (Player) sender;
		User user = users.getUser(player.getUniqueId());
		if (args.length > 0) {
			args[0] = args[0].toLowerCase();
			if (args[0].equals("on") || args[0].equals("allow") || args[0].equals("true")) {
				user.setSpectatable(true);
				sender.sendMessage(Color.GOOD
						+ "Other players are now allowed to spectate to you!");
				return true;
			}
			if (args[0].equals("off") || args[0].equals("deny") || args[0].equals("false")) {
				user.setSpectatable(false);
				sender.sendMessage(Color.GOOD
						+ "Other players are no longer allowed to spectate to you!");
				return true;
			}
			sender.sendMessage(this.getUsage());
			return true;
		}
		if (spectators.isSpectator(player.getUniqueId())) {
			sender.sendMessage(Color.GOOD + "Suddenly, you snap back to reality. It was all a dream... wasn't it?");
			spectators.removeSpectator(player, false);
		} else {
			if (player.getGameMode() != GameMode.SURVIVAL) {
				sender.sendMessage(Color.BAD + "You can only enter spectate mode from survival.");
				return true;
			}
			sender.sendMessage(Color.GOOD + "You feel a tingling sensation about your extremities as you hover up slightly.");
			spectators.addSpectator(player);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		// CHAT: tab-complete allow/deny
		return ImmutableList.of();
	}
}
