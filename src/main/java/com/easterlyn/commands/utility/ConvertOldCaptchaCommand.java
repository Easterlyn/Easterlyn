package com.easterlyn.commands.utility;

import java.util.List;

import com.easterlyn.Easterlyn;
import com.easterlyn.captcha.Captcha;
import com.easterlyn.commands.EasterlynCommand;

import com.google.common.collect.ImmutableList;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * EasterlynCommand for converting all captchacards in an older format to the latest.
 * 
 * @author Jikoo
 */
public class ConvertOldCaptchaCommand extends EasterlynCommand {

	public ConvertOldCaptchaCommand(Easterlyn plugin) {
		super(plugin, "convert");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(getLang().getValue("command.general.noConsole"));
			return true;
		}
		Player player = (Player) sender;
		int conversions = ((Easterlyn) getPlugin()).getModule(Captcha.class).convert(player);
		if (conversions > 0) {
			player.sendMessage(getLang().getValue("command.convert.success").replace("{COUNT}", String.valueOf(conversions)));
		} else {
			player.sendMessage(getLang().getValue("command.convert.failure"));
		}
		return true;
	}

	@NotNull
	@Override
	public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
		return ImmutableList.of();
	}
}
