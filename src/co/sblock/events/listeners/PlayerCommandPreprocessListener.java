package co.sblock.events.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.sblock.Sblock;
import co.sblock.users.Users;
import co.sblock.utilities.spectator.Spectators;

/**
 * Listener for PlayerCommandPreprocessEvents.
 * 
 * @author Jikoo
 */
public class PlayerCommandPreprocessListener implements Listener {

	private final SimpleDateFormat time =  new SimpleDateFormat("m:ss");
	private final HashMap<UUID, Long> tpacooldown = new HashMap<>();

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

		if ((Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId())
				|| Users.getGuaranteedUser(event.getPlayer().getUniqueId()).isServer())
				&& (isExecuting(command, "sethome") || isExecuting(command, "tpahere") || isExecuting(command, "tpaccept"))) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(ChatColor.RED + "You hear a fizzling noise as your spell fails.");
			return;
		}

		if (!event.getPlayer().hasPermission("sblock.helper") && (isExecuting(command, "tpa") || isExecuting(command, "tpahere"))) {
			if (tpacooldown.containsKey(event.getPlayer().getUniqueId())
					&& tpacooldown.get(event.getPlayer().getUniqueId()) > System.currentTimeMillis()) {
				event.getPlayer().sendMessage(
						ChatColor.RED + "You cannot send a teleport request for another " + ChatColor.GOLD
						+ time.format(new Date(tpacooldown.get(event.getPlayer().getUniqueId()) - System.currentTimeMillis()))
						+ ChatColor.RED + ".");
				event.setCancelled(true);
			} else {
				// 10 minutes * 60 seconds * 1000 ms
				tpacooldown.put(event.getPlayer().getUniqueId(), System.currentTimeMillis() + 600000L);
			}
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
