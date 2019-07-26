package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.user.UserRank;
import com.easterlyn.util.PermissionUtil;
import java.util.List;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class NearCommand extends BaseCommand {

	private final int maxRadius;

	public NearCommand() {
		PermissionUtil.addParent("easterlyn.command.near.spectators", UserRank.MODERATOR.getPermission());
		PermissionUtil.addParent("easterlyn.command.near.invisible", UserRank.MODERATOR.getPermission());
		PermissionUtil.addParent("easterlyn.command.near.far", UserRank.MODERATOR.getPermission());
		maxRadius = 200;
	}

	@CommandAlias("near")
	@Description("List nearby players.")
	@Syntax("/near [range]")
	@CommandCompletion("@integer")
	@CommandPermission("easterlyn.command.near")
	public void near(@Flags(CoreContexts.SELF) Player issuer, @Default("200") int range) {
		range = Math.max(1, range);
		if (!issuer.hasPermission("easterlyn.command.near.far")) {
			range = Math.min(maxRadius, range);
		}

		Location location = issuer.getLocation();
		boolean showSpectate = issuer.hasPermission("easterlyn.command.near.spectate");
		boolean showInvisible = issuer.hasPermission("easterlyn.command.near.invisible");
		double squared = Math.pow(range, 2);

		StringBuilder builder = new StringBuilder("Nearby players: ");

		List<Player> players = issuer.getWorld().getPlayers();
		if (players.size() <= 1) {
			builder.append("none");
			issuer.sendMessage(builder.toString());
			return;
		}

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
			int distance = (int) Math.sqrt(distanceSquared);
			builder.append(player.getDisplayName()).append('(').append(distance).append("), ");
		});

		if (builder.charAt(builder.length() - 2) == ',') {
			builder.delete(builder.length() - 2, builder.length());
		}

		issuer.sendMessage(builder.toString());
	}

}
