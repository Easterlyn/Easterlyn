package com.easterlyn.commands.admin;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Color;
import com.easterlyn.commands.SblockCommand;
import com.easterlyn.users.UserRank;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import net.md_5.bungee.api.ChatColor;

/**
 * SblockCommand for making people want to gouge their eyes out.
 * 
 * @author Jikoo
 */
public class LordEnglishEyefuckCommand extends SblockCommand {

	public LordEnglishEyefuckCommand(Easterlyn plugin) {
		super(plugin, "lel");
		this.setDescription("&e/le, now with 250% more &kbrain pain&e.");
		this.setPermissionLevel(UserRank.HORRORTERROR);
		this.setPermissionMessage("&0Lul.");
		this.setUsage("/lel <text>");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			msg.append(args[i].toUpperCase()).append(' ');
		}
		StringBuilder lelOut = new StringBuilder();
		for (int i = 0; i < msg.length();) {
			for (int j = 0; j < Color.RAINBOW.length; j++) {
				if (i >= msg.length())
					break;
				lelOut.append(Color.RAINBOW[j]).append(ChatColor.MAGIC).append(msg.charAt(i));
				i++;
			}
		}
		Bukkit.broadcastMessage(lelOut.substring(0, lelOut.length() - 1 > 0 ? lelOut.length() - 1 : 0));
		return true;
	}
}
