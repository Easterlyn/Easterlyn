package co.sblock.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.users.OfflineUser;
import co.sblock.users.Users;
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
		this.setUsage("To toggle spectate mode, use no arguments.\n"
				+ "To prevent players from spectating to you, use /spectate deny\n"
				+ "To allow players to spectate to you, use /spectate allow");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console support not offered at this time.");
			return true;
		}
		Player player = (Player) sender;
		OfflineUser user = Users.getGuaranteedUser(player.getUniqueId());
		if (args.length > 0 && (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("allow"))) {
			user.setSpectatable(true);
			sender.sendMessage(ChatColor.GREEN + "Other players are now allowed to spectate to you!");
			return true;
		}
		if (args.length > 0 && (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("deny"))) {
			user.setSpectatable(false);
			sender.sendMessage(ChatColor.GREEN + "Other players are no longer allowed to spectate to you!");
			return true;
		}
		if (args.length > 0) {
			sender.sendMessage(this.getUsage());
			return true;
		}
		if (user.isServer()) {
			sender.sendMessage(ChatColor.RED + "Perhaps you should focus on helping your client!");
			return true;
		}
		if (Spectators.getInstance().isSpectator(player.getUniqueId())) {
			sender.sendMessage(ChatColor.GREEN + "Suddenly, you snap back to reality. It was all a dream... wasn't it?");
			Spectators.getInstance().removeSpectator(player);
		} else {
			if (player.getGameMode() != GameMode.SURVIVAL) {
				sender.sendMessage(ChatColor.RED + "You can only enter spectate mode from survival.");
				return true;
			}
			sender.sendMessage(ChatColor.GREEN + "You feel a tingling sensation about your extremities as you hover up slightly.");
			Spectators.getInstance().addSpectator(player);
		}
		return true;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		return ImmutableList.of();
	}
}
