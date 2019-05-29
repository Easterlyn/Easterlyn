package com.easterlyn.commands.chat;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.chat.channel.Channel;
import com.easterlyn.chat.message.Message;
import com.easterlyn.chat.message.MessageBuilder;
import com.easterlyn.commands.EasterlynAsynchronousCommand;
import com.easterlyn.discord.Discord;
import com.easterlyn.events.event.EasterlynAsyncChatEvent;
import com.easterlyn.users.UserRank;
import com.easterlyn.users.Users;
import com.easterlyn.utilities.TextUtils;
import com.easterlyn.utilities.player.WrappedSenderPlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

/**
 * EasterlynCommand for /aether, the command executed to make IRC chat mimic normal channels.
 *
 * @author Jikoo
 */
public class AetherCommand extends EasterlynAsynchronousCommand {

	private final Discord discord;
	private final Users users;
	private final BaseComponent[] hover;
	private final Channel aether;

	public AetherCommand(Easterlyn plugin) {
		super(plugin, "aether");
		this.setAliases("aetherme");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setPermissionLevel(UserRank.HEAD_ADMIN);
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");
		this.setUsage("/aether <text>");
		this.discord = plugin.getModule(Discord.class);
		this.users = plugin.getModule(Users.class);

		hover = TextComponent.fromLegacyText(getLang().getValue("command.aether.hover"));
		aether = plugin.getModule(Chat.class).getChannelManager().getChannel("#Aether");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Hey Adam, stop faking empty IRC messages.");
			return true;
		}

		// set channel before and after to prevent @channel changing while also stripping invalid characters
		MessageBuilder builder = new MessageBuilder((Easterlyn) getPlugin())
				.setSender(ChatColor.WHITE + args[0]).setChannel(aether)
				.setMessage(TextUtils.join(args, ' ', 1, args.length))
				.setChannel(aether).setChannelClick("@# ").setNameClick("@# ").setNameHover(hover)
				.setThirdPerson(label.equals("aetherme"));

		if (builder.canNotBuild(false)) {
			return false;
		}

		Message message = builder.toMessage();

		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> users.getUser(p.getUniqueId()).getSuppression());

		WrappedSenderPlayer senderPlayer = new WrappedSenderPlayer((Easterlyn) getPlugin(),
				sender == null ? Bukkit.getConsoleSender() : sender, args[0]);

		EasterlynAsyncChatEvent event = new EasterlynAsyncChatEvent(true, senderPlayer, players, message);

		if (Bukkit.isPrimaryThread()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getPluginManager().callEvent(event);
					if (!event.isCancelled() || event.isGlobalCancelled()) {
						discord.postMessage(senderPlayer.getDisplayName(), message.getDiscordMessage(), discord.getMainChannelIDs());
					}
				}
			}.runTaskAsynchronously(getPlugin());
		} else {
			Bukkit.getPluginManager().callEvent(event);
			if (!event.isCancelled() || event.isGlobalCancelled()) {
				discord.postMessage(senderPlayer.getDisplayName(), message.getDiscordMessage(), discord.getMainChannelIDs());
			}
		}

		return true;
	}

}
