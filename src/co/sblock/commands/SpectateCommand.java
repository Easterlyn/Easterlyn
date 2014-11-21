package co.sblock.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.UserManager;
import co.sblock.utilities.spectator.Spectators;

/**
 * SblockCommand for toggling spectate mode.
 * 
 * @author Jikoo
 */
public class SpectateCommand extends SblockCommand {

	public SpectateCommand() {
		super("spectate");
		this.setDescription("Player: Become the ghost (toggles spectator mode)");
		this.setUsage("/spectate");
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		if (UserManager.getUser(player.getUniqueId()).isServer()) {
			sender.sendMessage(ChatColor.RED + "Perhaps you should focus on helping your client!");
			return true;
		}
		if (Spectators.getSpectators().isSpectator(player.getUniqueId())) {
			sender.sendMessage(ChatColor.GREEN + "Suddenly, you snap back to reality. It was all a dream... wasn't it?");
			Spectators.getSpectators().removeSpectator(player);
		} else {
			sender.sendMessage(ChatColor.GREEN + "You feel a tingling sensation about your extremities as you hover up slightly.");
			Spectators.getSpectators().addSpectator(player);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender arg0, String arg1, String[] arg2) {
		// No tab completion.
		return ImmutableList.of();
	}
}
