package co.sblock.Sblock.PlayerData;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import co.sblock.Sblock.CommandListener;
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

	public static final ChatColor PROFILE_COLOR = ChatColor.DARK_AQUA;

	@SblockCommand(consoleFriendly = true)
	public boolean profile(CommandSender sender, String playerToLookup) {
		SblockPlayer player = PlayerManager.getPlayerManager().getPlayer(
				playerToLookup);
		if (player == null)
			sender.sendMessage(ChatColor.YELLOW + "Player not found.");
		else {
			String message = PROFILE_COLOR
					+ "-----------------------------------------\n"
					+ ChatColor.YELLOW + playerToLookup + ": "
					+ player.getClassType().getDisplayName() + " of "
					+ player.getAspect().getDisplayName() + "\n"
					+ PROFILE_COLOR
					+ "-----------------------------------------\n"
					+ "Dream planet: " + ChatColor.YELLOW
					+ player.getDPlanet().getDisplayName() + "\n"
					+ PROFILE_COLOR + "Medium planet: " + ChatColor.YELLOW
					+ player.getDPlanet().getDisplayName() + "\n"
					+ PROFILE_COLOR + "Echeladder rank: " + ChatColor.YELLOW
					+ "Coming soon!\n";
			sender.sendMessage(message);
		}
		return true;
	}
}
