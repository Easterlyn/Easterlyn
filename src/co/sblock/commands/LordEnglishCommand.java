package co.sblock.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import co.sblock.chat.ColorDef;

/**
 * SblockCommand for being Lord English.
 * 
 * @author Jikoo
 */
public class LordEnglishCommand extends SblockCommand {

	public LordEnglishCommand() {
		super("le");
		this.setDescription("&4He's already here!");
		this.setUsage("/le <text>");
		this.setPermission("group.horrorterror");
		this.setPermissionMessage("&0Le no. Le /le is reserved for le fancy people.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		StringBuilder msg = new StringBuilder();
		for (int i = 0; i < args.length; i++) {
			msg.append(args[i].toUpperCase()).append(' ');
		}
		StringBuilder leOut = new StringBuilder();
		for (int i = 0; i < msg.length();) {
			for (int j = 0; j < ColorDef.RAINBOW.length; j++) {
				if (i >= msg.length())
					break;
				leOut.append(ColorDef.RAINBOW[j]).append(msg.charAt(i));
				i++;
			}
		}
		Bukkit.broadcastMessage(leOut.substring(0, leOut.length() - 1 > 0 ? leOut.length() - 1 : 0));
		return true;
	}
}
