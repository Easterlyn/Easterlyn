package co.sblock.commands.utility;

import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.users.Users;

/**
 * SblockCommand for checking a User's stored data.
 * 
 * @author Jikoo
 */
public class WhoIsCommand extends SblockAsynchronousCommand {

	public WhoIsCommand(Sblock plugin) {
		super(plugin, "whois");
		this.setAliases("profile");
		this.setDescription("Check data stored for a player.");
		this.setUsage("/whois <player>");
		Permission permission;
		try {
			permission = new Permission("sblock.command.whois.detail", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.whois.detail");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.felt", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(final CommandSender sender, final String label, final String[] args) {
		if (!(sender instanceof Player) && args.length == 0) {
			return false;
		}
		final UUID uuid = args.length >= 1 ? getUniqueId(args[0]) : ((Player) sender).getUniqueId();
		if (uuid == null) {
			sender.sendMessage(Color.BAD_PLAYER + args[0] + Color.BAD + " has never played on this server.");
			return true;
		}
		if (sender.hasPermission("sblock.command.whois.detail")) {
			sender.sendMessage(Users.getGuaranteedUser(((Sblock) getPlugin()), uuid).getWhois());
		} else {
			sender.sendMessage(Users.getGuaranteedUser(((Sblock) getPlugin()), uuid).getProfile());
		}
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
