package co.sblock.commands;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

/**
 * SblockCommand for the voice of a few Eidelons.
 * 
 * @author Jikoo
 */
public class AnaashCommand extends SblockCommand {

	public AnaashCommand() {
		super("an");
		this.setDescription("The voice of the god Anaash. Can be recolored for plaintext messaging.");
		this.setUsage("/an <text>");
		this.setPermissionMessage("&0&lYOU SEE NOTHING.");
		this.setPermissionLevel("denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		Bukkit.broadcastMessage(ChatColor.BLACK + ChatColor.BOLD.toString() + ChatColor.translateAlternateColorCodes('&', StringUtils.join(args, ' ').toUpperCase()));
		return true;
	}
}
