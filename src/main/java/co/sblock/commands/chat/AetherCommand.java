package co.sblock.commands.chat;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockAsynchronousCommand;
import co.sblock.discord.Discord;
import co.sblock.events.event.SblockAsyncChatEvent;
import co.sblock.users.Users;
import co.sblock.utilities.WrappedSenderPlayer;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

/**
 * SblockCommand for /aether, the command executed to make IRC chat mimic normal channels.
 * 
 * @author Jikoo
 */
public class AetherCommand extends SblockAsynchronousCommand {

	private final Discord discord;
	private final Users users;
	private final BaseComponent[] hover;
	private final Channel aether;

	public AetherCommand(Sblock plugin) {
		super(plugin, "aether");
		this.discord = plugin.getModule(Discord.class);
		this.users = plugin.getModule(Users.class);
		this.setAliases("aetherme");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setUsage("/aether <text>");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");

		hover = TextComponent.fromLegacyText(getLang().getValue("command.aether.hover"));
		aether = plugin.getModule(Chat.class).getChannelManager().getChannel("#Aether");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage("Hey Adam, stop faking empty IRC messages.");
			return true;
		}

		sendAether(sender, args[0], StringUtils.join(args, ' ', 1, args.length), label.equals("aetherme"));
		return true;
	}

	public void sendAether(CommandSender sender, String name, String msg, boolean thirdPerson) {

		// set channel before and after to prevent @channel changing while also stripping invalid characters
		MessageBuilder builder = new MessageBuilder((Sblock) getPlugin())
				.setSender(ChatColor.WHITE + name).setChannel(aether).setMessage(msg)
				.setChannel(aether).setChannelClick("@# ").setNameClick("@# ").setNameHover(hover)
				.setThirdPerson(thirdPerson);

		if (!builder.canBuild(false)) {
			return;
		}

		Message message = builder.toMessage();

		Set<Player> players = new HashSet<>(Bukkit.getOnlinePlayers());
		players.removeIf(p -> users.getUser(p.getUniqueId()).getSuppression());

		WrappedSenderPlayer senderPlayer = new WrappedSenderPlayer((Sblock) getPlugin(),
				sender == null ? Bukkit.getConsoleSender() : sender, name);

		SblockAsyncChatEvent event = new SblockAsyncChatEvent(true, senderPlayer, players, message);
		Bukkit.getPluginManager().callEvent(event);
		if (!event.isCancelled() || event.isGlobalCancelled()) {
			discord.postMessage(senderPlayer.getDisplayName(), message.getDiscordMessage(), discord.getMainChannel());
		}
	}
}
