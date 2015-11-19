package co.sblock.commands.chat;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

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

	private final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private final Random random = new Random();

	public DiscordLinkCommand() {
		super("link");
		setDescription("Generate a code to link your Discord account with Minecraft");
		setUsage(ChatColor.AQUA + "/link");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Discord discord = Discord.getInstance();
		if (!discord.isEnabled()) {
			sender.sendMessage(Color.BAD + "Discord link is currently nonfunctional!");
			return true;
		}
		String code = generateUniqueCode(((Player) sender).getUniqueId(), discord);
		sender.sendMessage(Color.GOOD + "Message the Discord bot \"" + Color.GOOD_EMPHASIS
				+ "/link " + code + Color.GOOD + "\" to complete linking your Discord account!\n"
				+ Color.BAD + "This code will expire in a minute.");
		return true;
	}

	private String generateUniqueCode(UUID uuid, Discord discord) {
		if (discord.getAuthCodes().containsKey(uuid)) {
			return discord.getAuthCodes().get(uuid);
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(chars.charAt(random.nextInt(chars.length())));
		}
		String code = sb.toString();
		if (discord.getAuthCodes().containsValue(code)) {
			return generateUniqueCode(uuid, discord);
		}
		discord.getAuthCodes().put(uuid, code);
		return code;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}

}
