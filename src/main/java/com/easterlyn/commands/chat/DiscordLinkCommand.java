package com.easterlyn.commands.chat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.discord.Discord;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PlayerLoader;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

/**
 * Command for linking a Discord account to Minecraft.
 * 
 * @author Jikoo
 */
public class DiscordLinkCommand extends EasterlynCommand {

	private final Discord discord;

	public DiscordLinkCommand(Easterlyn plugin) {
		super(plugin, "link");
		this.discord = plugin.getModule(Discord.class);
		this.addExtraPermission("force", UserRank.FELT);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!discord.isEnabled() || !discord.isReady()) {
			sender.sendMessage(getLang().getValue("core.error.moduleDisabled").replace("{MODULE}", "Discord"));
			return true;
		}

		if (args.length > 1 && sender.hasPermission("easterlyn.command.link.force")) {
			UUID uuid;
			try {
				uuid = UUID.fromString(args[0]);
			} catch (IllegalArgumentException e) {
				return false;
			}

			Player player = PlayerLoader.getPlayer(this.getPlugin(), uuid);
			if (hasHigherPerms(player, sender)) {
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
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
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

		if (args.length > 1 && code.toString().equals(args[0])) {
			// Ensure user can read. Most can't.
			sender.sendMessage(getLang().getValue("command.link.basicReadingComprehension"));
			return true;
		}

		sender.sendMessage(getLang().getValue("command.link.success").replace("{CODE}", code.toString()));
		return true;
	}

	/**
	 * Check if the Player being linked has a higher permission level than the CommandSender creating the link.
	 * 
	 * @param player the Player being linked
	 * @param sender the CommandSender
	 * 
	 * @return true if the Player being linked has a higher permission level
	 */
	private boolean hasHigherPerms(Player player, CommandSender sender) {
		if (sender instanceof ConsoleCommandSender) {
			return false;
		}
		UserRank[] ranks = UserRank.values();
		for (int i = UserRank.FELT.ordinal() + 1; i < ranks.length; ++i) {
			if (!sender.hasPermission(ranks[i].getPermission()) && player.hasPermission(ranks[i].getPermission())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
