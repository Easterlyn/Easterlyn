package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.easterlyn.BossBarTimer;
import com.easterlyn.EasterlynCore;
import com.easterlyn.user.User;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
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

	public BackCommand() {
		PermissionUtil.addParent("easterlyn.command.back.other", UserRank.MODERATOR.getPermission());
	}

	@CommandAlias("back|b")
	@Description("Teleport to your previous location!")
	@CommandPermission("easterlyn.command.back")
	public void back(BukkitCommandIssuer issuer, User user) {
		User target;
		if (issuer.isPlayer() && !issuer.hasPermission("easterlyn.command.back.other")
				&& !issuer.getUniqueId().equals(user.getUniqueId())) {
			target = core.getUserManager().getUser(issuer.getUniqueId());
		} else {
			target = user;
		}
		boolean other = !issuer.getUniqueId().equals(target.getUniqueId());

		// TODO rich messages
		if (!other && target.getStorage().getLong(BACK_COOLDOWN) >= System.currentTimeMillis()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss");
			target.sendMessage("You're on cooldown for `/back`! Next usage in "
					+ dateFormat.format(new Date(target.getStorage().getLong(BACK_COOLDOWN) - System.currentTimeMillis())));
			return;
		}

		Location back = target.getStorage().getSerializable(BACK_LOCATION, Location.class);

		if (back == null) {
			issuer.sendMessage("No back location!");
			return;
		}

		Player player = target.getPlayer();
		if (player == null) {
			issuer.sendMessage("Cannot teleport at this time, player not loaded.");
			return;
		}

		new BossBarTimer("Preparing to Teleport...", () -> {
			if (player.teleport(back)) {
				if (other) {
					issuer.sendMessage("Teleported " + target.getDisplayName() + " to their previous location.");
				} else {
					target.getStorage().set(BACK_COOLDOWN, System.currentTimeMillis() + 30000L);
				}
				target.sendMessage("Teleported to your previous location!");
			} else {
				issuer.sendMessage("Teleportation blocked!");
			}
		}, player).withFailureFunction(BossBarTimer.supplierPlayerImmobile(player),
				() -> target.sendMessage("Hold still to lock teleport location!"));
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
