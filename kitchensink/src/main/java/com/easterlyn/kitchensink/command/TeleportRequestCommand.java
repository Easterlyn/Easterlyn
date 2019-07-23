package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
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
	private static final String TPREQUEST = "kitchensink.tprequest";

	@Dependency
	EasterlynKitchenSink plugin;

	@CommandAlias("tpa|tpask|tprequest")
	@Description("Request to teleport to a player.")
	@CommandCompletion("@player")
	@Syntax("/tpa <player>")
	@CommandPermission("easterlyn.command.tpa")
	public void teleportRequest(@Flags(CoreContexts.SELF) User issuer, @Flags(CoreContexts.ONLINE) User target) {
		long nextTPA = issuer.getStorage().getLong(TPREQUEST);
		if (nextTPA > System.currentTimeMillis()) {
			SimpleDateFormat format = new SimpleDateFormat("m:ss");
			issuer.sendMessage("You can send another teleport request in " + format.format(new Date(nextTPA - System.currentTimeMillis())));
			return;
		}

		issuer.getStorage().set(TPREQUEST, System.currentTimeMillis() + plugin.getConfig().getLong(CONFIG_IGNORE));

		Player player = issuer.getPlayer();
		Player targetPlayer = target.getPlayer();

		if (player == null || targetPlayer == null) {
			issuer.sendMessage("Players must be online to teleport!");
			return;
		}

		if (target.setPendingRequest(new Request() {
			@Override
			public void accept() {
				Player localPlayer = issuer.getPlayer();
				Player localTarget = target.getPlayer();
				if (localPlayer == null || localTarget == null) {
					target.sendMessage("Players must be online to teleport!");
					return;
				}
				if (localPlayer.teleport(localTarget.getLocation().add(0, 0.1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
					localTarget.sendMessage("Accepted " + issuer.getDisplayName() + "'s teleport request!");
				}
			}
			@Override
			public void decline() {
				target.sendMessage("Request declined!");
				issuer.sendMessage(target.getDisplayName() + " declined your request!");
				issuer.getStorage().set(TPREQUEST,
						System.currentTimeMillis() + plugin.getConfig().getLong(CONFIG_ACCEPT)
								+ issuer.getStorage().getLong(TPREQUEST) - plugin.getConfig().getLong(CONFIG_IGNORE));
			}
		})) {
			target.sendMessage(issuer.getDisplayName() + " is requesting to teleport to you!\nUse /accept or /decline to manage the request.");
		} else {
			issuer.sendMessage(target.getDisplayName() + " already has a pending request!");
		}
	}

	@CommandAlias("tpahere|tpaskhere|call|callhere")
	@Description("Request to teleport a player to you.")
	@CommandCompletion("@player")
	@Syntax("/tpahere <player>")
	@CommandPermission("easterlyn.command.tpa")
	public void teleportHereRequest(@Flags(CoreContexts.SELF) User issuer, @Flags(CoreContexts.ONLINE) User target) {
		long nextTPA = issuer.getStorage().getLong(TPREQUEST);
		if (nextTPA > System.currentTimeMillis()) {
			SimpleDateFormat format = new SimpleDateFormat("m:ss");
			issuer.sendMessage("You can send another teleport request in " + format.format(new Date(nextTPA - System.currentTimeMillis())));
			return;
		}

		issuer.getStorage().set(TPREQUEST, System.currentTimeMillis() + plugin.getConfig().getLong(CONFIG_IGNORE));

		Player player = issuer.getPlayer();
		Player targetPlayer = target.getPlayer();

		if (player == null || targetPlayer == null) {
			issuer.sendMessage("Players must be online to teleport!");
			return;
		}

		if (target.setPendingRequest(new Request() {
			@Override
			public void accept() {
				Player localPlayer = issuer.getPlayer();
				Player localTarget = target.getPlayer();
				if (localPlayer == null || localTarget == null) {
					target.sendMessage("Players must be online to teleport!");
					return;
				}
				if (localTarget.teleport(localPlayer.getLocation().add(0, 0.1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
					localTarget.sendMessage("Accepted " + issuer.getDisplayName() + "'s teleport request!");
				}
			}
			@Override
			public void decline() {
				target.sendMessage("Request declined!");
				issuer.sendMessage(target.getDisplayName() + " declined your request!");
				issuer.getStorage().set(TPREQUEST,
						System.currentTimeMillis() + plugin.getConfig().getLong(CONFIG_ACCEPT)
								+ issuer.getStorage().getLong(TPREQUEST) - plugin.getConfig().getLong(CONFIG_IGNORE));
			}
		})) {
			target.sendMessage(issuer.getDisplayName() + " is requesting to teleport you to them!\nUse /accept or /decline to manage the request.");
		} else {
			issuer.sendMessage(target.getDisplayName() + " already has a pending request!");
		}
	}

}
