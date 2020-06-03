package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.util.BossBarTimer;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;

public class BackCommand extends BaseCommand implements Listener {

	private static final String BACK_LOCATION = "kitchensink.backLocation";
	public static final String BACK_COOLDOWN = "kitchensink.backTime";

	@Dependency
	EasterlynCore core;

	@CommandAlias("back|b")
	@Description("Teleport to your previous location!")
	@CommandPermission("easterlyn.command.back")
	public void back(BukkitCommandIssuer issuer, @Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
		boolean other = !issuer.getUniqueId().equals(user.getUniqueId());

		if (!other && user.getStorage().getLong(BACK_COOLDOWN) >= System.currentTimeMillis()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss");
			user.sendMessage("You're on cooldown for `/back`! Next usage in "
					+ dateFormat.format(new Date(user.getStorage().getLong(BACK_COOLDOWN) - System.currentTimeMillis())));
			return;
		}

		Location back = user.getStorage().getSerializable(BACK_LOCATION, Location.class);

		if (back == null) {
			issuer.sendMessage("No back location!");
			return;
		}

		Player player = user.getPlayer();
		if (player == null) {
			issuer.sendMessage("Cannot teleport at this time, player not loaded.");
			return;
		}

		new BossBarTimer("Preparing to Teleport...", () -> {
			if (player.teleport(back)) {
				if (other) {
					issuer.sendMessage("Teleported " + user.getDisplayName() + " to their previous location.");
				} else {
					user.getStorage().set(BACK_COOLDOWN, System.currentTimeMillis() + 30000L);
				}
				user.sendMessage("Teleported to your previous location!");
			} else {
				issuer.sendMessage("Teleportation blocked!");
			}
		}, player).withFailureFunction(BossBarTimer.supplierPlayerImmobile(player),
				() -> user.sendMessage("Hold still to lock teleport location!"));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		switch (event.getCause()) {
			case COMMAND:
			case END_GATEWAY:
			case PLUGIN:
			case UNKNOWN:
				break;
			case ENDER_PEARL:
			case NETHER_PORTAL:
			case END_PORTAL:
			case SPECTATE:
			case CHORUS_FRUIT:
			default:
				return;
		}

		User user = core.getUserManager().getUser(event.getPlayer().getUniqueId());
		user.getStorage().set(BACK_LOCATION, event.getFrom());
	}

}
