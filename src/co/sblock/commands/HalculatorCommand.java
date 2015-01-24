package co.sblock.commands;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.Chat;

/**
 * SblockCommand for using the halculate function just for yourself.
 * 
 * @author Jikoo
 */
public class HalculatorCommand extends SblockCommand {

	public HalculatorCommand() {
		super("halculate");
		this.setAliases("halc", "evhal", "evhaluate");
		this.setDescription("Halculate an equation privately.");
		this.setUsage("/halc 1+1");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length == 0) {
			sender.sendMessage("Please enter an equation! Ex. /halc (1+1)^(2/3) + 10");
		} else {
			sender.sendMessage(ChatColor.RED + "Evhaluation: " + ChatColor.GRAY
					+ Chat.getChat().getHalculator().evhaluate(StringUtils.join(args, ' ')));
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
