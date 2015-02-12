package co.sblock.events.listeners;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.sblock.Sblock;
import co.sblock.users.OfflineUser;
import co.sblock.users.OnlineUser;
import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerCommandPreprocessEvents.
 * 
 * @author Jikoo
 */
public class PlayerCommandPreprocessListener implements Listener {

	/**
	 * EventHandler for PlayerCommandPreprocessEvents.
	 * 
	 * @param event the PlayerCommandPreprocessEvent
	 */
	@EventHandler
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("sblock.denizen") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon));
		}

		String command = event.getMessage().toLowerCase().substring(1, space > 0 ? space : event.getMessage().length());

		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if ((user instanceof OnlineUser && ((OnlineUser) user).isServer()
				|| Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId()))
				&& (isExecuting(command, "sethome"))) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You hear a fizzling noise as your spell fails.");
			return;
		}

		if (isExecuting(command, "gc") && !event.getPlayer().hasPermission("sblock.helper")) {
			event.setMessage("/tps");
			return;
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
