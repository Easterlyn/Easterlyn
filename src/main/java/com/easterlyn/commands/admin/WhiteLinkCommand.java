package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.discord.Discord;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.player.PlayerUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_15_R1.UserCache;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.v1_15_R1.CraftServer;
import org.bukkit.entity.Player;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.UUID;

/**
 * EasterlynCommand for whitelisting and linking a user.
 *
 * @author Jikoo
 */
public class WhiteLinkCommand extends EasterlynAsynchronousCommand {

	private final Discord discord;

	public WhiteLinkCommand(Easterlyn plugin) {
		super(plugin, "whitelink");
		this.discord = plugin.getModule(Discord.class);
		this.setPermissionLevel(UserRank.MOD);
		this.setUsage("/whitelink <name/UUID> <Discord ID>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {

		if (args.length < 2) {
			return false;
		}

		UUID uuid = null;
		String name = null;

		// TODO: Make PlayerUtils#getOrLookUp
		try {
			uuid = UUID.fromString(args[0]);
		} catch (IllegalArgumentException e) {
			name = args[0];
		}

		if (name != null) {
			UserCache userCache = ((CraftServer) Bukkit.getServer()).getServer().getUserCache();
			GameProfile profile = userCache.getProfile(name);
			if (profile == null) {
				sender.sendMessage(getLang().getValue("core.error.invalidUser").replace("{PLAYER}", args[0]));
				return true;
			}
			uuid = profile.getId();
		}

		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
		offlinePlayer.setWhitelisted(true);
		Player player = PlayerUtils.getPlayer(this.getPlugin(), uuid);

		if (hasHigherPerms(player, sender)) {
			sender.sendMessage("Cannot link Discord for a user with higher ingame perms than your own.");
			return false;
		}

		String discordID = TextUtils.join(args, ' ', 1, args.length);
		IUser user = null;
		try {
			long longID = Long.parseLong(discordID);
			user = discord.getClient().getUserByID(longID);
		} catch (NumberFormatException ignored) {}

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
		for (int i = UserRank.MOD.ordinal() + 1; i < ranks.length; ++i) {
			if (!sender.hasPermission(ranks[i].getPermission()) && player.hasPermission(ranks[i].getPermission())) {
				return true;
			}
		}
		return false;
	}
}
