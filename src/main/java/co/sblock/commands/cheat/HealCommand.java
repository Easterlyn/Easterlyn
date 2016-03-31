package co.sblock.commands.cheat;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.ImmutableList;

import co.sblock.Sblock;
import co.sblock.commands.SblockCommand;

/**
 * SblockCommand for restoring health and saturation.
 * 
 * @author Jikoo
 */
public class HealCommand extends SblockCommand {

	public HealCommand(Sblock plugin) {
		super(plugin, "heal");
		this.setAliases("eat", "feed");
		this.setPermissionLevel("felt");
		this.addExtraPermission("other", "denizen");
	}

	@Override
	protected boolean onCommand(CommandSender sender, String label, String[] args) {
		if (!(sender instanceof Player) && (!sender.hasPermission("sblock.command.heal.other")
				|| args.length < 1)) {
			return false;
		}
		if (args.length == 0 || !sender.hasPermission("sblock.command.heal.other")) {
			heal((Player) sender, label);
			sender.sendMessage(getLang().getValue("command.heal.success"));
			return true;
		}
		List<Player> players = Bukkit.matchPlayer(args[0]);
		if (players.size() == 0) {
			return false;
		}
		heal(players.get(0), label);
		sender.sendMessage(getLang().getValue("command.heal.success"));
		return true;
	}

	private void heal(Player player, String label) {
		if (label.equalsIgnoreCase("heal")) {
			player.setHealth(player.getMaxHealth());
		}
		player.setFoodLevel(20);
		player.setSaturation(20);
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args)
			throws IllegalArgumentException {
		if (!sender.hasPermission(this.getPermission()) || args.length > 1
				|| !sender.hasPermission("sblock.command.heal.other")) {
			return ImmutableList.of();
		}
		return super.tabComplete(sender, alias, args);
	}

}
