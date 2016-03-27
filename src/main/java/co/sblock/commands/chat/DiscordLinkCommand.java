package co.sblock.commands.chat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Command for linking a Discord account to Minecraft.
 * 
 * @author Jikoo
 */
public class DiscordLinkCommand extends SblockCommand {

	private final Discord discord;

	public DiscordLinkCommand(Sblock plugin) {
		super(plugin, "link");
		this.discord = plugin.getModule(Discord.class);
		Permission permission;
		try {
			permission = new Permission("sblock.command.link.force", PermissionDefault.OP);
			Bukkit.getPluginManager().addPermission(permission);
		} catch (IllegalArgumentException e) {
			permission = Bukkit.getPluginManager().getPermission("sblock.command.link.force");
			permission.setDefault(PermissionDefault.OP);
		}
		permission.addParent("sblock.command.*", true).recalculatePermissibles();
		permission.addParent("sblock.felt", true).recalculatePermissibles();
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!discord.isEnabled() || !discord.isReady()) {
			sender.sendMessage(getLang().getValue("core.error.moduleDisabled").replace("{MODULE}", "Discord"));
			return true;
		}
		if (args.length > 1 && sender.hasPermission("sblock.command.link.force")) {
			UUID uuid;
			try {
				uuid = UUID.fromString(args[0]);
			} catch (IllegalArgumentException e) {
				return false;
			}
			String discordID = StringUtils.join(args, ' ', 1, args.length);
			IUser user = discord.getClient().getUserByID(discordID);
			if (user == null) {
				for (IGuild guild : discord.getClient().getGuilds()) {
					for (IUser iUser : guild.getUsers()) {
						if (iUser.getName().equalsIgnoreCase(discordID)) {
							user = iUser;
							break;
						}
					}
				}
			}
			if (user == null) {
				return false;
			}
			discord.addLink(uuid, user);
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		UUID uuid = ((Player) sender).getUniqueId();
		Object code;
		try {
			code = discord.getAuthCodes().get(uuid);
		} catch (ExecutionException e) {
			// Just re-throw the exception to use our automatic report creation feature
			throw new RuntimeException(e);
		}
		sender.sendMessage(getLang().getValue("command.link.success").replace("{CODE}", code.toString()));
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
