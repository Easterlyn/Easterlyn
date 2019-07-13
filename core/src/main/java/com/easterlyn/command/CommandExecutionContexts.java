package com.easterlyn.command;

import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandExecutionContext;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import com.easterlyn.EasterlynCore;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import com.easterlyn.util.NumberUtil;
import com.easterlyn.util.PlayerUtil;
import java.util.Date;
import java.util.UUID;
import org.bukkit.entity.Player;

public class CommandExecutionContexts {

	public static void register(EasterlynCore plugin) {
		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(BukkitCommandIssuer.class,
				CommandExecutionContext::getIssuer);

		plugin.getCommandManager().getCommandContexts().registerIssuerAwareContext(User.class, context -> {
			if (context.hasFlag("self")) {
				if (context.getSender() instanceof Player) {
					return plugin.getUserManager().getUser(((Player) context.getSender()).getUniqueId());
				}
				throw new InvalidCommandArgument(MessageKeys.NOT_ALLOWED_ON_CONSOLE);
			}
			String potentialIdentifier = context.getFirstArg();
			try {
				return plugin.getUserManager().getUser(UUID.fromString(potentialIdentifier));
			} catch (IllegalArgumentException ignored) {}
			// TODO other flag should ignore self, requires PlayerUtil modification
			Player player;
			try {
				player = PlayerUtil.matchPlayer(context.getSender(), potentialIdentifier, context.hasFlag("offline"), plugin);
			} catch (IllegalAccessException e) {
				plugin.getServer().getPluginManager().callEvent(new ReportableEvent(
						"Called PlayerUtil#matchPlayer on the main thread while executing!", e, 5));
				player = PlayerUtil.matchOnlinePlayer(context.getSender(), potentialIdentifier);
			}
			if (player != null) {
				context.popFirstArg();
				return plugin.getUserManager().getUser(player.getUniqueId());
			}
			if (context.hasFlag("other") || !(context.getSender() instanceof Player)) {
				throw new InvalidCommandArgument(MessageKeys.COULD_NOT_FIND_PLAYER);
			}
			return plugin.getUserManager().getUser(((Player) context.getSender()).getUniqueId());
		});

		plugin.getCommandManager().getCommandContexts().registerContext(Date.class, context -> {
			String firstArg = context.popFirstArg();
			long duration = NumberUtil.parseDuration(firstArg);
			return new Date(Math.addExact(System.currentTimeMillis(), duration));
		});
	}

	private CommandExecutionContexts() {}

}
