package com.easterlyn.kitchensink.combo;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import co.aikar.locales.MessageKey;
import com.easterlyn.EasterlynCore;
import com.easterlyn.EasterlynKitchenSink;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.User;
import com.easterlyn.util.BossBarTimer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;
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
	private EasterlynCore core;
	@Dependency
	private EasterlynKitchenSink sink;

	@CommandAlias("back|b")
	@Description("{@@sink.module.back.description}")
	@CommandPermission("easterlyn.command.back.self")
	@Syntax("[target]")
	@CommandCompletion("@player")
	public void back(BukkitCommandIssuer issuer, @Flags(CoreContexts.ONLINE_WITH_PERM) User user) {
		boolean other = !issuer.getUniqueId().equals(user.getUniqueId());

		if (!other && user.getStorage().getLong(BACK_COOLDOWN) >= System.currentTimeMillis()) {
			SimpleDateFormat dateFormat = new SimpleDateFormat("m:ss");
			issuer.sendInfo(MessageKey.of("sink.module.back.error.cooldown"), "{value}",
					dateFormat.format(new Date(user.getStorage().getLong(BACK_COOLDOWN) - System.currentTimeMillis())));
			return;
		}

		Location back = user.getStorage().getSerializable(BACK_LOCATION, Location.class);

		if (back == null) {
			issuer.sendInfo(MessageKey.of("sink.module.back.error.missing"));
			return;
		}

		Player player = user.getPlayer();
		if (player == null) {
			issuer.sendInfo(MessageKey.of("sink.common.teleport.player_unloaded"));
			return;
		}

		String title = core.getLocaleManager().getValue("sink.common.teleport.bar_title");
		if (title == null) {
			title = "";
		}

		new BossBarTimer(title, () -> {
			if (player.teleport(back)) {
				if (other) {
					issuer.sendInfo(MessageKey.of("sink.module.back.success_other"),
							"{target}", player.getName());
				} else {
					user.getStorage().set(BACK_COOLDOWN, System.currentTimeMillis() + 300000L);
				}
				core.getLocaleManager().sendMessage(player, "sink.module.back.success");
			} else {
				issuer.sendInfo(MessageKey.of("sink.common.teleport.blocked"));
			}
		}, player).withFailureFunction(BossBarTimer.supplierPlayerImmobile(player),
				() -> core.getLocaleManager().sendMessage(player, "sink.common.teleport.movement"))
				.schedule(sink, BACK_COOLDOWN + '.' + player.getUniqueId(), 4, TimeUnit.SECONDS);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onPlayerTeleport(PlayerTeleportEvent event) {
		switch (event.getCause()) {
			case COMMAND:
			case END_GATEWAY:
			case PLUGIN:
				break;
			case CHORUS_FRUIT:
			case END_PORTAL:
			case ENDER_PEARL:
			case NETHER_PORTAL:
			case SPECTATE:
			case UNKNOWN:
			default:
				return;
		}

		User user = core.getUserManager().getUser(event.getPlayer().getUniqueId());
		user.getStorage().set(BACK_LOCATION, event.getFrom());
	}

}
