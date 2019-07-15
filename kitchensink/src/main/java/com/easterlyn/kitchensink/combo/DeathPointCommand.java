package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import com.easterlyn.BossBarTimer;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CommandRank;
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
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathPointCommand extends BaseCommand implements Listener {

	private static final String DEATH_LOCATION = "kitchensink.deathLocation";
	public static final String DEATH_COOLDOWN = "kitchensink.deathTime";

	@Dependency
	EasterlynCore core;

	public DeathPointCommand() {
		PermissionUtil.addParent("easterlyn.command.death.other", UserRank.MODERATOR.getPermission());
	}

	@CommandAlias("death")
	@Description("Teleport to your last death!")
	@CommandPermission("easterlyn.command.death")
	@CommandRank(UserRank.MODERATOR)
	public void back(BukkitCommandIssuer issuer, User user) {
		User target;
		if (issuer.isPlayer() && !issuer.hasPermission("easterlyn.command.death.other")
				&& !issuer.getUniqueId().equals(user.getUniqueId())) {
			target = core.getUserManager().getUser(issuer.getUniqueId());
		} else {
			target = user;
		}
		boolean other = !issuer.getUniqueId().equals(target.getUniqueId());

		// TODO rich messages
		if (!other && target.getStorage().getLong(DEATH_COOLDOWN) >= System.currentTimeMillis()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss");
			target.sendMessage("You're on cooldown for `/death`! Next usage in "
					+ dateFormat.format(new Date(target.getStorage().getLong(DEATH_COOLDOWN) - System.currentTimeMillis())));
			return;
		}

		Location back = target.getStorage().getSerializable(DEATH_LOCATION, Location.class);

		if (back == null) {
			issuer.sendMessage("No death location!");
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
					issuer.sendMessage("Teleported " + target.getDisplayName() + " to their last death location.");
				} else {
					target.getStorage().set(DEATH_COOLDOWN, System.currentTimeMillis() + 30000L);
				}
				target.sendMessage("Teleported to your last death!");
			} else {
				issuer.sendMessage("Teleportation blocked!");
			}
		}, player).withFailureFunction(BossBarTimer.supplierPlayerImmobile(player),
				() -> target.sendMessage("Hold still to lock teleport location!"));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerDeathEvent event) {
		User user = core.getUserManager().getUser(event.getEntity().getUniqueId());
		user.getStorage().set(DEATH_LOCATION, event.getEntity().getLocation());
	}

}
