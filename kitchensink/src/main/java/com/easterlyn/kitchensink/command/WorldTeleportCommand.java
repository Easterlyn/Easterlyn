package com.easterlyn.kitchensink.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.command.CoreContexts;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldTeleportCommand extends BaseCommand {

  // TODO remove and replace with /execute as @p in <world>
  @CommandAlias("wtp")
  @Description("{@@sink.module.wtp.description}")
  @CommandPermission("easterlyn.command.wtp")
  @Syntax("<target> <world> <x> <y> <z> [pitch] [yaw]")
  @CommandCompletion("@player @world @decimal @decimal @decimal @decimal @decimal")
  public void teleport(
      @Flags(CoreContexts.ONLINE) Player player,
      World world,
      double x,
      double y,
      double z,
      @Optional Float pitch,
      @Optional Float yaw) {
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
