package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.events.region.NetherPortalAgent;
import com.easterlyn.users.UserRank;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for spawning a nether portal.
 *
 * @author Jikoo
 */
public class NetherPortalTestCommand extends EasterlynCommand {

	public NetherPortalTestCommand(Easterlyn plugin) {
		super(plugin, "netherportaltest");
		this.setPermissionLevel(UserRank.ADMIN);

	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;

		NetherPortalAgent agent = new NetherPortalAgent();
		agent.setFrom(player.getLocation().getBlock());
		return agent.createPortal(player.getLocation());
	}
}
