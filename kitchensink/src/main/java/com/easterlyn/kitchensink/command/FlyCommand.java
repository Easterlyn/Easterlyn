package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Single;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.util.StringUtil;
import org.bukkit.entity.Player;

public class FlyCommand extends BaseCommand {

	@Dependency
	EasterlynCore core;

	@CommandAlias("fly")
	@Description("{@@sink.module.fly.description}")
	@CommandPermission("easterlyn.command.fly.self")
	@CommandCompletion("@player @boolean")
	@Syntax("[player] [true|false]")
	public void fly(@Flags(CoreContexts.ONLINE_WITH_PERM) Player player, @Default("toggle") @Single String flightString) {

		Boolean flight = StringUtil.asBoolean(flightString);
		if (flight == null) {
			flight = !player.getAllowFlight();
		}

		String valuePath = flight ? "core.common.on" : "core.common.off";
		flightString = core.getLocaleManager().getValue(valuePath, core.getLocaleManager().getLocale(player));
		if (flight != player.getAllowFlight()) {
			player.setAllowFlight(flight);
			core.getLocaleManager().sendMessage(player, "sink.module.fly.message",
					"{value}", flightString == null ? "null" : flightString);
		}

		CommandIssuer issuer = getCurrentCommandIssuer();
		if (issuer.getUniqueId().equals(player.getUniqueId())) {
			return;
		}

		valuePath = flight ? "core.common.on" : "core.common.off";
		flightString = core.getLocaleManager().getValue(valuePath, core.getLocaleManager().getLocale(issuer.getIssuer()));
		flightString += core.getLocaleManager().getValue("", core.getLocaleManager().getLocale(issuer.getIssuer()),
				"{target}", player.getName());
		core.getLocaleManager().sendMessage(issuer.getIssuer(), "sink.module.fly.message", "{value}", flightString);
	}

}
