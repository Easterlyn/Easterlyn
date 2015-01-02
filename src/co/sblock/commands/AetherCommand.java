package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.message.Message;
import co.sblock.chat.message.MessageBuilder;

/**
 * SblockCommand for /aether, the command executed to make IRC chat mimic normal channels.
 * 
 * @author Jikoo
 */
public class AetherCommand extends SblockCommand {

	public AetherCommand() {
		super("aether");
		this.setDescription("For usage in console largely. Talks in #Aether.");
		this.setUsage("/aether <text>");
		this.setPermission("group.horrorterror");
		this.setPermissionMessage("The aetherial realm eludes your grasp once more.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Hey Planar, stop faking empty IRC messages.");
			return true;
		}
		Message message = new MessageBuilder().setSender(ChatColor.WHITE + args[0])
				.setMessage(StringUtils.join(args, ' ', 1, args.length))
				.setChannel(ChannelManager.getChannelManager().getChannel("#Aether")).toMessage();

		message.send(Bukkit.getOnlinePlayers());
		// TODO allow Hal features
		return true;
	}
}
