package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CommandRank;
import com.easterlyn.user.UserRank;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldTeleportCommand extends BaseCommand {

	@CommandAlias("wtp")
	@Description("Teleport to a very specific location.")
	@Syntax("/wtp <target> <world> <x> <y> <z> [pitch] [yaw]")
	@CommandCompletion("@player @world @decimal @decimal @decimal @decimal @decimal")
	@CommandPermission("easterlyn.command.wtp")
	@CommandRank(UserRank.MODERATOR)
	public void teleport(Player player, World world, double x, double y, double z, @Optional Float pitch, @Optional Float yaw) {
		Location location = new Location(world, x, y, z);
		if (pitch != null) {
			location.setPitch(pitch);
		}
		if (yaw != null) {
			location.setYaw(yaw);
		}
		player.teleport(location);
	}

}
