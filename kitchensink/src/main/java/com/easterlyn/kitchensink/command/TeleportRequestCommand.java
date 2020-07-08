package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynKitchenSink;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.util.Request;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

public class TeleportRequestCommand extends BaseCommand {

	private static final String CONFIG_ACCEPT = "command.teleportrequest.accept";
	private static final String CONFIG_IGNORE = "command.teleportrequest.ignore";
	public static final String TPREQUEST_COOLDOWN = "kitchensink.tprequest";

	@Dependency
	EasterlynCore core;
	@Dependency
	EasterlynKitchenSink sink;

	@CommandAlias("tpa|tpask|tprequest")
	@Description("{@@sink.module.tprequest.to.description}")
	@CommandPermission("easterlyn.command.tpa")
	@Syntax("<player>")
	@CommandCompletion("@player")
	public void teleportRequest(@Flags(CoreContexts.SELF) User issuer, @Flags(CoreContexts.ONLINE) User target) {
		tpRequest(issuer, target, true);
	}

	@CommandAlias("tpahere|tpaskhere|call|callhere")
	@Description("{@@sink.module.tprequest.pull.description}")
	@CommandPermission("easterlyn.command.tpa")
	@Syntax("<player>")
	@CommandCompletion("@player")
	public void teleportHereRequest(@Flags(CoreContexts.SELF) User issuer, @Flags(CoreContexts.ONLINE) User target) {
		tpRequest(issuer, target, false);
	}

	private void tpRequest(User issuer, User requested, boolean to) {
		long nextTPA = issuer.getStorage().getLong(TPREQUEST_COOLDOWN);
		if (nextTPA > System.currentTimeMillis()) {
			SimpleDateFormat format = new SimpleDateFormat("m:ss");
			core.getLocaleManager().sendMessage(issuer.getPlayer(), "sink.module.tprequest.error.cooldown",
					"{value}", format.format(new Date(nextTPA - System.currentTimeMillis())));
			return;
		}

		issuer.getStorage().set(TPREQUEST_COOLDOWN, System.currentTimeMillis() + sink.getConfig().getLong(CONFIG_IGNORE));

		Player issuingPlayer = issuer.getPlayer();
		Player requestedPlayer = requested.getPlayer();

		if (issuingPlayer == null || requestedPlayer == null) {
			core.getLocaleManager().sendMessage(issuingPlayer, "sink.module.tprequest.error.offline");
			return;
		}

		if (requested.setPendingRequest(new Request() {
			@Override
			public void accept() {
				Player localIssuer = issuer.getPlayer();
				Player localRequested = requested.getPlayer();
				if (localIssuer == null || localRequested == null) {
					core.getLocaleManager().sendMessage(localRequested, "sink.module.tprequest.error.offline");
					return;
				}
				Player destination = to ? localRequested : localIssuer;
				Player teleportee = to ? localIssuer : localRequested;
				if (teleportee.teleport(destination.getLocation().add(0, 0.1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
					core.getLocaleManager().sendMessage(localRequested, "sink.module.tprequest.common.accept");
					core.getLocaleManager().sendMessage(localIssuer, "sink.module.tprequest.common.accepted",
							"{value}", localRequested.getName());
				}
			}
			@Override
			public void decline() {
				core.getLocaleManager().sendMessage(requestedPlayer, "sink.module.tprequest.common.decline");
				core.getLocaleManager().sendMessage(issuingPlayer, "sink.module.tprequest.common.declined",
						"{value}", requestedPlayer.getName());
				issuer.getStorage().set(TPREQUEST_COOLDOWN,
						issuer.getStorage().getLong(TPREQUEST_COOLDOWN) - sink.getConfig().getLong(CONFIG_IGNORE) + sink.getConfig().getLong(CONFIG_ACCEPT));
			}
		})) {
			core.getLocaleManager().sendMessage(issuingPlayer, "sink.module.tprequest.common.issued");
			core.getLocaleManager().sendMessage(requestedPlayer,
					to ? "sink.module.tprequest.to.request" : "sink.module.tprequest.pull.request",
					"{value}", issuingPlayer.getName());
		} else {
			core.getLocaleManager().sendMessage(issuingPlayer, "sink.module.tprequest.error.popular",
					"{value}", requestedPlayer.getName());
		}
	}

}
