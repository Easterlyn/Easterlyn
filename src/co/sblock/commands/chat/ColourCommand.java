package co.sblock.commands.chat;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.chat.Color;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for /color
 * 
 * @author Jikoo
 */
public class ColourCommand extends SblockCommand {

	public ColourCommand(Sblock plugin) {
		super(plugin, "colour");
		this.setAliases("color");
		this.setDescription("List all colours.");
		this.setUsage("/colour");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		sender.sendMessage(Color.listColors());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
