package co.sblock.events.listeners.player;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.commands.utility.OopsCommand;
import co.sblock.discord.Discord;
import co.sblock.discord.DiscordPlayer;
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

	// future constructor dependency injection
	private final Sblock sblock = Sblock.getInstance();
	private final SimpleCommandMap map = sblock.getCommandMap();

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

		String command = event.getMessage().substring(1, space > 0 ? space : event.getMessage().length()).toLowerCase();

		if (!event.getPlayer().hasPermission("sblock.felt")
				&& !sblock.getConfig().getStringList("discord.command-blacklist").contains(command)) {
			Discord.getInstance().postMessage(event.getPlayer().getName(),
					event.getPlayer().getName() + " issued command: " + event.getMessage(), false);
		}

		if (((OopsCommand) map.getCommand("oops"))
				.handleFailedCommand(event.getPlayer(), command, space > 0
						? event.getMessage().substring(space + 1) : null)) {
			event.setCancelled(true);
			return;
		}

		Command cmd = map.getCommand(command);
		if (cmd == null) {
			return;
		}

		OfflineUser user = Users.getGuaranteedUser(event.getPlayer().getUniqueId());
		if ((user instanceof OnlineUser && ((OnlineUser) user).isServer()
				|| Spectators.getInstance().isSpectator(event.getPlayer().getUniqueId()))
				&& (cmd.getName().equals("sethome"))) {
			event.setCancelled(true);
			event.getPlayer().sendMessage(Color.BAD + "You hear a fizzling noise as your spell fails.");
			return;
		}

		if (cmd.getName().equals("afk")) {
			if (Chat.getChat().testForMute(event.getPlayer())) {
				event.setCancelled(true);
			}
		} else if (cmd.getName().equals("mail")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("send")
					&& Chat.getChat().testForMute(event.getPlayer())) {
				event.setCancelled(true);
			}
		} else if (cmd.getName().equals("prism")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("undo")) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(Color.BAD + "Restore, don't undo.");
			}
		} else if (cmd.getName().equals("fly")) {
			if ((event.getPlayer().hasPermission("essentials.fly")
					|| event.getPlayer().hasPermission("essentials.*"))) {
				event.getPlayer().setFallDistance(0);
			}
		} else if (cmd.getName().equals("gc")) {
			if (!event.getPlayer().hasPermission("essentials.gc")
					&& !event.getPlayer().hasPermission("essentials.*")) {
				event.setMessage("/tps");
				command = "tps";
			}
		}

		if (event.getPlayer() instanceof DiscordPlayer) {
			if (!sblock.getConfig().getStringList("discord.command-whitelist").contains(cmd.getName())) {
				event.getPlayer().sendMessage('/' + cmd.getName() + " isn't allowed from Discord, sorry!");
				event.setCancelled(true);
			}
		}

	}
}
