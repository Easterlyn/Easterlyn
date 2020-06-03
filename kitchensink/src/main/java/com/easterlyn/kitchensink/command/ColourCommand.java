package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.InvalidCommandArgument;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MinecraftMessageKeys;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import java.util.EnumSet;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

@CommandAlias("colour|color")
@Description("Taste the rainbow!")
@CommandPermission("easterlyn.command.colour")
public class ColourCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@Default
	public void colour(BukkitCommandIssuer issuer) {
		StringBuilder builder = new StringBuilder();
		for (ChatColor colour : ChatColor.values()) {
			builder.append(colour).append('&').append(colour.getChar()).append(' ')
					.append(colour.name().toLowerCase()).append(ChatColor.RESET).append(' ');
		}
		// ACF forces color code translations for & codes, have to manually circumvent.
		issuer.getIssuer().sendMessage(builder.toString());
	}

	@Subcommand("select")
	@Description("Select your colour.")
	@CommandPermission("easterlyn.command.colour.select")
	public void colour(CommandIssuer issuer, @Flags("colour") ChatColor colour) {
		if (!issuer.isPlayer()) {
			issuer.sendError(MinecraftMessageKeys.NO_PLAYER_FOUND, "{search}", "null");
			return;
		}

		EnumSet<ChatColor> allowed = EnumSet.of(ChatColor.RED, ChatColor.GOLD,
				ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.AQUA,
				ChatColor.DARK_AQUA, ChatColor.BLUE, ChatColor.DARK_BLUE, ChatColor.LIGHT_PURPLE,
				ChatColor.DARK_PURPLE, ChatColor.GRAY, ChatColor.DARK_GRAY, ChatColor.WHITE);

		if (!allowed.contains(colour)) {
			throw new InvalidCommandArgument(MessageKeys.PLEASE_SPECIFY_ONE_OF, "{valid}",
					allowed.stream().map(ChatColor::name).collect(Collectors.joining(", ", "[", "]")));
		}

		User user = core.getUserManager().getUser(issuer.getUniqueId());
		user.setColor(colour);

		user.sendMessage("Set colour to " + colour + colour.name());
	}

	@CommandAlias("colour|color")
	@Description("Set someone's colour. Bypasses selection limitations.")
	@CommandPermission("easterlyn.command.colour.select.other")
	public void colour(CommandIssuer issuer, User target, @Flags("colour") ChatColor colour) {
		target.setColor(colour);
		target.sendMessage("Your colour was set to " + colour + colour.name());
		issuer.sendMessage("Set " + target.getDisplayName() + "'s colour to " + colour + colour.name());
	}

	@CommandAlias("lel")
	@Description("【ＴＡＳＴＥ　ＴＨＥ　ＰＡＩＮＢＯＷ】")
	@Syntax("[painful to view sentence]")
	@CommandPermission("easterlyn.command.lel")
	public void requestLordEnglishEyeFuck(String message) {
		ChatColor[] rainbow = { ChatColor.DARK_RED, ChatColor.RED, ChatColor.GOLD,
				ChatColor.YELLOW, ChatColor.GREEN, ChatColor.DARK_GREEN, ChatColor.AQUA,
				ChatColor.DARK_AQUA, ChatColor.BLUE, ChatColor.DARK_BLUE, ChatColor.LIGHT_PURPLE,
				ChatColor.DARK_PURPLE };
		StringBuilder lelOut = new StringBuilder();
		for (int i = 0; i < message.length();) {
			for (int j = 0; i < message.length() && j < rainbow.length; ++i, ++j) {
				char charAt = message.charAt(i);
				if (Character.isWhitespace(charAt)) {
					--j;
					lelOut.append(charAt);
					continue;
				}
				lelOut.append(rainbow[j]).append(ChatColor.MAGIC).append(charAt);
			}
		}
		Bukkit.broadcastMessage(lelOut.substring(0, Math.max(lelOut.length() - 1, 0)));
	}

}
