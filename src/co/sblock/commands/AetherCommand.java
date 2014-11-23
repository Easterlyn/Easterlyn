package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.chat.ChannelManager;
import co.sblock.chat.Message;
import co.sblock.users.User;
import co.sblock.users.UserManager;

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
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (args.length < 2) {
			sender.sendMessage(ChatColor.RED + "Hey Planar, stop faking empty IRC messages.");
			return true;
		}
		Message message = new Message(ChatColor.WHITE + args[0].replaceAll("<(.*?)>", "$1"), StringUtils.join(args, ' ', 1, args.length));
		message.setChannel(ChannelManager.getChannelManager().getChannel("#Aether"));
		// Rather than call message.send() and limit recipients to #Aether, this is supposed to be global.
		// TODO allow Hal features
		Bukkit.getConsoleSender().sendMessage("[#Aether] " + message.getConsoleMessage());
		for (Player p : Bukkit.getOnlinePlayers()) {
			User u = UserManager.getUser(p.getUniqueId());
			if (!u.isSuppressing()) {
				u.rawHighlight(message.getMessage());
			}
		}
		return true;
	}
}
