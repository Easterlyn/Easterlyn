package com.easterlyn.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.BukkitCommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Dependency;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Private;
import com.easterlyn.Easterlyn;
import com.easterlyn.event.ReportableEvent;
import java.text.DecimalFormat;
import org.bukkit.Location;

@CommandAlias("report")
@Description("Create a report for staff.")
@CommandPermission("easterlyn.command.report")
public class ReportCommand extends BaseCommand {

	@Dependency
	private Easterlyn easterlyn;
	private final DecimalFormat format = new DecimalFormat("#.##");

	@Default
	@Private
	public void report(BukkitCommandIssuer issuer, String args) {
		String message = "Report by " + issuer.getIssuer().getName();
		if (issuer.isPlayer()) {
			Location location = issuer.getPlayer().getLocation();
			message += String.format(" - /wtp @p %s %.2f %.2f %.2f %.2f %.2f", location.getWorld().getName(),
					location.getX(), location.getY(), location.getZ(), location.getPitch(), location.getYaw());
		}
		message += '\n' + args;

		if (ReportableEvent.getHandlerList().getRegisteredListeners().length == 0) {
			// TODO rich text
			issuer.sendMessage("No report handlers are enabled! Please use an alternate contact method.");
		}

		easterlyn.getServer().getPluginManager().callEvent(new ReportableEvent(message));
	}

}
