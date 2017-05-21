package com.easterlyn.events.listeners.player;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.Language;
import com.easterlyn.commands.utility.OopsCommand;
import com.easterlyn.discord.Discord;
import com.easterlyn.discord.DiscordPlayer;
import com.easterlyn.events.listeners.EasterlynListener;
import com.easterlyn.micromodules.AwayFromKeyboard;
import com.easterlyn.micromodules.Spectators;
import com.easterlyn.users.UserRank;
import com.easterlyn.utilities.PermissionUtils;

import org.bukkit.command.Command;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

/**
 * Listener for PlayerCommandPreprocessEvents.
 *
 * @author Jikoo
 */
public class CommandPreprocessListener extends EasterlynListener {

	private final AwayFromKeyboard afk;
	private final Chat chat;
	private final Discord discord;
	private final Language lang;
	private final Spectators spectators;
	private final SimpleCommandMap map;

	public CommandPreprocessListener(final Easterlyn plugin) {
		super(plugin);
		this.afk = plugin.getModule(AwayFromKeyboard.class);
		this.chat = plugin.getModule(Chat.class);
		this.discord = plugin.getModule(Discord.class);
		this.lang = plugin.getModule(Language.class);
		this.spectators = plugin.getModule(Spectators.class);
		this.map = plugin.getCommandMap();

		PermissionUtils.addParent("easterlyn.commands.unfiltered", UserRank.HEAD_MOD.getPermission());
		PermissionUtils.addParent("easterlyn.commands.unfiltered", "easterlyn.spam");
		PermissionUtils.addParent("easterlyn.commands.unlogged", UserRank.MOD.getPermission());
		PermissionUtils.addParent("easterlyn.commands.unlogged", "easterlyn.spam");
	}

	/**
	 * EventHandler for PlayerCommandPreprocessEvents.
	 *
	 * @param event the PlayerCommandPreprocessEvent
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
		this.afk.extendActivity(event.getPlayer());

		int colon = event.getMessage().indexOf(':');
		int space = event.getMessage().indexOf(' ');
		if (!event.getPlayer().hasPermission("easterlyn.commands.unfiltered") && 0 < colon && (colon < space || space < 0)) {
			event.setMessage("/" + event.getMessage().substring(colon + 1));
			if (space > 0) {
				space -= colon;
			}
		}

		String command = event.getMessage().substring(1, space > 0 ? space : event.getMessage().length()).toLowerCase();
		Command cmd = this.map.getCommand(command);

		if (!event.getPlayer().hasPermission("easterlyn.commands.unlogged") && cmd != null
				&& !this.discord.getConfig().getStringList("discord.command-log-blacklist").contains(cmd.getName())) {
			this.discord.log(event.getPlayer().getName() + " issued command: " + event.getMessage());
		}

		if (((OopsCommand) this.map.getCommand("oops"))
				.handleFailedCommand(event.getPlayer(), command, space > 0
						? event.getMessage().substring(space + 1) : null)) {
			event.setCancelled(true);
			return;
		}

		if (cmd == null) {
			return;
		}

		if (cmd.getName().equals("gc") && !event.getPlayer().hasPermission("essentials.gc")
				&& !event.getPlayer().hasPermission("essentials.*")) {
			event.setMessage("/tps");
			command = "tps";
			cmd = this.map.getCommand(command);
			space = -1;
		}

		if (cmd.getName().equals("sethome")) {
			if (this.spectators.isSpectator(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(this.lang.getValue("events.command.spectatefail"));
				return;
			}
		} else if (cmd.getName().equals("warp")) {
			if (space > 1) {
				String[] args = event.getMessage().split(" ");
				if (args.length >= 2) {
					if (!event.getPlayer().hasPermission("essentials.warps." + args[1])
							&& !event.getPlayer().hasPermission("essentials.warps.*")
							&& !event.getPlayer().hasPermission("essentials.*")) {
						event.setCancelled(true);
						event.getPlayer().sendMessage(this.lang.getValue("events.command.spectatefail"));
						return;
					}
				}
			}
		} else if (cmd.getName().equals("mail")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("send")
					&& this.chat.testForMute(event.getPlayer())) {
				event.setCancelled(true);
				return;
			}
		} else if (cmd.getName().equals("prism")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("undo")) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(this.lang.getValue("events.command.prismUndoCrash"));
				return;
			}
		} else if (cmd.getName().equals("party")) {
			if (space > 0 && event.getMessage().substring(space + 1).toLowerCase().startsWith("chat")) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(
						Language.getColor("command") + "/channel new <name> <access> <type>"
								+ Language.getColor("neutral") + ": Create a new channel.");
				return;
			}
		}

		if (event.getPlayer() instanceof DiscordPlayer) {
			if (!this.discord.getConfig().getStringList("discord.command-whitelist").contains(cmd.getName())) {
				event.getPlayer().sendMessage('/' + cmd.getName() + " isn't allowed from Discord, sorry!");
				event.setCancelled(true);
				return;
			}
		}

	}

}
