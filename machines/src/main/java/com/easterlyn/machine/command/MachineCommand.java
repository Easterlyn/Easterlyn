package com.easterlyn.machine.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.MessageKeys;
import co.aikar.commands.MessageType;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import com.easterlyn.EasterlynMachines;
import com.easterlyn.command.CommandRank;
import com.easterlyn.machine.Machine;
import com.easterlyn.user.UserRank;

@CommandAlias("machine")
@Description("Machinations.")
@CommandPermission("easterlyn.command.machine")
@CommandRank(UserRank.ADMIN)
public class MachineCommand extends BaseCommand {

	@Dependency
	private EasterlynMachines plugin;

	@Default
	@Private
	public void getMachine(BukkitCommandIssuer issuer, String machineName) {
		if (!issuer.isPlayer()) {
			issuer.sendMessage(MessageType.ERROR, MessageKeys.NOT_ALLOWED_ON_CONSOLE);
			return;
		}

		Machine machine = plugin.getByName(machineName);

		if (machine == null) {
			issuer.sendMessage("Invalid machine type!");
			return;
		}

		issuer.getPlayer().getWorld().dropItem(issuer.getPlayer().getLocation(), machine.getUniqueDrop()).setPickupDelay(0);
	}

}
