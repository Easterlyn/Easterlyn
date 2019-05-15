package com.easterlyn.commands.info;

import com.easterlyn.Easterlyn;
import com.easterlyn.chat.Chat;
import com.easterlyn.commands.EasterlynCommand;
import com.easterlyn.utilities.TextUtils;
import com.google.common.collect.ImmutableList;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

/**
 * EasterlynCommand for using the halculate function just for yourself.
 * 
 * @author Jikoo
 */
public class HalculatorCommand extends EasterlynCommand {

	private final Chat chat;

	public HalculatorCommand(Easterlyn plugin) {
		super(plugin, "halculate");
		this.setAliases("halc", "evhal", "evhaluate");
		this.chat = plugin.getModule(Chat.class);
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			return false;
		} else {
			UUID uuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
			sender.sendMessage(getLang().getValue("command.halculate.success")
					.replace("{VALUE}", chat.getHalculator().evhaluate(uuid, TextUtils.join(args, ' '))));
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
