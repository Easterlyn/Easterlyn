package co.sblock.commands.utility;

import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.captcha.Captcha;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for converting all captchacards in an older format to the latest.
 * 
 * @author Jikoo
 */
public class ConvertOldCaptchaCommand extends SblockCommand {

	public ConvertOldCaptchaCommand(Sblock plugin) {
		super(plugin, "convert");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		int conversions = ((Sblock) getPlugin()).getModule(Captcha.class).convert(player);
		if (conversions > 0) {
			player.sendMessage(getLang().getValue("command.convert.success").replace("{COUNT}", String.valueOf(conversions)));
		} else {
			player.sendMessage(getLang().getValue("command.convert.failure"));
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
