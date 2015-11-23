package co.sblock.commands.chat;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

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
	private final String chars = "123456789ABCDEFGHIJKLMNPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

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
		String code;
		if (discord.getAuthCodes().containsKey(uuid)) {
			code = discord.getAuthCodes().get(uuid);
		} else {
			code = generateUniqueCode(uuid, discord);
			new BukkitRunnable() {
				@Override
				public void run() {
					discord.getAuthCodes().remove(uuid);
				}
			}.runTaskLater(getPlugin(), 1200L);
		}
		sender.sendMessage(Color.GOOD + "Message the Discord bot \"" + Color.GOOD_EMPHASIS
				+ "/link " + code + Color.GOOD + "\" to complete linking your Discord account!\n"
				+ Color.BAD + "This code will expire in a minute.");
		return true;
	}

	private String generateUniqueCode(UUID uuid, Discord discord) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 6; i++) {
			sb.append(chars.charAt(((Sblock) getPlugin()).getRandom().nextInt(chars.length())));
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
