package co.sblock.commands.chat;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock;
import co.sblock.chat.Chat;
import co.sblock.chat.Color;
import co.sblock.chat.channel.Channel;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;
import co.sblock.commands.SblockAsynchronousCommand;
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

	private final BaseComponent[] hover;
	private final WrappedSenderPlayer sender;
	private final Channel aether;

	public AetherCommand(Sblock plugin) {
		super(plugin, "aether");
		this.setAliases("aetherme");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setUsage("/aether <text>");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");

		hover = TextComponent.fromLegacyText(Color.GOOD_EMPHASIS + "IRC Chat\n"
				+ Color.GOOD + "Server: irc.freenode.net\n"
				+ Color.GOOD + "Channel: #sblockserver");
		sender = new WrappedSenderPlayer(plugin, Bukkit.getConsoleSender());
		aether = plugin.getModule(Chat.class).getChannelManager().getChannel("#Aether");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(Color.BAD + "Hey Planar, stop faking empty IRC messages.");
			return true;
		}

		sendAether(args[0], StringUtils.join(args, ' ', 1, args.length), label.equals("aetherme"));
		return true;
	}

	public void sendAether(String name, String msg, boolean thirdPerson) {

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
		players.removeIf(p -> Users.getGuaranteedUser(((Sblock) getPlugin()), p.getUniqueId()).getSuppression());

		// CHAT: Verify that this does not cause concurrency issues (It totally does)
		sender.setDisplayName(name);

		Bukkit.getPluginManager().callEvent(new SblockAsyncChatEvent(false, sender, players, message));
	}
}
