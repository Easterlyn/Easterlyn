package co.sblock.Sblock.Utilities.Counter;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.SblockCommand;

public class CounterCommandListener implements CommandListener	{

	@SblockCommand(consoleFriendly = true, mergeLast = true)
	public boolean counter(CommandSender sender, String text)	{
		String[] args = text.split(" ");
		if(sender.isOp())	{
			if(args.length == 2)	{
				Player target = Bukkit.getPlayer(args[0]);
				int length = Integer.parseInt(args[1]);
				CounterModule.createCounter(target, length);
				return true;
			}
		}		
		return false;
	}
}
