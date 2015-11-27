package co.sblock.commands.chat;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;
import co.sblock.discord.Discord;

import net.md_5.bungee.api.ChatColor;

/**
 * 
 * 
 * @author Jikoo
 */
public class DiscordLinkCommand extends SblockCommand {

	private final Discord discord;

	public DiscordLinkCommand(Sblock plugin) {
		super(plugin, "link");
		setDescription("Generate a code to link your Discord account with Minecraft");
		setUsage(ChatColor.AQUA + "/link");
		this.discord = plugin.getModule(Discord.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		if (!discord.isEnabled()) {
			sender.sendMessage(Color.BAD + "Discord link is currently nonfunctional!");
			return true;
		}
		UUID uuid = ((Player) sender).getUniqueId();
		Object code;
		try {
			code = discord.getAuthCodes().get(uuid);
		} catch (ExecutionException e) {
			// Just re-throw the exception to use our automatic report creation feature
			throw new RuntimeException(e);
		}
		sender.sendMessage(Color.GOOD + "Message the Discord bot \"" + Color.GOOD_EMPHASIS
				+ "/link " + code + Color.GOOD + "\" to complete linking your Discord account!\n"
				+ Color.BAD + "This code will expire in a minute.");
		return true;
	}
	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
