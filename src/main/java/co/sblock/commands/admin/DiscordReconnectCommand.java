package co.sblock.commands.admin;

import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;

/**
 * Command for re-connecting the Discord integration.
 * 
 * @author Jikoo
 */
public class DiscordReconnectCommand extends SblockCommand {

	private final Discord discord;

	public DiscordReconnectCommand(Sblock plugin) {
		super(plugin, "dc-reconnect");
		this.discord = plugin.getModule(Discord.class);
		this.setDescription("Reconnect the Discord bot.");
		this.setPermissionLevel("denizen");
		this.setPermissionMessage("But why do you need to disconnect and reconnect?");
		this.setUsage("/dc-reconnect");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (discord.disable().enable().isEnabled()) {
			sender.sendMessage("Reconnected successfully.");
		} else {
			sender.sendMessage("Reconnection failed. Check console.");
		}
		return true;
	}

}
