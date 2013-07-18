package co.sblock.Sblock.PlayerData;

import org.bukkit.Bukkit;

import co.sblock.Sblock.CommandListener;
import co.sblock.Sblock.Sblock;
import co.sblock.Sblock.SblockCommand;

/**
 * Class for holding commands associated with this module.
 * 
 * For more information on how the command system works, please see
 * {@link co.sblock.Sblock.SblockCommand}
 * 
 * @author FireNG
 * 
 */
public class PlayerDataCommands implements CommandListener {

    //Just some test shenanigans, will be removed next commit.
    @SblockCommand
    public boolean test(String player) {
	Bukkit.getPlayerExact(player).sendMessage("It works!");
	Sblock.getInstance().getLogger().info("It works!");
	return true;
    }
    
    @SblockCommand(mergeLast = true)
    public boolean test2(String player, String part1, String part2, String partRest) {
        Bukkit.getPlayerExact(player).sendMessage(String.format("Part1: %s\nPart2: %s\nThe rest: %s", part1, part2, partRest));
        return true;
    }
    
    @SblockCommand(mergeLast = true)
    public boolean test3(String player, String message)
    {
        Bukkit.getPlayerExact(player).sendMessage("Today's inspirational message is: " + message);
        return true;
    }
}
