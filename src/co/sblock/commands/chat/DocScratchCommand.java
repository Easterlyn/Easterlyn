package co.sblock.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for being Doc Scratch.
 * 
 * @author Jikoo
 */
public class DocScratchCommand extends SblockCommand {

	public DocScratchCommand(Sblock plugin) {
		super(plugin, "o");
		this.setDescription("&a> Be the white text guy");
		this.setUsage("/o <text>");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("&f&l[o] You try to be the white text guy, but fail to be the white text guy. "
					+ "No one can be the white text guy except for the white text guy.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args == null || args.length == 0) {
			sender.sendMessage(ChatColor.BOLD + "[o] If you're going to speak for me, please proceed.");
			return true;
		}
		StringBuilder o = new StringBuilder(ChatColor.BOLD.toString()).append("[o] ");
		for (String s : args) {
			o.append(s).append(' ');
		}
		Bukkit.broadcastMessage(o.substring(0, o.length() - 1 > 0 ? o.length() - 1 : 0));
		return true;
	}
}
