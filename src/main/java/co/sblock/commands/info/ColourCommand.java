package co.sblock.commands.info;

import java.util.List;

import org.bukkit.command.CommandSender;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;

import net.md_5.bungee.api.ChatColor;

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
		StringBuilder sb = new StringBuilder();
		for (ChatColor c : ChatColor.values()) {
			sb.append(c).append('&').append(c.toString().substring(1)).append(' ');
			sb.append(c.name().toLowerCase()).append(ChatColor.RESET).append(' ');
		}
		sender.sendMessage(sb.toString());
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		return ImmutableList.of();
	}
}
