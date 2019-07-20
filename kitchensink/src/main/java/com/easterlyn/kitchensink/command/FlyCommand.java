package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CommandRank;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import com.easterlyn.util.StringUtil;
import org.bukkit.entity.Player;

public class FlyCommand extends BaseCommand {

	public FlyCommand() {
		PermissionUtil.addParent("easterlyn.command.fly.other", UserRank.MODERATOR.getPermission());
	}

	@CommandAlias("fly")
	@Description("Manage flight for a player.")
	@CommandPermission("easterlyn.command.fly")
	@CommandRank(UserRank.MODERATOR)
	@CommandCompletion("@player @boolean")
	@Syntax("/fly [player] [true|false]")
	public void fly(@Flags(CoreContexts.ONLINE_WITH_PERM) Player player, @Default("toggle") @Single String flightString) {

		Boolean flight = StringUtil.asBoolean(flightString);
		if (flight == null) {
			flight = !player.getAllowFlight();
		}

		CommandIssuer issuer = getCurrentCommandIssuer();
		flightString = flight ? "on" : "off";
		if (flight == player.getAllowFlight()) {
			issuer.sendMessage("Flight is already " + flightString + " for " + player.getName());
			return;
		}

		player.setAllowFlight(flight);
		player.sendMessage("Flight turned " + flightString);
		if (!issuer.getUniqueId().equals(player.getUniqueId())) {
			issuer.sendMessage("Flight turned " + flightString + " for " + player.getName());
		}
	}

}
