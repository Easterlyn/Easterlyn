package co.sblock.events.listeners.player;

import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.micromodules.Slack;
import co.sblock.micromodules.Spectators;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;

/**
 * Listener for PlayerCommandPreprocessEvents.
 * 
 * @author Jikoo
 */
public class CommandPreprocessListener implements Listener {

	/**
	 * EventHandler for PlayerCommandPreprocessEvents.
	 * 
	 * @param event the PlayerCommandPreprocessEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("sblock.denizen") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon + 1));
		}

		String command = event.getMessage().toLowerCase().substring(1, space > 0 ? space : event.getMessage().length());

		if (!event.getPlayer().hasPermission("sblock.felt")
				&& !Sblock.getInstance().getConfig().getStringList("slack.command-blacklist").contains(command)) {
			Slack.getInstance().postMessage(event.getPlayer().getName(), event.getPlayer().getUniqueId(),
					event.getPlayer().getName() + " issued command: " + event.getMessage(), false);
		}

		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if ((user instanceof OnlineUser && ((OnlineUser) user).isServer()
				|| Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId()))
				&& (isExecuting(command, "sethome"))) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Color.BAD + "You hear a fizzling noise as your spell fails.");
			return;
		}

		if (isExecuting(command, "gc") && !event.getPlayer().hasPermission("essentials.gc")
				&& !event.getPlayer().hasPermission("essentials.*")) {
			event.setMessage("/tps");
			return;
		}

		if (isExecuting(command, "fly") && (event.getPlayer().hasPermission("essentials.fly")
				|| event.getPlayer().hasPermission("essentials.*"))) {
			event.getPlayer().setFallDistance(0);
		}
	}

	private boolean isExecuting(String executed, String toCheck) {
		Command command = Sblock.getInstance().getCommandMap().getCommand(toCheck);
		if (command ==  null) {
			return false;
		}
		if (executed.equals(command.getName())) {
			return true;
		}
		for (String alias : command.getAliases()) {
			if (executed.equals(alias)) {
				return true;
			}
		}
		return false;
	}
}
