package co.sblock.commands.chat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for being Lord English.
 * 
 * @author Jikoo
 */
public class LordEnglishCommand extends SblockCommand {

	public LordEnglishCommand(Sblock plugin) {
		super(plugin, "le");
		this.setDescription("&4He's already here!");
		this.setUsage("/le <text>");
		this.setPermissionLevel("horrorterror");
		this.setPermissionMessage("&0Le no. Le /le is reserved for le fancy people.");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			msg.append(args[i].toUpperCase()).append(' ');
		}
		StringBuilder leOut = new StringBuilder();
		for (int i = 0; i < msg.length();) {
			for (int j = 0; j < Color.RAINBOW.length; j++) {
				if (i >= msg.length())
					break;
				leOut.append(Color.RAINBOW[j]).append(msg.charAt(i));
				i++;
			}
		}
		Bukkit.broadcastMessage(leOut.substring(0, leOut.length() - 1 > 0 ? leOut.length() - 1 : 0));
		return true;
	}
}
