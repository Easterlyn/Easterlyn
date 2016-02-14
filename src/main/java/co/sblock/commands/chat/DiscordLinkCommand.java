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
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;

import net.md_5.bungee.api.ChatColor;

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
		setDescription("Generate a code to link your Discord account with Minecraft");
		setUsage(ChatColor.AQUA + "/link");
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
		if (args.length > 1 && sender.hasPermission("sblock.command.link.force")) {
			UUID uuid;
			try {
				uuid = UUID.fromString(args[0]);
			} catch (IllegalArgumentException e) {
				sender.sendMessage(Color.BAD + "Invalid UUID. /link [UUID] [DiscordUser/ID]");
				return true;
			}
			String discordID = StringUtils.join(args, ' ', 1, args.length);
			IUser user = discord.getAPI().getUserByID(discordID);
			if (user == null) {
				for (IGuild guild : discord.getAPI().getGuilds()) {
					for (IUser iUser : guild.getUsers()) {
						if (iUser.getName().equalsIgnoreCase(discordID)) {
							user = iUser;
							break;
						}
					}
				}
			}
			if (user == null) {
				sender.sendMessage(Color.BAD + "Unknown Discord user. /link [UUID] [DiscordUser/ID]");
			}
			discord.addLink(uuid, user);
			return true;
		}
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (!discord.isEnabled()) {
			sender.sendMessage(Color.BAD + "Discord link is currently nonfunctional!");
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
		sender.sendMessage(Color.GOOD + "Message the Discord bot \"" + Color.GOOD_EMPHASIS
				+ "/link " + code + Color.GOOD + "\" to complete linking your Discord account!\n"
				+ Color.BAD + "This code will expire in a minute.");
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
