package co.sblock.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.utilities.captcha.Captcha;

/**
 * SblockCommand for converting all captchacards in an older format to the latest.
 * 
 * @author Jikoo
 */
public class ConvertOldCaptchaCommand extends SblockCommand {

	public ConvertOldCaptchaCommand() {
		super("convert");
		this.setDescription("Converts captchacards from paper to plastic.");
		this.setUsage("Run /convert with a Captchacard in hand.");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		int conversions = Captcha.convert(player);
		if (conversions > 0) {
			player.sendMessage(ChatColor.GREEN.toString() + conversions + " captchacards converted!");
		} else {
			player.sendMessage(ChatColor.RED + "No old captchacards found!");
		}
		return true;
	}
}
