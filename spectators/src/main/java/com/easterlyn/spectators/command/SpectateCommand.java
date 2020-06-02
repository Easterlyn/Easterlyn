package com.easterlyn.spectators.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Private;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynSpectators;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import com.easterlyn.util.Request;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

@CommandAlias("spectate|spec")
@CommandPermission("easterlyn.command.spectate")
public class SpectateCommand extends BaseCommand {

	@Default
	@Private
	public void spectate(@Flags(CoreContexts.SELF) User user) {
		Player player = user.getPlayer();
		if (player == null) {
			user.sendMessage("Gotta be online, buckaroo!");
			return;
		}

		Location spectateReturn = user.getStorage().getSerializable(EasterlynSpectators.USER_SPECTATE_RETURN, Location.class);

		if (spectateReturn == null) {
			if (player.getGameMode() != GameMode.SURVIVAL) {
				user.sendMessage("Woah nelly, you've gotta be in survival to enter spectate mode.");
				return;
			}
			player.setGameMode(GameMode.SPECTATOR);
			user.getStorage().set(EasterlynSpectators.USER_SPECTATE_RETURN, player.getLocation());
			user.sendMessage("Insert snappy spectate message here.");
			return;
		}

		if (player.getGameMode() != GameMode.SPECTATOR) {
			user.sendMessage("Excuse me, are you trying to exit spectator mode from not spectator mode?" +
					"\nThat's ILLEGAL. I'm calling the cops. Generating a report for you!");
			ReportableEvent.call(user.getDisplayName() + " attempted to exit spectator mode while in " + player.getGameMode().name());
		}

		user.getStorage().set(EasterlynSpectators.USER_SPECTATE_RETURN, null);
		user.getStorage().set(EasterlynSpectators.USER_SPECTATE_COOLDOWN, System.currentTimeMillis() + 480000L);
		player.teleport(spectateReturn);
		player.setGameMode(GameMode.SURVIVAL);
	}

	@CommandAlias("spectate|spec|spectpa")
	@Description("Request to spectate to a player.")
	@CommandCompletion("@player")
	@Syntax("/spectpa <player>")
	@CommandPermission("easterlyn.command.spectpa")
	public void spectateTPA(@Flags(CoreContexts.SELF) User user, @Flags(CoreContexts.ONLINE) User target) {
		Player player = user.getPlayer();
		Player targetPlayer = target.getPlayer();
		if (player == null || targetPlayer == null) {
			user.sendMessage("Players must be online to teleport!");
			return;
		}
		if (player.getGameMode() != GameMode.SPECTATOR) {
			player.sendMessage("You must be in spectate mode to send a spectate request!");
			return;
		}
		if (target.setPendingRequest(new Request() {

			@Override
			public void accept() {
				Player localPlayer = user.getPlayer();
				Player localTarget = target.getPlayer();
				if (localPlayer == null || localTarget == null) {
					target.sendMessage("Players must be online to teleport!");
					return;
				}
				if (localPlayer.getGameMode() != GameMode.SPECTATOR) {
					localTarget.sendMessage(user.getDisplayName() + " is no longer spectating!");
					return;
				}
				if (localPlayer.teleport(localTarget.getLocation().add(0, 0.1, 0), PlayerTeleportEvent.TeleportCause.PLUGIN)) {
					localTarget.sendMessage("Accepted " + user.getDisplayName() + "'s spectate request!");
				}
			}

			@Override
			public void decline() {
				target.sendMessage("Request declined!");
				user.sendMessage(target.getDisplayName() + " declined your request!");
			}
		})) {
			target.sendMessage(user.getDisplayName() + " is requesting to spectate to you!\nUse /accept or /decline to manage the request.");
		} else {
			user.sendMessage(target.getDisplayName() + " already has a pending request!");
		}
	}

}
