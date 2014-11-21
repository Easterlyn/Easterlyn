package co.sblock.commands;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ColorDef;

/**
 * SblockCommand for /color
 * 
 * @author Jikoo
 */
public class ColorCommand extends SblockCommand {

	public ColorCommand() {
		super("color");
		this.setDescription("List all colors.");
		this.setUsage("&c/color");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		sender.sendMessage(ColorDef.listColors());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String arg1, String[] arg2) {
		// No tab completion.
		return ImmutableList.of();
	}
}
