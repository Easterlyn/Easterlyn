package com.easterlyn.machine.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Flags;
import co.aikar.commands.annotation.Syntax;
import com.easterlyn.EasterlynMachines;
import com.easterlyn.command.CoreContexts;
import com.easterlyn.machine.Machine;
import org.bukkit.entity.Player;

public class MachineCommand extends BaseCommand {

  @Dependency private EasterlynMachines plugin;

  @CommandAlias("machine")
  @Description("Machinations.")
  @CommandPermission("easterlyn.command.machine")
  @Syntax("<machine>")
  @CommandCompletion("@machines")
  public void getMachine(@Flags(CoreContexts.SELF) Player player, String machineName) {
    Machine machine = plugin.getByName(machineName);

    if (machine == null) {
      player.sendMessage("Invalid machine type!");
      return;
    }

    player.getWorld().dropItem(player.getLocation(), machine.getUniqueDrop()).setPickupDelay(0);
  }
}
