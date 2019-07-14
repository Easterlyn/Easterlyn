package com.easterlyn.spectators.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Private;
import com.easterlyn.EasterlynSpectators;
import com.easterlyn.event.ReportableEvent;
import com.easterlyn.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

@CommandAlias("spectate|spec")
@CommandPermission("easterlyn.command.spectate")
public class SpectateCommand extends BaseCommand {

	// TODO rich messages
	@Default
	@Private
	public void spectate(@Flags("self") User user) {
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
			Bukkit.getServer().getPluginManager().callEvent(new ReportableEvent(user.getDisplayName() + " attempted to exit spectator mode while in " + player.getGameMode().name()));
		}

		user.getStorage().set(EasterlynSpectators.USER_SPECTATE_RETURN, null);
		user.getStorage().set(EasterlynSpectators.USER_SPECTATE_COOLDOWN, System.currentTimeMillis() + 480000L);
		player.teleport(spectateReturn);
		player.setGameMode(GameMode.SURVIVAL);
	}

	@CommandAlias("spectate|spec|spectpa")
	public void spectateTPA(@Flags("self") User user, @Flags("other") User target) {
		user.sendMessage("Sorry, Adam is a lazy bum and hasn't gotten to this bit yet.");
		// TODO /spectpa
	}

}
