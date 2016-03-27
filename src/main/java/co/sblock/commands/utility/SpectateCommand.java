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
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		if (!spectators.isEnabled()) {
			sender.sendMessage(getLang().getValue("core.error.moduleDisabled").replace("{MODULE}", "Spectate"));
			return true;
		}
		Player player = (Player) sender;
		User user = users.getUser(player.getUniqueId());
		if (args.length > 0) {
			args[0] = args[0].toLowerCase();
			if (args[0].equals("on") || args[0].equals("allow") || args[0].equals("true")) {
				user.setSpectatable(true);
				player.sendMessage(getLang().getValue("command.spectate.allow"));
				return true;
			}
			if (args[0].equals("off") || args[0].equals("deny") || args[0].equals("false")) {
				user.setSpectatable(false);
				player.sendMessage(getLang().getValue("command.spectate.deny"));
				return true;
			}
			sender.sendMessage(this.getUsage());
			return true;
		}
		if (spectators.isSpectator(player.getUniqueId())) {
			player.sendMessage(getLang().getValue("spectators.return.standard"));
			spectators.removeSpectator(player, false);
		} else {
			if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.SPECTATOR) {
				player.sendMessage(getLang().getValue("command.spectate.gamemode"));
				return true;
			}
			player.sendMessage(getLang().getValue("spectators.initiate"));
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
