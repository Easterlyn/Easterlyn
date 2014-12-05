package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Message;
import co.sblock.users.OfflineUser;
import co.sblock.users.Users;

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
		Message message = new Message(ChatColor.WHITE + args[0], StringUtils.join(args, ' ', 1, args.length));
		message.setChannel(ChannelManager.getChannelManager().getChannel("#Aether"));
		// Rather than call message.send() and limit recipients to #Aether, this is supposed to be global.
		// TODO allow Hal features
		Bukkit.getConsoleSender().sendMessage(message.getConsoleMessage());
		for (Player p : Bukkit.getOnlinePlayers()) {
			OfflineUser u = Users.getGuaranteedUser(p.getUniqueId());
			if (!u.getSuppression()) {
				u.rawHighlight(message.getMessage());
			}
		}
		return true;
	}
}
