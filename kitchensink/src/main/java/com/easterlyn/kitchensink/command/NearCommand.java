package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynCore;
import com.easterlyn.command.CoreContexts;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class NearCommand extends BaseCommand {

	private static final int MAX_RADIUS = 200;

	@Dependency
	EasterlynCore core;

	@CommandAlias("near")
	@Description("{@@sink.module.near.description}")
	@Syntax("[range]")
	@CommandCompletion("@integer")
	@CommandPermission("easterlyn.command.near")
	public void near(@Flags(CoreContexts.SELF) Player issuer, @Default("200") int range) {
		range = Math.max(1, range);
		if (!issuer.hasPermission("easterlyn.command.near.far")) {
			range = Math.min(MAX_RADIUS, range);
		}

		List<Player> players = issuer.getWorld().getPlayers();
		if (players.size() <= 1) {
			core.getLocaleManager().sendMessage(issuer, "sink.module.near.none");
			return;
		}

		Location location = issuer.getLocation();
		boolean showSpectate = issuer.hasPermission("easterlyn.command.near.spectate");
		boolean showInvisible = issuer.hasPermission("easterlyn.command.near.invisible");
		double squared = Math.pow(range, 2);
		AtomicInteger matches = new AtomicInteger();
		BaseComponent message = new TextComponent(core.getLocaleManager().getValue("sink.module.near.message",
				core.getLocaleManager().getLocale(issuer)));
		TextComponent separator = new TextComponent(", ");

		players.forEach(player -> {
			if (issuer.getUniqueId().equals(player.getUniqueId()) || !issuer.canSee(player)
					|| !showSpectate && player.getGameMode() == GameMode.SPECTATOR
					|| !showInvisible && player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
				return;
			}
			double distanceSquared = location.distanceSquared(player.getLocation());
			if (distanceSquared > squared) {
				return;
			}
			matches.incrementAndGet();
			int distance = (int) Math.sqrt(distanceSquared);
			message.addExtra(core.getUserManager().getUser(player.getUniqueId()).getMention());
			message.addExtra("(" + distance + ')');
			message.addExtra(separator);
		});

		if (message.getExtra() == null || message.getExtra().size() == 0) {
			core.getLocaleManager().sendMessage(issuer, "sink.module.near.none");
			return;
		}

		// Remove trailing comma component
		message.getExtra().remove(message.getExtra().size() - 1);

		issuer.spigot().sendMessage(message);
	}

}
