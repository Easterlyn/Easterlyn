package co.sblock.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.chat.ColorDef;

/**
 * SblockCommand for /color
 * 
 * @author Jikoo
 */
public class ColourCommand extends SblockCommand {

	public ColourCommand() {
		super("colour");
		this.setDescription("List all colours.");
		this.setUsage("&c/colour");
		ArrayList<String> aliases = new ArrayList<>();
		aliases.add("color");
		this.setAliases(aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		sender.sendMessage(ColorDef.listColors());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
